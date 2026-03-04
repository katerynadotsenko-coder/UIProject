import json
import sys
from typing import Dict, Set, Any
from google import genai
from google.genai import types

class AIReviewer:
    def __init__(self, api_key: str, config: Dict[str, Any]):
        if not api_key:
            raise ValueError("❌ GEMINI_API_KEY is missing.")
        self.client = genai.Client(api_key=api_key)
        self.model_id = config.get("model_id", "models/gemini-2.0-flash")
        self.system_instruction = config.get("system_instruction", "You are an expert code reviewer.")

        self.response_schema = {
            "type": "OBJECT",
            "properties": {
                "reasoning": {"type": "STRING"},
                "issues": {
                    "type": "ARRAY",
                    "items": {
                        "type": "OBJECT",
                        "properties": {
                            "line": {"type": "INTEGER"},
                            "rule": {"type": "STRING"},
                            "description": {"type": "STRING"},
                            "code": {"type": "STRING"},
                            "extracted_methods": {"type": "STRING"}
                        },
                        "required": ["line", "rule", "description", "code", "extracted_methods"]
                    }
                }
            },
            "required": ["reasoning", "issues"]
        }

    def analyze_code(self, file_path: str, content: str, valid_lines: Set[int]) -> Dict:
        numbered_lines = [f"{i + 1} | {line}" for i, line in enumerate(content.split('\n'))]
        numbered_content = "\n".join(numbered_lines)
        user_prompt = f"FILE ( {file_path} ):\n```java\n{numbered_content}\n```"

        try:
            response = self.client.models.generate_content(
                model=self.model_id,
                contents=user_prompt,
                config=types.GenerateContentConfig(
                    system_instruction=self.system_instruction,
                    temperature=0.0,
                    response_mime_type="application/json",
                    response_schema=self.response_schema
                )
            )

            # --- DEBUG LINES ---
            print(f"RAW AI RESPONSE FOR {file_path}:")
            print(response.text)
            print("--------------------------------------------------")

            # Strip markdown formatting just in case
            cleaned_text = response.text.strip()
            if cleaned_text.startswith("```json"):
                cleaned_text = cleaned_text.removeprefix("```json").removesuffix("```").strip()
            elif cleaned_text.startswith("```"):
                cleaned_text = cleaned_text.removeprefix("```").removesuffix("```").strip()

            parsed_json = json.loads(cleaned_text)
            raw_issues = parsed_json.get("issues", [])

            inline_comments = []
            general_feedback = ""

            for issue in raw_issues:
                line = int(issue.get('line', -1))
                body = (
                    f"### ❌ {issue.get('rule')}\n"
                    f"{issue.get('description')}\n\n"
                    f"### 🛠️ RECOMMENDED FIX\n```java\n{issue.get('code')}\n```\n"
                )
                if issue.get('extracted_methods'):
                    body += f"### 📦 EXTRACTED METHODS\n{issue.get('extracted_methods')}\n"

                if line in valid_lines:
                    inline_comments.append({"path": file_path, "line": line, "body": body})
                else:
                    general_feedback += f"\n**File:** `{file_path}` (Line {line})\n{body}\n---"

            return {"inline": inline_comments, "general": general_feedback}

        except Exception as e:
            error_msg = str(e)
            print(f"💥 AI Error for {file_path}: {repr(e)}")

            # 🚨 Check if it's a Rate Limit error
            if "429" in error_msg or "RESOURCE_EXHAUSTED" in error_msg:
                print("❌ FATAL: Gemini API Rate Limit Exceeded. Failing the GitHub Action!")
                sys.exit(1) # This forces the GitHub Action step to turn RED

            # For other minor errors, return empty so the rest of the files can be checked
            return {"inline": [], "general": ""}