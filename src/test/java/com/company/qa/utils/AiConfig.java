package com.company.qa.utils;

public class AiConfig {
    public static final String MODEL_NAME = "gemini-2.5-flash";
    public static final double TEMPERATURE = 0.1;

    // Directory paths
    public static final String TEST_CASES_DIR = "target/site/allure-maven-plugin/data/test-cases";
    public static final String ATTACHMENTS_DIR = "target/site/allure-maven-plugin/data/attachments";

    // Prompts
    public static final String SYSTEM_MESSAGE = """
            You are a strict, concise Senior Java AQA. Strictly English.

            CRITICAL RULES:
            1. Do NOT invent product names, data, or guess what might be off-screen.
            2. ONLY describe what you physically see in the image.
            3. If the error described in the JSON is NOT visible in the screenshot, explicitly state: "The elements causing the error are not visible in this screenshot."

            RESPONSE FORMAT:
            1. TYPE: (Bug/Flaky/Env)
            2. REASON: (Max 1 sentence explaining the JSON error)
            3. CODE: (Exact class and method from stacktrace)
            4. SCREENSHOT: (Max 2 sentences. Fact-check the JSON against the image truthfully.)

            FORBIDDEN: Introductory words, greetings, essays, or suggested fixes.
            """;

    public static final String USER_MESSAGE_NO_SCREENSHOT = "JSON: {{json}}\nAnalyze the error. No screenshot is attached.";

    public static final String MULTIMODAL_PROMPT_PREFIX = "JSON: ";
    public static final String MULTIMODAL_PROMPT_SUFFIX = "\n\n" +
            "You are a strict Senior Java AQA. Analyze the JSON error and the attached screenshot.\n" +
            "CRITICAL RULES: Do NOT write essays. Do NOT invent data. ONLY describe what you physically see.\n\n" +
            "YOU MUST USE THIS EXACT FORMAT AND NOTHING ELSE:\n" +
            "**TYPE:** (Bug / Flaky / Env)\n" +
            "**REASON:** (1-2 sentences explaining the core issue from the JSON)\n" +
            "**CODE:** (Exact class and method from the stacktrace)\n" +
            "**SCREENSHOT:** (1-2 sentences. Fact-check the JSON against the image truthfully. If the error is off-screen, state: 'The elements causing the error are not visible in this screenshot.')";

    // Fallback message
    public static final String FALLBACK_MESSAGE = "⚠️ **AI AQA Assistant:** Analysis unavailable. Gemini API rate limit exceeded or a network error occurred.";
}
