import os
import yaml
from parser import PatchParser
from github_service import GitHubClient
from ai_service import AIReviewer

def load_config():
    # Try to load custom rules from the repository being reviewed, fallback to local
    config_path = ".ai-reviewer.yml"
    if os.path.exists(config_path):
        with open(config_path, "r") as f:
            return yaml.safe_load(f)
    return {}

def main():
    print("🤖 Booting AI Reviewer...")

    # Securely load environment variables provided by GitHub Actions
    github_token = os.environ.get("GITHUB_TOKEN")
    gemini_key = os.environ.get("GEMINI_API_KEY")
    repo_full_name = os.environ.get("GITHUB_REPOSITORY") # e.g., "owner/repo"
    pr_num = os.environ.get("PR_NUMBER")

    if not all([github_token, gemini_key, repo_full_name, pr_num]):
        print("❌ Missing required environment variables. Exiting.")
        return

    owner, repo = repo_full_name.split("/")

    config = load_config()
    gh = GitHubClient(github_token, owner, repo, pr_num)
    ai = AIReviewer(gemini_key, config)

    files = gh.get_pr_files()
    all_inline = []
    master_general = "🤖 **AI Automated Review Findings:**\n"
    has_general = False

    for f in files:
        path = f['filename']
        # We can make this dynamic later based on the config file
        if "patch" not in f: continue

        print(f"🔎 Analyzing {path}...")
        valid_lines = PatchParser.get_valid_lines(f['patch'])
        content = gh.get_file_content(f['raw_url'])

        results = ai.analyze_code(path, content, valid_lines)
        all_inline.extend(results["inline"])

        if results["general"]:
            has_general = True
            master_general += results["general"]

    if not all_inline and not has_general:
        print("✅ No issues found! LGTM.")
        return

    gh.post_review(master_general if has_general else "", all_inline)

if __name__ == "__main__":
    main()