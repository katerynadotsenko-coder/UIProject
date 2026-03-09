package base;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

public class AllureAiRunner {

    // Helper to find the screenshot in the generated Allure report
    private static byte[] extractScreenshotFromGeneratedReport(JsonNode rootNode, File attachmentsDir) {
        try {
            // In the generated report, teardown steps are inside "afterStages"
            JsonNode afterStages = rootNode.path("afterStages");
            if (afterStages.isArray()) {
                for (JsonNode stage : afterStages) {
                    JsonNode attachments = stage.path("attachments");
                    if (attachments.isArray()) {
                        for (JsonNode attachment : attachments) {
                            if (attachment.path("type").asText().contains("image/png")) {
                                String fileName = attachment.path("source").asText();
                                File screenshotFile = new File(attachmentsDir, fileName);

                                if (screenshotFile.exists()) {
                                    System.out.println("✅ Found screenshot: " + screenshotFile.getName());
                                    return Files.readAllBytes(screenshotFile.toPath());
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error reading screenshot: " + e.getMessage());
        }
        return null;
    }

    public static void main(String[] args) throws IOException {
        // 1. Setup the AI Model
        GoogleAiGeminiChatModel model = GoogleAiGeminiChatModel.builder()
                .apiKey(System.getenv("GEMINI_API_KEY"))
                .modelName("gemini-2.5-flash") // Fast & perfect for log analysis
                .temperature(0.1) // Keep it stable for technical analysis
                .build();
        AllureAnalyst analyst = AiServices.create(AllureAnalyst.class,
                model);

        // 2. Point to the GENERATED test-cases and attachments folders
        File testCasesDir = new File("target/site/allure-maven-plugin/data/test-cases");
        File attachmentsDir = new File("target/site/allure-maven-plugin/data/attachments");

        if (!testCasesDir.exists()) {
            System.out.println("❌ Could not find generated report. Did you run 'allure generate'?");
            return;
        }

        File[] jsonFiles = testCasesDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (jsonFiles == null) return;

        ObjectMapper mapper = new ObjectMapper();

        for (File file : jsonFiles) {
            JsonNode rootNode = mapper.readTree(file);
            String status = rootNode.path("status").asText();

            // 3. Only analyze failed/broken tests
            if ("failed".equals(status) || "broken".equals(status)) {
                System.out.println("🤖 Analyzing failure in: " + rootNode.path("name").asText());

                byte[] screenshotBytes = extractScreenshotFromGeneratedReport(rootNode, attachmentsDir);
                String aiAdvice;

                if (screenshotBytes != null) {
                    // 1. Prepare the Image
                    dev.langchain4j.data.message.ImageContent imageContent =
                            dev.langchain4j.data.message.ImageContent.from(
                                    Base64.getEncoder().encodeToString(screenshotBytes), "image/png"
                            );

                    dev.langchain4j.data.message.TextContent textContent =
                            dev.langchain4j.data.message.TextContent.from(
                                    "JSON: " + rootNode.toString() +
                                            "\nAnalyze this JSON and the attached screenshot strictly following the system rules."
                            );

                    // 3. Combine them into a single UserMessage
                    dev.langchain4j.data.message.UserMessage multimodalMsg =
                            dev.langchain4j.data.message.UserMessage.from(textContent, imageContent);

                    // 4. Send to Gemini!
                    aiAdvice = analyst.analyzeWithScreenshot(multimodalMsg);
                } else {
                    aiAdvice = analyst.analyzeReport(rootNode.toString());
                }
                // 4. Inject the advice into this generated JSON file
                AllureInjectedAnalyst.injectAiAdvice(file, aiAdvice);
            }
        }
    }



}