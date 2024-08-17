package io.github.h2kb.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.h2kb.manager.InMemoryTaskManager;
import io.github.h2kb.manager.TaskManager;
import io.github.h2kb.task.Epic;
import io.github.h2kb.task.Status;
import io.github.h2kb.task.SubTask;
import io.github.h2kb.task.Task;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HttpTaskServerTest {

    TaskManager taskManager = new InMemoryTaskManager();
    HttpTaskServer httpTaskServer = new HttpTaskServer(taskManager);
    Gson gson = HttpTaskServer.getGson();

    public HttpTaskServerTest() throws IOException {
    }

    @BeforeEach
    public void setUp() {
        taskManager.clearTasks();
        taskManager.clearSubTasks();
        taskManager.clearEpics();
        httpTaskServer.start();
    }

    @AfterEach
    public void shutDown() {
        httpTaskServer.stop();
    }

    @Test
    public void getAllTasks_tasksDoNotExist_returnEmptyJson() throws IOException, InterruptedException {
        assertTrue(taskManager.getAllTasks().isEmpty());

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8090/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<Task> tasks = gson.fromJson(response.body(), new TypeToken<List<Task>>() {
        }.getType());
        assertTrue(tasks.isEmpty());
        assertEquals(200, response.statusCode());
    }

    @Test
    public void getTaskById_taskDoesNotExist_return404() throws IOException, InterruptedException {
        assertTrue(taskManager.getAllTasks().isEmpty());

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8090/tasks/1");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    public void getTaskById_taskExists_returnTask() throws IOException, InterruptedException {
        Task task = new Task("Task name", "Task description",
                Status.NEW, Duration.ofMinutes(5), LocalDateTime.now());
        Integer taskId = taskManager.createTask(task);
        assertEquals(1, taskManager.getAllTasks().size());

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(String.format("http://localhost:8090/tasks/%d", taskId));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Task retrievedTask = gson.fromJson(response.body(), Task.class);

        assertEquals(taskId, retrievedTask.getId());
        assertEquals(task.getName(), retrievedTask.getName());
        assertEquals(task.getDescription(), retrievedTask.getDescription());
        assertEquals(Status.NEW, retrievedTask.getStatus());
    }

    @Test
    public void createTask_happyPath_return200() throws IOException, InterruptedException {
        Task task = new Task("Task name", "Task description",
                Status.NEW, Duration.ofMinutes(5), LocalDateTime.now());
        String taskJson = gson.toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8090/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Task> tasksFromManager = taskManager.getAllTasks();

        assertNotNull(tasksFromManager);
        assertEquals(1, tasksFromManager.size());
        assertEquals("Task name", tasksFromManager.getFirst().getName());
    }

    @Test
    public void createTasks_tasksHaveIntersectionOfTime_return406() throws IOException, InterruptedException {
        Task task = new Task("Task name", "Task description",
                Status.NEW, Duration.ofMinutes(5), LocalDateTime.now());
        taskManager.createTask(task);
        String taskJson = gson.toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8090/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode());
    }

    @Test
    public void updateTask_taskDoesNotExist_return404() throws IOException, InterruptedException {
        Task task = new Task("Task name", "Task description",
                Status.NEW, Duration.ofMinutes(5), LocalDateTime.now());
        String taskJson = gson.toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8090/tasks/1");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    public void updateTask_taskExists_returnUpdatedTask() throws IOException, InterruptedException {
        Task task = new Task("Task name", "Task description",
                Status.NEW, Duration.ofMinutes(5), LocalDateTime.now());
        Integer taskId = taskManager.createTask(task);
        task.setName("Updated task name");
        String taskJson = gson.toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(String.format("http://localhost:8090/tasks/%d", taskId));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Task retrievedTask = gson.fromJson(response.body(), Task.class);

        assertEquals(task.getName(), retrievedTask.getName());
    }

    @Test
    public void deleteTask_taskDoesNotExist_return404() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8090/tasks/1");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    public void deleteTask_taskExists_returnIdOfDeletedTask() throws IOException, InterruptedException {
        Task task = new Task("Task name", "Task description",
                Status.NEW, Duration.ofMinutes(5), LocalDateTime.now());
        Integer taskId = taskManager.createTask(task);
        assertFalse(taskManager.getAllTasks().isEmpty());

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(String.format("http://localhost:8090/tasks/%d", taskId));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        assertTrue(taskManager.getAllTasks().isEmpty());
    }

    @Test
    public void createSubTask_epicDoesNotExist_return406() throws IOException, InterruptedException {
        SubTask subTask = new SubTask("Subtask name", "Subtask description",
                Status.NEW, 1, Duration.ofMinutes(5), LocalDateTime.now());
        String subTaskJson = gson.toJson(subTask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8090/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subTaskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode());
    }

    @Test
    public void createSubTask_happyPath_return200() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic name", "Epic description", Status.NEW);
        Integer epicId = taskManager.createEpic(epic);
        SubTask subTask = new SubTask("Subtask name", "Subtask description",
                Status.NEW, epicId, Duration.ofMinutes(5), LocalDateTime.now());
        String taskJson = gson.toJson(subTask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8090/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<SubTask> tasksFromManager = taskManager.getAllSubTasks();

        assertNotNull(tasksFromManager);
        assertEquals(1, tasksFromManager.size());
        assertEquals("Subtask name", tasksFromManager.getFirst().getName());
    }

    @Test
    public void getSubTasksByEpicId_epicDoesNotExist_return404() throws IOException, InterruptedException {

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8090/epics/1/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    public void getSubTasksByEpicId_epicExists_returnSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic name", "Epic description", Status.NEW);
        Integer epicId = taskManager.createEpic(epic);
        assertTrue(taskManager.getSubTasksByEpicId(epicId).isEmpty());

        SubTask subTask1 = new SubTask("Subtask1 name", "Subtask1 description",
                Status.NEW, epicId, Duration.ofMinutes(5), LocalDateTime.now());
        taskManager.createSubTask(subTask1);

        SubTask subTask2 = new SubTask("Subtask2 name", "Subtask2 description",
                Status.NEW, epicId, Duration.ofMinutes(5), LocalDateTime.now().plusMinutes(10));
        taskManager.createSubTask(subTask2);

        SubTask subTask3 = new SubTask("Subtask3 name", "Subtask3 description",
                Status.NEW, epicId, Duration.ofMinutes(5), LocalDateTime.now().plusMinutes(20));
        taskManager.createSubTask(subTask3);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(String.format("http://localhost:8090/epics/%d/subtasks", epicId));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<SubTask> retrievedSubTasks = gson.fromJson(response.body(), new TypeToken<List<SubTask>>() {
        }.getType());
        assertNotNull(retrievedSubTasks);
        assertEquals(3, retrievedSubTasks.size());
    }

    @Test
    public void getPrioritized_hasNoPrioritized_returnEmptyJson() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8090/prioritized");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<Task> tasks = gson.fromJson(response.body(), new TypeToken<List<Task>>() {
        }.getType());
        assertTrue(tasks.isEmpty());
        assertEquals(200, response.statusCode());
    }

    @Test
    public void getPrioritized_hasPrioritized_returnPrioritizedTasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic name", "Epic description", Status.NEW);
        Integer epicId = taskManager.createEpic(epic);
        assertTrue(taskManager.getSubTasksByEpicId(epicId).isEmpty());

        SubTask subTask1 = new SubTask("Subtask1 name", "Subtask1 description",
                Status.NEW, epicId, Duration.ofMinutes(5), LocalDateTime.now());
        taskManager.createSubTask(subTask1);

        SubTask subTask2 = new SubTask("Subtask2 name", "Subtask2 description",
                Status.NEW, epicId, Duration.ofMinutes(5), LocalDateTime.now().plusMinutes(10));
        taskManager.createSubTask(subTask2);

        SubTask subTask3 = new SubTask("Subtask3 name", "Subtask3 description",
                Status.NEW, epicId, Duration.ofMinutes(5), LocalDateTime.now().plusMinutes(20));
        taskManager.createSubTask(subTask3);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(String.format("http://localhost:8090/prioritized", epicId));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<SubTask> retrievedSubTasks = gson.fromJson(response.body(), new TypeToken<List<SubTask>>() {
        }.getType());
        assertNotNull(retrievedSubTasks);
        assertEquals(3, retrievedSubTasks.size());
        assertEquals("Subtask1 name", retrievedSubTasks.get(0).getName());
        assertEquals("Subtask2 name", retrievedSubTasks.get(1).getName());
        assertEquals("Subtask3 name", retrievedSubTasks.get(2).getName());

        assertEquals(Duration.ofMinutes(15), taskManager.getEpic(epicId).getDuration());
        assertEquals(retrievedSubTasks.getFirst().getStartTime(), taskManager.getEpic(epicId).getStartTime());
        assertEquals(retrievedSubTasks.getLast().getEndTime(), taskManager.getEpic(epicId).getEndTime());
    }

    @Test
    public void getHistory_hasNoHistory_returnEmptyJson() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8090/history");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<Task> tasks = gson.fromJson(response.body(), new TypeToken<List<Task>>() {
        }.getType());
        assertTrue(tasks.isEmpty());
        assertEquals(200, response.statusCode());
    }

    @Test
    public void getHistory_hasHistory_returnEmptyJson() throws IOException, InterruptedException {
        Task task1 = new Task("Task1 name", "Task1 description",
                Status.NEW, Duration.ofMinutes(5), LocalDateTime.now());
        Integer task1Id = taskManager.createTask(task1);

        Task task2 = new Task("Task2 name", "Task2 description",
                Status.NEW, Duration.ofMinutes(5), LocalDateTime.now().plusMinutes(10));
        Integer task2Id = taskManager.createTask(task2);

        Task task3 = new Task("Task3 name", "Task3 description",
                Status.NEW, Duration.ofMinutes(5), LocalDateTime.now().plusMinutes(20));
        Integer task3Id = taskManager.createTask(task3);

        assertTrue(taskManager.getHistory().isEmpty());

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(String.format("http://localhost:8090/tasks/%d", task1Id));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        client.send(request, HttpResponse.BodyHandlers.ofString());

        url = URI.create(String.format("http://localhost:8090/tasks/%d", task2Id));
        request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        client.send(request, HttpResponse.BodyHandlers.ofString());

        url = URI.create(String.format("http://localhost:8090/tasks/%d", task3Id));
        request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        client.send(request, HttpResponse.BodyHandlers.ofString());

        url = URI.create("http://localhost:8090/history");
        request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Task> tasksFromManager = taskManager.getAllTasks();

        assertNotNull(tasksFromManager);
        assertEquals(3, tasksFromManager.size());
        assertEquals("Task1 name", tasksFromManager.get(0).getName());
        assertEquals("Task2 name", tasksFromManager.get(1).getName());
        assertEquals("Task3 name", tasksFromManager.get(2).getName());
    }
}
