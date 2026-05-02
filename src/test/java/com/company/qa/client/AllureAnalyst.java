package com.company.qa.client;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import com.company.qa.utils.AiConfig;

public interface AllureAnalyst {
    @SystemMessage(AiConfig.SYSTEM_MESSAGE)

    // For when no screenshot is found
    @UserMessage(AiConfig.USER_MESSAGE_NO_SCREENSHOT)
    String analyzeReport(@V("json") String json);

    // For when a screenshot IS found
    String analyzeWithScreenshot(dev.langchain4j.data.message.UserMessage msg);
}
