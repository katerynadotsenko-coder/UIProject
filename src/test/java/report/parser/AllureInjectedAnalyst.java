package report.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class AllureInjectedAnalyst {
    private static final Logger logger = LoggerFactory.getLogger(AllureInjectedAnalyst.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void injectAiAdvice(File jsonFile, String aiAdvice) {
        try {
            ObjectNode root = (ObjectNode) mapper.readTree(jsonFile);

            // 1. Get the current HTML description (if it exists)
            String currentHtml = root.has("descriptionHtml") ? root.get("descriptionHtml").asText() : "";

            // 2. Format and inject the AI advice
            String updatedHtml = utils.HtmlFormatter.appendAiAdvice(currentHtml, aiAdvice);

            // 3. Inject it straight into the HTML field
            root.put("descriptionHtml", updatedHtml);

            // 4. Save the file
            mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, root);
            logger.info("✅ AI Advice injected into: {}", jsonFile.getName());

        } catch (Exception e) {
            logger.error("❌ Failed to inject AI advice into {}", jsonFile.getName(), e);
        }
    }
}
