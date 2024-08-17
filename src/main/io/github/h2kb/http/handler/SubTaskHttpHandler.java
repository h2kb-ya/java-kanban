package io.github.h2kb.http.handler;

import com.sun.net.httpserver.HttpExchange;
import io.github.h2kb.exception.TaskIntersectionOfTimeException;
import io.github.h2kb.http.HttpTaskServer;
import io.github.h2kb.manager.TaskManager;
import io.github.h2kb.task.SubTask;
import java.io.IOException;

public class SubTaskHttpHandler extends AbstractTaskHandler<SubTask> {

    public SubTaskHttpHandler(TaskManager taskManager) {
        super(
                taskManager,
                SubTask.class,
                taskManager::getAllSubTasks,
                taskManager::getSubTask,
                taskManager::createSubTask,
                taskManager::updateSubTask,
                taskManager::removeSubTask
        );
    }

    @Override
    protected void handlePost(HttpExchange httpExchange) throws IOException {
        Integer subTaskId = getIdFromPath(httpExchange);

        if (subTaskId != null) {
            SubTask existedSubTask = taskManager.getSubTask(subTaskId);

            if (existedSubTask != null) {
                SubTask subTaskFromRequest = getTaskFromRequest(httpExchange, SubTask.class);
                subTaskFromRequest.setId(existedSubTask.getId());
                taskManager.updateTask(subTaskFromRequest);
                sendText(httpExchange, HttpTaskServer.getGson().toJson(subTaskFromRequest));
            } else {
                sendError(httpExchange, 404, String.format(TASK_NOT_FOUND, SubTask.class.getSimpleName(), subTaskId));
            }
        } else {
            SubTask subTaskFromRequest = getTaskFromRequest(httpExchange, SubTask.class);

            try {
                sendText(httpExchange, HttpTaskServer.getGson().toJson(taskManager.createSubTask(subTaskFromRequest)));
            } catch (TaskIntersectionOfTimeException | IllegalArgumentException e) {
                sendError(httpExchange, 406, e.getMessage());
            }
        }
    }
}
