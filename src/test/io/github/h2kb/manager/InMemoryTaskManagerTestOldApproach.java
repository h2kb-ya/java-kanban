package io.github.h2kb.manager;

import io.github.h2kb.task.Epic;
import io.github.h2kb.task.SubTask;
import io.github.h2kb.task.Task;
import io.github.h2kb.task.Status;

public class InMemoryTaskManagerTestOldApproach {

    public static void main(String[] args) {
        createTaskManager_happyPath_createdTaskManagerWithoutTasks();
        createUpdateDeleteTask_happyPath_noError();
        createUpdateDeleteEpic_happyPath_noError();
        clearTasks_happyPath_noError();
        clearEpics_happyPath_epicsAndSubTasksRemoved();
        removeSubTask_happyPath_subTasksRemovedAndEpicHasNoSubTaskId();
    }

    private static void createTaskManager_happyPath_createdTaskManagerWithoutTasks() {
        InMemoryTaskManager taskManager = new InMemoryTaskManager();

        assert taskManager.getAllTasks().isEmpty() : "Спискок задач должен быть пуст";
        assert taskManager.getAllEpics().isEmpty() : "Список эпиков должен быть пуст";
        assert taskManager.getAllSubTasks().isEmpty() : "Список подзадач должен быть пуст";

        System.out.println("Test 'createTaskManager_happyPath_createdTaskManagerWithoutTasks()' passed.");
    }

    private static void createUpdateDeleteTask_happyPath_noError() {
        InMemoryTaskManager taskManager = new InMemoryTaskManager();

        Task task = new Task("Бить баклуши", "Описание как бить баклуши", Status.NEW);
        Integer taskId = taskManager.createTask(task);
        assert taskId != null : "Id задачи не может быть пустым";
        assert taskManager.getTask(taskId) != null : "Список задач должен содержать задачу с id " + taskId;

        task.setStatus(Status.IN_PROGRESS);
        taskManager.updateTask(task);
        assert taskManager.getTask(taskId).getStatus() == Status.IN_PROGRESS :
                "Статус задачи должен быть " + Status.IN_PROGRESS;

        taskManager.removeTask(taskId);
        assert !taskManager.isTaskExist(taskId) : "Задача с id " + taskId + " должна быть удалена";

        System.out.println("Test 'createUpdateDeleteTask_happyPath_noError()' passed.");
    }

    private static void createUpdateDeleteEpic_happyPath_noError() {
        InMemoryTaskManager taskManager = new InMemoryTaskManager();

        Epic epic = new Epic("Посмотреть Гарри Поттера", "Уже давно хотелось", Status.NEW);
        Integer epicId = taskManager.createTask(epic);

        SubTask subTask1 = new SubTask("Купить телевизор", "Без него Гарри не посмотреть", Status.NEW, epicId);
        SubTask subTask2 = new SubTask("Купить подписку на Кинопоиск", "Хотя можно было бы и спиратить", Status.NEW,
                epicId);
        Integer subTask1Id = taskManager.createTask(subTask1);
        Integer subTask2Id = taskManager.createTask(subTask2);
        assert taskManager.getSubTasksByEpicId(epicId).size() == 2 : "Эпик должен иметь две подзадачи";
        assert taskManager.getSubTasksByEpicId(epicId).stream().
                anyMatch(i -> i.getId().equals(subTask1Id)) : "Эпик должен быть связан с подзадачей 1";
        assert taskManager.getSubTasksByEpicId(epicId).stream().
                anyMatch(i -> i.getId().equals(subTask2Id)) : "Эпик должен быть связан с подзадачей 2";

        subTask1.setStatus(Status.IN_PROGRESS);
        taskManager.updateTask(subTask1);
        assert taskManager.getSubTask(subTask1Id).getStatus() == Status.IN_PROGRESS :
                "Статус подзадачи должен быть " + Status.IN_PROGRESS;
        assert taskManager.getEpic(epicId).getStatus() == Status.IN_PROGRESS :
                "Статус эпика должен быть " + Status.IN_PROGRESS;

        subTask1.setStatus(Status.DONE);
        taskManager.updateTask(subTask1);
        assert taskManager.getSubTask(subTask1Id).getStatus() == Status.DONE :
                "Статус подзадачи должен быть " + Status.DONE;
        assert taskManager.getEpic(epicId).getStatus() == Status.IN_PROGRESS :
                "Статус эпика должен быть " + Status.IN_PROGRESS;

        subTask2.setStatus(Status.DONE);
        taskManager.updateTask(subTask2);
        assert taskManager.getSubTask(subTask2Id).getStatus() == Status.DONE :
                "Статус подзадачи должен быть " + Status.DONE;
        assert taskManager.getEpic(epicId).getStatus() == Status.DONE :
                "Статус эпика должен быть " + Status.DONE;

        SubTask subTask3 = new SubTask("Купить попкорн", "Забыли. А голодными смотреть нельзя", Status.NEW,
                epicId);
        Integer subTask3Id = taskManager.createTask(subTask3);
        assert taskManager.getEpic(epicId).getStatus() == Status.IN_PROGRESS :
                "Статус эпика должен быть " + Status.IN_PROGRESS;

        subTask3.setStatus(Status.DONE);
        taskManager.updateTask(subTask3);
        assert taskManager.getSubTask(subTask3Id).getStatus() == Status.DONE :
                "Статус подзадачи должен быть " + Status.DONE;
        assert taskManager.getEpic(epicId).getStatus() == Status.DONE :
                "Статус эпика должен быть " + Status.DONE;

        assert taskManager.getSubTasksByEpicId(epicId).size() == 3 : "Эпик должен иметь три подзадачи";
        taskManager.removeEpic(epicId);
        assert taskManager.getEpic(epicId) == null : "Эпик должен быть удалён";
        assert taskManager.getSubTasksByEpicId(epicId).isEmpty() : "После удаления эпика, удаляются и подзадачи";

        System.out.println("Test 'createUpdateDeleteEpic_happyPath_noError()' passed.");
    }

