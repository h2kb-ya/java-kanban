package io.github.h2kb.http.handler;

import com.sun.net.httpserver.HttpExchange;
import io.github.h2kb.exception.TaskIntersectionOfTimeException;
import io.github.h2kb.http.HttpTaskServer;
import io.github.h2kb.manager.TaskManager;
import io.github.h2kb.task.Task;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class AbstractTaskHandler<T extends Task> extends BaseHttpHandler {

    protected final TaskManager taskManager;
    private final Class<T> taskClass;
    private final Supplier<List<T>> allTasksSupplier;
    private final Function<Integer, T> taskByIdFunction;
    private final Function<T, Integer> createTaskFunction;
    private final Consumer<T> updateTaskConsumer;
    private final Consumer<Integer> removeTaskConsumer;

    public AbstractTaskHandler(TaskManager taskManager, Class<T> taskClass,
            Supplier<List<T>> allTasksSupplier,
            Function<Integer, T> taskByIdFunction,
            Function<T, Integer> createTaskFunction,
            Consumer<T> updateTaskFunction,
            Consumer<Integer> removeTaskFunction) {
        this.taskManager = taskManager;
        this.taskClass = taskClass;
        this.allTasksSupplier = allTasksSupplier;
        this.taskByIdFunction = taskByIdFunction;
        this.createTaskFunction = createTaskFunction;
        this.updateTaskConsumer = updateTaskFunction;
        this.removeTaskConsumer = removeTaskFunction;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {

        switch (httpExchange.getRequestMethod()) {
            case "GET" -> handleGet(httpExchange);
            case "POST" -> handlePost(httpExchange);
            case "DELETE" -> handleDelete(httpExchange);
            default -> sendError(httpExchange, 405,
                    String.format("Unsupported request method: %s", httpExchange.getRequestMethod()));
        }
    }

    protected void handleGet(HttpExchange httpExchange) throws IOException {
        Integer taskId = getIdFromPath(httpExchange);

        if (taskId != null) {
            T existedTask = taskByIdFunction.apply(taskId);

            if (existedTask != null) {
                sendText(httpExchange, HttpTaskServer.getGson().toJson(existedTask));
            } else {
                sendError(httpExchange, 404, String.format(TASK_NOT_FOUND, taskClass.getSimpleName(), taskId));
            }
        } else {
            List<T> tasks = allTasksSupplier.get();
            sendText(httpExchange, HttpTaskServer.getGson().toJson(tasks));
        }
    }

    protected void handlePost(HttpExchange httpExchange) throws IOException {
        Integer taskId = getIdFromPath(httpExchange);

        if (taskId != null) {
            T existedTask = taskByIdFunction.apply(taskId);

            if (existedTask != null) {
                T taskFromRequest = getTaskFromRequest(httpExchange, taskClass);
                taskFromRequest.setId(existedTask.getId());
                updateTaskConsumer.accept(taskFromRequest);
                sendText(httpExchange, HttpTaskServer.getGson().toJson(taskFromRequest));
            } else {
                sendError(httpExchange, 404, String.format(TASK_NOT_FOUND, taskClass.getSimpleName(), taskId));
            }
        } else {
            T task = getTaskFromRequest(httpExchange, taskClass);

            try {
                sendText(httpExchange, HttpTaskServer.getGson().toJson(createTaskFunction.apply(task)));
            } catch (TaskIntersectionOfTimeException e) {
                sendError(httpExchange, 406, e.getMessage());
            }
        }
    }

    private void handleDelete(HttpExchange httpExchange) throws IOException {
        Integer taskId = getIdFromPath(httpExchange);

        if (taskId != null) {
            T existedTask = taskByIdFunction.apply(taskId);

            if (existedTask != null) {
                removeTaskConsumer.accept(taskId);
                sendText(httpExchange, HttpTaskServer.getGson().toJson(existedTask));
            } else {
                sendError(httpExchange, 404, String.format(TASK_NOT_FOUND, taskClass.getSimpleName(), taskId));
            }
        }
    }
}

