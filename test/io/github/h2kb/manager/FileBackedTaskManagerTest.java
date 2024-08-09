package io.github.h2kb.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.h2kb.task.Epic;
import io.github.h2kb.task.Status;
import io.github.h2kb.task.SubTask;
import io.github.h2kb.task.Task;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FileBackedTaskManagerTest {

    private TaskManager taskManager;

    @BeforeEach
    void setUp() throws IOException {
        taskManager = Managers.getFileBacked(Files.createTempFile(null, null));
    }

    @Test
    void createdTaskManager_happyPath_managerNotNull() {
        assertNotNull(taskManager);
    }

    @Test
    void createdTaskManager_hasNoAddedTasks_returnEmptyLists() {
        assertTrue(taskManager.getAllTasks().isEmpty());
        assertTrue(taskManager.getAllEpics().isEmpty());
        assertTrue(taskManager.getAllSubTasks().isEmpty());
    }

    @Test
    void createTasks_checkSavedFile_listOfTasksAreEquals() throws NoSuchFieldException, IllegalAccessException {
        Task task = new Task("Task1", "Task Description", Status.NEW);
        Epic epic = new Epic("Epic1", "Epic Description", Status.NEW);

        taskManager.createTask(task);
        Integer epicId = taskManager.createEpic(epic);

        SubTask subTask1 = new SubTask("Subtask1", "Subtask Description", Status.NEW, epicId);
        SubTask subTask2 = new SubTask("Subtask2", "Subtask Description", Status.NEW, epicId);
        SubTask subTask3 = new SubTask("Subtask3", "Subtask Description", Status.NEW, 6);

        taskManager.createSubTask(subTask1);
        taskManager.createSubTask(subTask2);
        taskManager.createSubTask(subTask3);

        Field storageFile = taskManager.getClass().getDeclaredField("storageFile");
        storageFile.setAccessible(true);
        Path pathToFile = (Path) storageFile.get(taskManager);

        TaskManager savedTaskManager = Managers.getFileBacked(pathToFile);

        List<Task> savedTasks = savedTaskManager.getAllTasks();
        List<SubTask> savedSubTasks = savedTaskManager.getAllSubTasks();
        List<Epic> savedEpics = savedTaskManager.getAllEpics();

        List<Task> tasks = taskManager.getAllTasks();
        List<SubTask> subTasks = taskManager.getAllSubTasks();
        List<Epic> epics = taskManager.getAllEpics();

        assertEquals(tasks.size(), savedTasks.size());
        assertEquals(subTasks.size(), savedSubTasks.size());
        assertEquals(epics.size(), savedEpics.size());

        assertEquals(tasks, savedTasks);
        assertEquals(subTasks, savedSubTasks);
        assertEquals(epics, savedEpics);
    }
}
