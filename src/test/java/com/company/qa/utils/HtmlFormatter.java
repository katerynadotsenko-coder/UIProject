package com.company.qa.utils;

public class HtmlFormatter {
    private static final String AI_ADVICE_TEMPLATE = "<br><hr>" +
            "<h3>\uD83E\uDD16 AI AQA Assistant Analysis</h3>" +
            "<pre style=\"white-space: pre-wrap; word-wrap: break-word; background-color: #f8eaec; padding: 15px; border-radius: 5px; border-left: 5px solid #d9534f;\">%s</pre>";

    public static String appendAiAdvice(String currentHtml, String aiAdvice) {
        String safeHtml = currentHtml != null ? currentHtml : "";
        String formattedAdvice = String.format(AI_ADVICE_TEMPLATE, aiAdvice);
        return safeHtml + formattedAdvice;
    }
}