    private static void clearTasks_happyPath_noError() {
        InMemoryTaskManager taskManager = new InMemoryTaskManager();

        Task task1 = new Task("Считать овец", "По другому не уснуть", Status.NEW);
        Task task2 = new Task("Бродить по комнате 1 час", "По другому не уснуть", Status.NEW);
        Task task3 = new Task("Посетить душ", "По другому не уснуть", Status.NEW);
        taskManager.createTask(task1);
        taskManager.createTask(task2);
        taskManager.createTask(task3);
        assert taskManager.getAllTasks().size() == 3 : "Список задач должен содержать три задачи";

        taskManager.clearTasks();
        assert taskManager.getAllTasks().isEmpty() : "После удаления всех задач, список должен быть пуст";

        System.out.println("Test 'clearTasks_happyPath_noError()' passed.");
    }

    private static void clearEpics_happyPath_epicsAndSubTasksRemoved() {
        InMemoryTaskManager taskManager = new InMemoryTaskManager();

        Epic epic1 = new Epic("Выучить английский", "Пригодиться", Status.NEW);
        Epic epic2 = new Epic("Научиться рисовать", "Тоже пригодиться", Status.NEW);
        Integer epic1Id = taskManager.createTask(epic1);
        Integer epic2Id = taskManager.createTask(epic2);
        assert taskManager.getAllEpics().size() == 2 : "Список эпиков должен содержать два эпика";

        SubTask subTask1 = new SubTask("Прочитать Гарри Поттера на английском", "Потом и посмотрим на английском", Status.NEW, epic1Id);
        SubTask subTask2 = new SubTask("Посмотреть Гарри Поттера на английском", "Гарри, Гарри, Гарри", Status.NEW, epic1Id);
        SubTask subTask3 = new SubTask("Купить акварель", "А можно и угольком порисовать", Status.NEW,
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
        InMemoryTaskManager taskManager = new InMemoryTaskManager();

        Epic epic = new Epic("Выучить английский", "Пригодиться", Status.NEW);
        Integer epicId = taskManager.createTask(epic);

        SubTask subTask1 = new SubTask("Прочитать Гарри Поттера на английском", "Потом и посмотрим на английском", Status.NEW, epicId);
        SubTask subTask2 = new SubTask("Посмотреть Гарри Поттера на английском", "Гарри, Гарри, Гарри", Status.NEW, epicId);
        taskManager.createTask(subTask1);
        taskManager.createTask(subTask2);
        assert taskManager.getEpic(epicId).getSubTaskIds().size() == 2 : "У эпика две подзадачи";

        SubTask subTask3 = new SubTask("Посмотреть Друзей на английском", "Джо, Джо, Джо", Status.NEW, epicId);
        taskManager.createTask(subTask3);
        assert taskManager.getEpic(epicId).getSubTaskIds().size() == 3 : "У эпика три подзадачи";

        taskManager.removeSubTask(subTask3.getId());
        taskManager.removeSubTask(subTask2.getId());
        assert taskManager.getEpic(epicId).getSubTaskIds().size() == 1 : "У эпика осталась одна подзадача";

        taskManager.clearSubTasks();
        assert taskManager.getEpic(epicId).getSubTaskIds().isEmpty() : "У эпика больше нет подзадач";

        System.out.println("Test 'removeSubTask_happyPath_subTasksRemovedAndEpicHasNoSubTaskId()' passed.");
    }
}
