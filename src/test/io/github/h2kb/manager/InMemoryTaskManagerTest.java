package io.github.h2kb.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.h2kb.task.Epic;
import io.github.h2kb.task.Status;
import io.github.h2kb.task.SubTask;
import io.github.h2kb.task.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InMemoryTaskManagerTest {

    private TaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = Managers.getDefault();
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
    void createUpdateDeleteTask_happyPath_noError() {
        Task task = new Task("Task name", "Task description", Status.NEW);
        Integer taskId = taskManager.createTask(task);
        assertNotNull(taskId);
        assertNotNull(taskManager.getTask(taskId));

        task.setStatus(Status.IN_PROGRESS);
        taskManager.updateTask(task);
        assertEquals(Status.IN_PROGRESS, taskManager.getTask(taskId).getStatus());

        taskManager.removeTask(taskId);
        assertNull(taskManager.getTask(taskId));
    }

    @Test
    void createUpdateDeleteEpic_happyPath_noError() {
        Epic epic = new Epic("Epic name", "Epic description", Status.NEW);
        Integer epicId = taskManager.createTask(epic);
        assertNotNull(epicId);
        assertNotNull(taskManager.getEpic(epicId));

        SubTask subTask1 = new SubTask("Subtask 1", "Subtask description", Status.NEW, epicId);
        SubTask subTask2 = new SubTask("Subtask 2", "Subtask description", Status.NEW, epicId);
        Integer subTask1Id = taskManager.createTask(subTask1);
        Integer subTask2Id = taskManager.createTask(subTask2);
        assertEquals(2, taskManager.getSubTasksByEpicId(epicId).size());

        subTask1.setStatus(Status.IN_PROGRESS);
        taskManager.updateTask(subTask1);
        assertEquals(Status.IN_PROGRESS, taskManager.getSubTask(subTask1Id).getStatus());
        assertEquals(Status.IN_PROGRESS, taskManager.getEpic(epicId).getStatus());

        subTask1.setStatus(Status.DONE);
        taskManager.updateTask(subTask1);
        assertEquals(Status.DONE, taskManager.getSubTask(subTask1Id).getStatus());
        assertEquals(Status.IN_PROGRESS, taskManager.getEpic(epicId).getStatus());

        subTask2.setStatus(Status.DONE);
        taskManager.updateTask(subTask2);
        assertEquals(Status.DONE, taskManager.getSubTask(subTask2Id).getStatus());
        assertEquals(Status.DONE, taskManager.getEpic(epicId).getStatus());

        SubTask subTask3 = new SubTask("Subtask 3", "Subtask description", Status.NEW, epicId);
        Integer subTask3Id = taskManager.createTask(subTask3);
        assertEquals(3, taskManager.getSubTasksByEpicId(epicId).size());
        assertEquals(Status.NEW, taskManager.getSubTask(subTask3Id).getStatus());
        assertEquals(Status.IN_PROGRESS, taskManager.getEpic(epicId).getStatus());

        subTask3.setStatus(Status.DONE);
        taskManager.updateTask(subTask3);
        assertEquals(Status.DONE, taskManager.getSubTask(subTask3Id).getStatus());
        assertEquals(Status.DONE, taskManager.getEpic(epicId).getStatus());

        taskManager.removeEpic(epicId);
        assertNull(taskManager.getEpic(epicId));
        assertTrue(taskManager.getSubTasksByEpicId(epicId).isEmpty());
    }

    @Test
    void clearTasks_happyPath_noError() {
        Task task1 = new Task("Task1 name", "Task description", Status.NEW);
        Task task2 = new Task("Task2 name", "Task description", Status.NEW);
        Task task3 = new Task("Task3 name", "Task description", Status.NEW);

        taskManager.createTask(task1);
        taskManager.createTask(task2);
        taskManager.createTask(task3);
        assertEquals(3, taskManager.getAllTasks().size());

        taskManager.clearTasks();
        assertTrue(taskManager.getAllSubTasks().isEmpty());
    }

    @Test
    void clearEpics_happyPath_epicsAndSubTasksRemoved() {
        Epic epic1 = new Epic("Epic1 name", "Epic1 description", Status.NEW);
        Epic epic2 = new Epic("Epic2 name", "Epic2 description", Status.NEW);
        Integer epic1Id = taskManager.createTask(epic1);
        Integer epic2Id = taskManager.createTask(epic2);
        assertEquals(2, taskManager.getAllEpics().size());

        SubTask subTask1 = new SubTask("Subtask 1", "Subtask description", Status.NEW, epic1Id);
        SubTask subTask2 = new SubTask("Subtask 2", "Subtask description", Status.NEW, epic1Id);
        SubTask subTask3 = new SubTask("Subtask 3", "Subtask description", Status.NEW, epic2Id);
        taskManager.createTask(subTask1);
        taskManager.createTask(subTask2);
        taskManager.createTask(subTask3);
        assertEquals(3, taskManager.getAllSubTasks().size());

        taskManager.clearEpics();
        assertTrue(taskManager.getAllEpics().isEmpty());
        assertTrue(taskManager.getAllSubTasks().isEmpty());
    }

    @Test
    void removeSubTask_happyPath_subTasksRemovedAndEpicHasNoSubTaskId() {
        Epic epic = new Epic("Epic name", "Epic description", Status.NEW);
        Integer epicId = taskManager.createTask(epic);
        SubTask subTask1 = new SubTask("Subtask 1", "Subtask description", Status.NEW, epicId);
        SubTask subTask2 = new SubTask("Subtask 2", "Subtask description", Status.NEW, epicId);
        taskManager.createTask(subTask1);
        taskManager.createTask(subTask2);
        assertEquals(2, taskManager.getSubTasksByEpicId(epicId).size());

        SubTask subTask3 = new SubTask("Subtask 3", "Subtask description", Status.NEW, epicId);
        taskManager.createTask(subTask3);
        assertEquals(3, taskManager.getSubTasksByEpicId(epicId).size());

        taskManager.removeSubTask(subTask3.getId());
        taskManager.removeSubTask(subTask2.getId());
        assertEquals(1, taskManager.getSubTasksByEpicId(epicId).size());

        taskManager.clearSubTasks();
        assertTrue(taskManager.getSubTasksByEpicId(epicId).isEmpty());
    }

    @Test
    void checkTasksForEquality_happyPath_tasksEqual() {
        Task task = new Task("Task name", "Task description", Status.NEW);
        Integer taskId = taskManager.createTask(task);

        assertEquals(taskManager.getTask(taskId), taskManager.getTask(taskId));
    }

    @Test
    void checkTasksForEquality_happyPath_tasksNotEqual() {
        Task task1 = new Task("Task name", "Task description", Status.NEW);
        Task task2 = new Task("Task name", "Task description", Status.NEW);
        Integer task1Id = taskManager.createTask(task1);
        Integer task2Id = taskManager.createTask(task2);

        assertNotEquals(taskManager.getTask(task1Id), taskManager.getTask(task2Id));
    }

    @Test
    void checkEpicsForEquality_happyPath_tasksEqual() {
        Epic epic = new Epic("Epic name", "Epic description", Status.NEW);
        Integer epicId = taskManager.createTask(epic);

        assertEquals(taskManager.getTask(epicId), taskManager.getTask(epicId));
    }

    @Test
    void createSubTask_wrongEpicId_subTaskNotCreated() {
        SubTask subTask = new SubTask("Subtask", "Subtask description", Status.NEW, 333);
        taskManager.createTask(subTask);

        assertTrue(taskManager.getAllSubTasks().isEmpty());
    }
}