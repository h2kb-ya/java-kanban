package io.github.h2kb.http.handler;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.github.h2kb.http.HttpTaskServer;
import io.github.h2kb.task.Task;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public abstract class BaseHttpHandler implements HttpHandler {

    protected static final String TASK_NOT_FOUND = "%s with id %d is not found";

    protected void sendText(HttpExchange httpExchange, String text) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        httpExchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        httpExchange.sendResponseHeaders(200, resp.length);
        httpExchange.getResponseBody().write(resp);
        httpExchange.close();
    }

    protected void sendError(HttpExchange httpExchange, int code, String message) throws IOException {
        httpExchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        httpExchange.sendResponseHeaders(code, message.length());
        httpExchange.getResponseBody().write(message.getBytes(StandardCharsets.UTF_8));
        httpExchange.close();
    }

    protected Integer getIdFromPath(HttpExchange httpExchange) throws IOException {
        String[] pathParts = httpExchange.getRequestURI().getPath().split("/");

        if (pathParts.length >= 3) {
            try {
                return Integer.parseInt(pathParts[2]);
            } catch (NumberFormatException e) {
                sendError(httpExchange, 400, "Error getting id from path variable " + e.getMessage());
            }
        }

        return null;
    }

    protected <T extends Task> T getTaskFromRequest(HttpExchange httpExchange, Class<T> taskClass) throws IOException {
        String requestBody = new String(httpExchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        boolean isRequestBodyNullOrEmpty = false;

        try {
            isRequestBodyNullOrEmpty = isRequestBodyNullOrEmpty(requestBody);
        } catch (JsonSyntaxException e) {
            sendError(httpExchange, 400, e.getMessage());
        }

        if (isRequestBodyNullOrEmpty) {
            sendError(httpExchange, 400, "Request body is null or empty");
            return null;
        }

        return HttpTaskServer.getGson().fromJson(requestBody, taskClass);
    }

    private boolean isRequestBodyNullOrEmpty(String requestBody) {
        JsonElement json = JsonParser.parseString(requestBody);

        return json.isJsonNull() || json.getAsJsonObject().entrySet().isEmpty();
    }
}
