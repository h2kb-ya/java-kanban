package io.github.h2kb.manager;

import io.github.h2kb.task.Epic;
import io.github.h2kb.task.SubTask;
import io.github.h2kb.task.Task;
import io.github.h2kb.task.TaskStatus;

public class TaskManagerTest {

    public static void main(String[] args) {
        createTaskManager_happyPath_createdTaskManagerWithoutTasks();
        createUpdateDeleteTask_happyPath_noError();
        createUpdateDeleteEpic_happyPath_noError();
        clearTasks_happyPath_noError();
        clearEpics_happyPath_epicsAndSubTasksRemoved();
        removeSubTask_happyPath_subTasksRemovedAndEpicHasNoSubTaskId();
    }

    private static void createTaskManager_happyPath_createdTaskManagerWithoutTasks() {
        TaskManager taskManager = new TaskManager();

        assert taskManager.getAllTasks().isEmpty() : "Спискок задач должен быть пуст";
        assert taskManager.getAllEpics().isEmpty() : "Список эпиков должен быть пуст";
        assert taskManager.getAllSubTasks().isEmpty() : "Список подзадач должен быть пуст";

        System.out.println("Test 'createTaskManager_happyPath_createdTaskManagerWithoutTasks()' passed.");
    }

    private static void createUpdateDeleteTask_happyPath_noError() {
        TaskManager taskManager = new TaskManager();

        Task task = new Task("Бить баклуши", "Описание как бить баклуши", TaskStatus.NEW);
        Integer taskId = taskManager.createTask(task);
        assert taskId != null : "Id задачи не может быть пустым";
        assert taskManager.getTask(taskId) != null : "Список задач должен содержать задачу с id " + taskId;

        task.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateTask(task);
        assert taskManager.getTask(taskId).getStatus() == TaskStatus.IN_PROGRESS :
                "Статус задачи должен быть " + TaskStatus.IN_PROGRESS;

        taskManager.removeTask(taskId);
        assert !taskManager.isTaskExist(taskId) : "Задача с id " + taskId + " должна быть удалена";

        System.out.println("Test 'createUpdateDeleteTask_happyPath_noError()' passed.");
    }

    private static void createUpdateDeleteEpic_happyPath_noError() {
        TaskManager taskManager = new TaskManager();

        Epic epic = new Epic("Посмотреть Гарри Поттера", "Уже давно хотелось", TaskStatus.NEW);
        Integer epicId = taskManager.createTask(epic);

        SubTask subTask1 = new SubTask("Купить телевизор", "Без него Гарри не посмотреть", TaskStatus.NEW, epicId);
        SubTask subTask2 = new SubTask("Купить подписку на Кинопоиск", "Хотя можно было бы и спиратить", TaskStatus.NEW,
                epicId);
        Integer subTask1Id = taskManager.createTask(subTask1);
        Integer subTask2Id = taskManager.createTask(subTask2);
        assert taskManager.getSubTasksByEpicId(epicId).size() == 2 : "Эпик должен иметь две подзадачи";
        assert taskManager.getSubTasksByEpicId(epicId).stream().
                anyMatch(i -> i.getId().equals(subTask1Id)) : "Эпик должен быть связан с подзадачей 1";
        assert taskManager.getSubTasksByEpicId(epicId).stream().
                anyMatch(i -> i.getId().equals(subTask2Id)) : "Эпик должен быть связан с подзадачей 2";

        subTask1.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateTask(subTask1);
        assert taskManager.getSubTask(subTask1Id).getStatus() == TaskStatus.IN_PROGRESS :
                "Статус подзадачи должен быть " + TaskStatus.IN_PROGRESS;
        assert taskManager.getEpic(epicId).getStatus() == TaskStatus.IN_PROGRESS :
                "Статус эпика должен быть " + TaskStatus.IN_PROGRESS;

        subTask1.setStatus(TaskStatus.DONE);
        taskManager.updateTask(subTask1);
        assert taskManager.getSubTask(subTask1Id).getStatus() == TaskStatus.DONE :
                "Статус подзадачи должен быть " + TaskStatus.DONE;
        assert taskManager.getEpic(epicId).getStatus() == TaskStatus.IN_PROGRESS :
                "Статус эпика должен быть " + TaskStatus.IN_PROGRESS;

        subTask2.setStatus(TaskStatus.DONE);
        taskManager.updateTask(subTask2);
        assert taskManager.getSubTask(subTask2Id).getStatus() == TaskStatus.DONE :
                "Статус подзадачи должен быть " + TaskStatus.DONE;
        assert taskManager.getEpic(epicId).getStatus() == TaskStatus.DONE :
                "Статус эпика должен быть " + TaskStatus.DONE;

        SubTask subTask3 = new SubTask("Купить попкорн", "Забыли. А голодными смотреть нельзя", TaskStatus.NEW,
                epicId);
        Integer subTask3Id = taskManager.createTask(subTask3);
        assert taskManager.getEpic(epicId).getStatus() == TaskStatus.IN_PROGRESS :
                "Статус эпика должен быть " + TaskStatus.IN_PROGRESS;

        subTask3.setStatus(TaskStatus.DONE);
        taskManager.updateTask(subTask3);
        assert taskManager.getSubTask(subTask3Id).getStatus() == TaskStatus.DONE :
                "Статус подзадачи должен быть " + TaskStatus.DONE;
        assert taskManager.getEpic(epicId).getStatus() == TaskStatus.DONE :
                "Статус эпика должен быть " + TaskStatus.DONE;

        assert taskManager.getSubTasksByEpicId(epicId).size() == 3 : "Эпик должен иметь три подзадачи";
        taskManager.removeEpic(epicId);
        assert taskManager.getEpic(epicId) == null : "Эпик должен быть удалён";
        assert taskManager.getSubTasksByEpicId(epicId).isEmpty() : "После удаления эпика, удаляются и подзадачи";

        System.out.println("Test 'createUpdateDeleteEpic_happyPath_noError()' passed.");
    }

