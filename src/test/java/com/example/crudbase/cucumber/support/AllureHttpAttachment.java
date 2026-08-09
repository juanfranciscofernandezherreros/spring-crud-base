package com.example.crudbase.cucumber.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.qameta.allure.Allure;
import io.restassured.http.Headers;
import io.restassured.response.Response;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Centralizes Allure HTTP evidence: attaches the actual request and response
 * of every Cucumber API step, masking sensitive headers and JSON fields
 * before they are recorded.
 */
public final class AllureHttpAttachment {

    private static final String MASK = "***MASKED***";

    private static final Set<String> SENSITIVE_HEADERS = Set.of(
            "authorization", "proxy-authorization", "cookie", "set-cookie", "x-api-key", "api-key");

    private static final Set<String> SENSITIVE_JSON_FIELDS = Set.of(
            "password", "token", "accesstoken", "refreshtoken", "secret", "clientsecret", "apikey");

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private AllureHttpAttachment() {
    }

    /**
     * Attaches the outgoing HTTP request (method, URL, headers, body) to the current Allure step.
     *
     * @param method  the HTTP method
     * @param url     the full request URL
     * @param headers the request headers that were sent
     * @param body    the raw request body, or {@code null} if none
     */
    public static void attachRequest(String method, String url, Map<String, String> headers, String body) {
        StringBuilder text = new StringBuilder();
        text.append("Method: ").append(method).append(System.lineSeparator());
        text.append("URL: ").append(url).append(System.lineSeparator());
        headers.forEach((name, value) ->
                text.append(name).append(": ").append(sanitizeHeaderValue(name, value)).append(System.lineSeparator()));
        appendBody(text, body);
        Allure.addAttachment("HTTP Request", "application/json", text.toString(), ".txt");
    }

    /**
     * Attaches the actual HTTP response (status, headers, body) to the current Allure step.
     *
     * @param response the RestAssured response returned by the server
     */
    public static void attachResponse(Response response) {
        StringBuilder text = new StringBuilder();
        text.append("Status: ").append(response.getStatusCode()).append(System.lineSeparator());
        Headers headers = response.getHeaders();
        headers.forEach(header ->
                text.append(header.getName()).append(": ")
                        .append(sanitizeHeaderValue(header.getName(), header.getValue()))
                        .append(System.lineSeparator()));
        String body = response.getBody() != null ? response.getBody().asString() : null;
        appendBody(text, body);
        Allure.addAttachment("HTTP Response", "application/json", text.toString(), ".txt");
    }

    private static void appendBody(StringBuilder text, String body) {
        if (body != null && !body.isBlank()) {
            text.append(System.lineSeparator()).append(sanitizeBody(body));
        }
    }

    private static String sanitizeHeaderValue(String name, String value) {
        return SENSITIVE_HEADERS.contains(name.toLowerCase(Locale.ROOT)) ? MASK : value;
    }

    static String sanitizeBody(String rawBody) {
        try {
            JsonNode node = OBJECT_MAPPER.readTree(rawBody);
            maskSensitiveFields(node);
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(node);
        } catch (Exception notJson) {
            return rawBody;
        }
    }

    private static void maskSensitiveFields(JsonNode node) {
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            for (Map.Entry<String, JsonNode> field : objectNode.properties()) {
                if (SENSITIVE_JSON_FIELDS.contains(field.getKey().toLowerCase(Locale.ROOT))) {
                    objectNode.put(field.getKey(), MASK);
                } else {
                    maskSensitiveFields(field.getValue());
                }
            }
        } else if (node.isArray()) {
            node.forEach(AllureHttpAttachment::maskSensitiveFields);
        }
    }
}
