package base;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;

public class AllureInjectedAnalyst {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void injectAiAdvice(File jsonFile, String aiAdvice) {
        try {
            ObjectNode root = (ObjectNode) mapper.readTree(jsonFile);

            // 1. Get the current HTML description (if it exists)
            String currentHtml = root.has("descriptionHtml") ? root.get("descriptionHtml").asText() : "";

            // 2. Format the AI advice inside a clean HTML <pre> block for the UI
            String aiHtmlBlock = "<br><hr>" +
                    "<h3>\uD83E\uDD16 AI AQA Assistant Analysis</h3>" +
                    "<pre style=\"white-space: pre-wrap; word-wrap: break-word; background-color: #f8eaec; padding: 15px; border-radius: 5px; border-left: 5px solid #d9534f;\">" +
                    aiAdvice +
                    "</pre>";

            // 3. Inject it straight into the HTML field
            root.put("descriptionHtml", currentHtml + aiHtmlBlock);

            // 4. Save the file
            mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, root);
            System.out.println("✅ AI Advice injected into: " + jsonFile.getName());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}