    private static void clearTasks_happyPath_noError() {
        TaskManager taskManager = new TaskManager();

        Task task1 = new Task("Считать овец", "По другому не уснуть", TaskStatus.NEW);
        Task task2 = new Task("Бродить по комнате 1 час", "По другому не уснуть", TaskStatus.NEW);
        Task task3 = new Task("Посетить душ", "По другому не уснуть", TaskStatus.NEW);
        taskManager.createTask(task1);
        taskManager.createTask(task2);
        taskManager.createTask(task3);
        assert taskManager.getAllTasks().size() == 3 : "Список задач должен содержать три задачи";

        taskManager.clearTasks();
        assert taskManager.getAllTasks().isEmpty() : "После удаления всех задач, список должен быть пуст";

        System.out.println("Test 'clearTasks_happyPath_noError()' passed.");
    }

    private static void clearEpics_happyPath_epicsAndSubTasksRemoved() {
        TaskManager taskManager = new TaskManager();

        Epic epic1 = new Epic("Выучить английский", "Пригодиться", TaskStatus.NEW);
        Epic epic2 = new Epic("Научиться рисовать", "Тоже пригодиться", TaskStatus.NEW);
        Integer epic1Id = taskManager.createTask(epic1);
        Integer epic2Id = taskManager.createTask(epic2);
        assert taskManager.getAllEpics().size() == 2 : "Список эпиков должен содержать два эпика";

        SubTask subTask1 = new SubTask("Прочитать Гарри Поттера на английском", "Потом и посмотрим на английском", TaskStatus.NEW, epic1Id);
        SubTask subTask2 = new SubTask("Посмотреть Гарри Поттера на английском", "Гарри, Гарри, Гарри", TaskStatus.NEW, epic1Id);
        SubTask subTask3 = new SubTask("Купить акварель", "А можно и угольком порисовать", TaskStatus.NEW,
                epic2Id);
        taskManager.createTask(subTask1);
        taskManager.createTask(subTask2);
        taskManager.createTask(subTask3);
        assert taskManager.getAllSubTasks().size() == 3 : "Список подзадач должен содержать три подзадачи";

        taskManager.clearEpics();
        assert taskManager.getAllEpics().isEmpty() : "После удаления всех эпиков, список должен быть пуст";
        assert taskManager.getAllSubTasks().isEmpty() : "После удаления всех эпиков, также должны удалиться и подзадачи";

        System.out.println("Test 'clearEpics_happyPath_epicsAndSubTasksRemoved' passed.");
    }

    private static void removeSubTask_happyPath_subTasksRemovedAndEpicHasNoSubTaskId() {
        TaskManager taskManager = new TaskManager();

        Epic epic = new Epic("Выучить английский", "Пригодиться", TaskStatus.NEW);
        Integer epicId = taskManager.createTask(epic);

        SubTask subTask1 = new SubTask("Прочитать Гарри Поттера на английском", "Потом и посмотрим на английском", TaskStatus.NEW, epicId);
        SubTask subTask2 = new SubTask("Посмотреть Гарри Поттера на английском", "Гарри, Гарри, Гарри", TaskStatus.NEW, epicId);
        taskManager.createTask(subTask1);
        taskManager.createTask(subTask2);
        assert taskManager.getEpic(epicId).getChildren().size() == 2 : "У эпика две подзадачи";

        SubTask subTask3 = new SubTask("Посмотреть Друзей на английском", "Джо, Джо, Джо", TaskStatus.NEW, epicId);
        taskManager.createTask(subTask3);
        assert taskManager.getEpic(epicId).getChildren().size() == 3 : "У эпика три подзадачи";

        taskManager.removeSubTask(subTask3.getId());
        taskManager.removeSubTask(subTask2.getId());
        assert taskManager.getEpic(epicId).getChildren().size() == 1 : "У эпика осталась одна подзадача";

        taskManager.clearSubTasks();
        assert taskManager.getEpic(epicId).getChildren().isEmpty() : "У эпика больше нет подзадач";

        System.out.println("Test 'removeSubTask_happyPath_subTasksRemovedAndEpicHasNoSubTaskId()' passed.");
    }
}
