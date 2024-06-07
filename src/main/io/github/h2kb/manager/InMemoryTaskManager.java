package io.github.h2kb.manager;

import io.github.h2kb.task.Epic;
import io.github.h2kb.task.SubTask;
import io.github.h2kb.task.Task;
import io.github.h2kb.task.Status;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InMemoryTaskManager implements TaskManager {

    private int taskIdCounter = 1;
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, SubTask> subTasks = new HashMap<>();
    private final HistoryManager historyManager = Managers.getDefaultHistory();

    private int nextTaskId() {
        return taskIdCounter++;
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<SubTask> getAllSubTasks() {
        return new ArrayList<>(subTasks.values());
    }

    @Override
    public void clearTasks() {
        tasks.clear();
    }

    @Override
    public void clearEpics() {
        Set<Integer> epicIds = epics.keySet();

        epicIds.stream()
                .flatMap(epicId -> getSubTasksByEpicId(epicId).stream())
                .map(SubTask::getId)
                .forEach(this::removeSubTask);

        epics.clear();
    }

    @Override
    public void clearSubTasks() {
        List<Integer> epicIds = getAllSubTasks().stream()
                .map(SubTask::getEpicId)
                .toList();

        List<Integer> subTaskIds = getAllSubTasks().stream()
                .map(SubTask::getId)
                .toList();

        for (Integer epicId : epicIds) {
            for (Integer subTaskId : subTaskIds) {
                Epic epic = getEpic(epicId);
                epic.getSubTaskIds().remove(subTaskId);
            }
        }

        subTasks.clear();
    }

    @Override
    public Task getTask(Integer id) {
        Task task = tasks.get(id);
        historyManager.add(task);

        return task;
    }

    @Override
    public Epic getEpic(Integer id) {
        Epic epic = epics.get(id);
        historyManager.add(epic);

        return epic;
    }

    @Override
    public SubTask getSubTask(Integer id) {
        SubTask subTask = subTasks.get(id);
        historyManager.add(subTask);

        return subTask;
    }

    @Override
    public Integer createTask(Task task) {
        task.setId(nextTaskId());

        if (task instanceof Epic) {
            epics.put(task.getId(), (Epic) task);
        } else if (task instanceof SubTask subTask) {
            Integer epicId = subTask.getEpicId();

            if (epicId != null && epics.containsKey(epicId)) {
                subTasks.put(task.getId(), subTask);
                getEpic(epicId).getSubTaskIds().add(subTask.getId());
                calculateEpicStatus(epicId);
            }
        } else {
            tasks.put(task.getId(), task);
        }

        return task.getId();
    }

    @Override
    public void updateTask(Task task) {
        if (task == null || !isTaskExist(task.getId())) {
            return;
        }

        if (task instanceof Epic) {
            epics.put(task.getId(), (Epic) task);
        } else if (task instanceof SubTask subTask) {
            Integer parentId = subTask.getEpicId();

            subTasks.put(task.getId(), subTask);
            calculateEpicStatus(parentId);
        } else {
            tasks.put(task.getId(), task);
        }
    }

    @Override
    public void removeTask(Integer id) {
        tasks.remove(id);
    }

    @Override
    public void removeEpic(Integer id) {
        Epic epic = getEpic(id);
        List<Integer> children = new ArrayList<>(epic.getSubTaskIds());

        children.forEach(this::removeSubTask);
        epics.remove(id);
    }

    @Override
    public void removeSubTask(Integer id) {
        Epic epic = getEpic(getSubTask(id).getEpicId());
        epic.getSubTaskIds().remove(id);

        subTasks.remove(id);
    }

    @Override
    public List<SubTask> getSubTasksByEpicId(Integer id) {
        if (id == null || !epics.containsKey(id)) {
            return Collections.emptyList();
        }

        Epic epic = getEpic(id);

        return epic.getSubTaskIds().stream().map(this::getSubTask).toList();
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    protected boolean isTaskExist(Integer id) {
        return tasks.containsKey(id) || epics.containsKey(id) || subTasks.containsKey(id);
    }

    private boolean allSubTasksHaveStatus(List<SubTask> subTasks, Status status) {
        for (SubTask subTask : subTasks) {
            if (subTask.getStatus() != status) {
                return false;
            }
        }

        return true;
    }

    private void calculateEpicStatus(Integer epicId) {
        Epic epic = getEpic(epicId);
        List<Integer> children = epic.getSubTaskIds();

        if (children.isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }

        List<SubTask> subTasks = getSubTasksByEpicId(epicId);

        if (allSubTasksHaveStatus(subTasks, Status.NEW)) {
            epic.setStatus(Status.NEW);
        } else if (allSubTasksHaveStatus(subTasks, Status.DONE)) {
            epic.setStatus(Status.DONE);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }
}
