package io.github.h2kb.http.handler;

import com.sun.net.httpserver.HttpExchange;
import io.github.h2kb.http.HttpTaskServer;
import io.github.h2kb.manager.TaskManager;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrioritizedHttpHandler extends BaseHttpHandler {

    private final TaskManager taskManager;
    private static final Logger logger = LoggerFactory.getLogger(PrioritizedHttpHandler.class);

    public PrioritizedHttpHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        logger.info("Handling {} {}", httpExchange.getRequestMethod(), httpExchange.getRequestURI());

        if (httpExchange.getRequestMethod().equals("GET")) {
            sendText(httpExchange, HttpTaskServer.getGson().toJson(taskManager.getPrioritizedTasks()));
        } else {
            sendError(httpExchange, 405,
                    String.format("Unsupported request method: %s", httpExchange.getRequestMethod()));
        }
    }
}
