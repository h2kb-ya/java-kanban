package io.github.h2kb.manager;

import io.github.h2kb.task.Epic;
import io.github.h2kb.task.SubTask;
import io.github.h2kb.task.Task;
import java.util.List;

public interface TaskManager {

    List<Task> getAllTasks();

    List<Epic> getAllEpics();

    List<SubTask> getAllSubTasks();

    void clearTasks();

    void clearEpics();

    void clearSubTasks();

    Task getTask(Integer id);

    Epic getEpic(Integer id);

    SubTask getSubTask(Integer id);

    Integer createTask(Task task);

    void updateTask(Task task);

    void removeTask(Integer id);

    void removeEpic(Integer id);

    void removeSubTask(Integer id);

    List<SubTask> getSubTasksByEpicId(Integer id);

    List<Task> getHistory();
}
