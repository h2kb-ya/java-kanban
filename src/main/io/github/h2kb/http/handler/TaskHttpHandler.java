package io.github.h2kb.http.handler;

import io.github.h2kb.manager.TaskManager;
import io.github.h2kb.task.Task;

public class TaskHttpHandler extends AbstractTaskHandler<Task> {

    public TaskHttpHandler(TaskManager taskManager) {
        super(
                taskManager,
                Task.class,
                taskManager::getAllTasks,
                taskManager::getTask,
                taskManager::createTask,
                taskManager::updateTask,
                taskManager::removeTask
        );
    }
}
