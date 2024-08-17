package io.github.h2kb.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import io.github.h2kb.http.adapter.DurationAdaptor;
import io.github.h2kb.http.adapter.LocalDateTimeAdaptor;
import io.github.h2kb.http.handler.EpicHttpHandler;
import io.github.h2kb.http.handler.HistoryHttpHandler;
import io.github.h2kb.http.handler.PrioritizedHttpHandler;
import io.github.h2kb.http.handler.SubTaskHttpHandler;
import io.github.h2kb.http.handler.TaskHttpHandler;
import io.github.h2kb.manager.Managers;
import io.github.h2kb.manager.TaskManager;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

public class HttpTaskServer {

    private static final int PORT = 8090;
    private final HttpServer httpServer;
    private final TaskManager taskManager;

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = taskManager;
        httpServer = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);
        httpServer.createContext("/tasks", new TaskHttpHandler(taskManager));
        httpServer.createContext("/epics", new EpicHttpHandler(taskManager));
        httpServer.createContext("/subtasks", new SubTaskHttpHandler(taskManager));
        httpServer.createContext("/history", new HistoryHttpHandler(taskManager));
        httpServer.createContext("/prioritized", new PrioritizedHttpHandler(taskManager));
    }

    public static void main(String[] args) throws IOException {
        HttpTaskServer httpTaskServer = new HttpTaskServer(Managers.getDefault());
        httpTaskServer.start();
    }

    public void start() {
        httpServer.start();
    }

    public void stop() {
        httpServer.stop(0);
    }

    public static Gson getGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdaptor())
                .registerTypeAdapter(Duration.class, new DurationAdaptor())
                .create();
    }
}
