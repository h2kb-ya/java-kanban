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

    protected int nextTaskId() {
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
        tasks.keySet().forEach(historyManager::remove);
        tasks.clear();
    }

    @Override
    public void clearEpics() {
        Set<Integer> epicIds = epics.keySet();

        epicIds.stream()
                .flatMap(epicId -> getSubTasksByEpicId(epicId).stream())
                .map(SubTask::getId)
                .peek(historyManager::remove)
                .forEach(this::removeSubTask);

        epics.keySet().forEach(historyManager::remove);
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

        epicIds.forEach(historyManager::remove);
        subTaskIds.forEach(historyManager::remove);
        subTasks.clear();
    }

    @Override
    public Task getTask(Integer id) {
        Task task = tasks.get(id);

        if (task == null) {
            return null;
        }

        historyManager.add(task);

        return task;
    }

    @Override
    public Epic getEpic(Integer id) {
        Epic epic = epics.get(id);

        if (epic == null) {
            return null;
        }

        historyManager.add(epic);

        return epic;
    }

    @Override
    public SubTask getSubTask(Integer id) {
        SubTask subTask = subTasks.get(id);

        if (subTask == null) {
            return null;
        }

        historyManager.add(subTask);

        return subTask;
    }

    @Override
    public Integer createTask(Task task) {
        task.setId(nextTaskId());
        tasks.put(task.getId(), task);

        return task.getId();
    }

    @Override
    public Integer createEpic(Epic epic) {
        epic.setId(nextTaskId());
        epics.put(epic.getId(), epic);

        return epic.getId();
    }

    @Override
    public Integer createSubTask(SubTask subTask) {
        Integer epicId = subTask.getEpicId();

        if (epicId == null || !epics.containsKey(epicId)) {
            return null;
        }

        subTask.setId(nextTaskId());
        subTasks.put(subTask.getId(), subTask);

        Epic epic = getEpic(epicId);
        epic.getSubTaskIds().add(subTask.getId());
        calculateEpicStatus(epic);

        return subTask.getId();
    }

    @Override
    public void updateTask(Task task) {
        if (task == null || !tasks.containsKey(task.getId())) {
            return;
        }

        tasks.put(task.getId(), task);
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epic == null || !epics.containsKey(epic.getId())) {
            return;
        }

        epics.put(epic.getId(), epic);
    }

    @Override
    public void updateSubTask(SubTask subTask) {
        if (subTask == null || !subTasks.containsKey(subTask.getId())) {
            return;
        }

        Epic epic = getEpic(subTask.getEpicId());

        if (epic == null) {
            return;
        }

        subTasks.put(subTask.getId(), subTask);
        calculateEpicStatus(epic);
    }

    @Override
    public void removeTask(Integer id) {
        tasks.remove(id);
        historyManager.remove(id);
    }

    @Override
    public void removeEpic(Integer id) {
        Epic epic = getEpic(id);
        List<Integer> children = new ArrayList<>(epic.getSubTaskIds());

        children.forEach(historyManager::remove);
        children.forEach(this::removeSubTask);
        historyManager.remove(id);
        epics.remove(id);
    }

    @Override
    public void removeSubTask(Integer id) {
        Epic epic = getEpic(getSubTask(id).getEpicId());
        epic.getSubTaskIds().remove(id);

        historyManager.remove(id);
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

    private void calculateEpicStatus(Epic epic) {
        List<Integer> children = epic.getSubTaskIds();

        if (children.isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }

        List<SubTask> subTasks = getSubTasksByEpicId(epic.getId());

        if (allSubTasksHaveStatus(subTasks, Status.NEW)) {
            epic.setStatus(Status.NEW);
        } else if (allSubTasksHaveStatus(subTasks, Status.DONE)) {
            epic.setStatus(Status.DONE);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }
}
