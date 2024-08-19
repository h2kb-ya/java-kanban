package io.github.h2kb.http.handler;

import com.sun.net.httpserver.HttpExchange;
import io.github.h2kb.exception.TaskIntersectionOfTimeException;
import io.github.h2kb.http.HttpTaskServer;
import io.github.h2kb.manager.TaskManager;
import io.github.h2kb.task.Epic;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EpicHttpHandler extends AbstractTaskHandler<Epic> {

    public EpicHttpHandler(TaskManager taskManager) {
        super(
                taskManager,
                Epic.class,
                taskManager::getAllEpics,
                taskManager::getEpic,
                taskManager::createEpic,
                taskManager::updateEpic,
                taskManager::removeEpic
        );
    }

    @Override
    protected void handleGet(HttpExchange httpExchange) throws IOException {
        Integer epicId = getIdFromPath(httpExchange);

        if (epicId != null) {
            Epic existedEpic = taskManager.getEpic(epicId);

            if (existedEpic != null) {

                if (httpExchange.getRequestURI().getPath().contains("subtasks")) {
                    sendText(httpExchange, HttpTaskServer.getGson().toJson(taskManager.getSubTasksByEpicId(epicId)));
                } else {
                    sendText(httpExchange, HttpTaskServer.getGson().toJson(existedEpic));
                }
            } else {
                sendError(httpExchange, 404, String.format(TASK_NOT_FOUND, Epic.class.getSimpleName(), epicId));
            }
        } else {
            List<Epic> epics = taskManager.getAllEpics();
            sendText(httpExchange, HttpTaskServer.getGson().toJson(epics));
        }
    }

    @Override
    protected void handlePost(HttpExchange httpExchange) throws IOException {
        Integer epicId = getIdFromPath(httpExchange);

        if (epicId != null) {
            Epic existedEpic = taskManager.getEpic(epicId);

            if (existedEpic != null) {
                Epic epicFromRequest = getTaskFromRequest(httpExchange, Epic.class);
                epicFromRequest.setId(existedEpic.getId());

                if (epicFromRequest.getSubTaskIds() == null || epicFromRequest.getSubTaskIds().isEmpty()) {
                    epicFromRequest.setSubTaskIds(existedEpic.getSubTaskIds());
                }
                taskManager.updateEpic(epicFromRequest);
                sendText(httpExchange, HttpTaskServer.getGson().toJson(epicFromRequest));
            } else {
                sendError(httpExchange, 404, String.format(TASK_NOT_FOUND, Epic.class.getSimpleName(), epicId));
            }
        } else {
            Epic epicFromRequest = getTaskFromRequest(httpExchange, Epic.class);

            if (epicFromRequest.getSubTaskIds() == null || epicFromRequest.getSubTaskIds().isEmpty()) {
                epicFromRequest.setSubTaskIds(new ArrayList<>());
            }

            try {
                sendText(httpExchange, HttpTaskServer.getGson().toJson(taskManager.createEpic(epicFromRequest)));
            } catch (TaskIntersectionOfTimeException e) {
                sendError(httpExchange, 406, e.getMessage());
            }
        }
    }
}
