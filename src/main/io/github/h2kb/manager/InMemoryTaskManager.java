package io.github.h2kb.manager;

import io.github.h2kb.exception.TaskIntersectionOfTimeException;
import io.github.h2kb.task.Epic;
import io.github.h2kb.task.SubTask;
import io.github.h2kb.task.Task;
import io.github.h2kb.task.Status;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {

    private int taskIdCounter = 1;
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, SubTask> subTasks = new HashMap<>();
    private final HistoryManager historyManager = Managers.getDefaultHistory();
    private final Set<Task> tasksSortedByStartTime = new TreeSet<>(Comparator.comparing(Task::getStartTime));

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
                .peek(tasksSortedByStartTime::remove)
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

        epicIds.forEach(epicId -> {
            Epic epic = getEpic(epicId);
            subTaskIds.forEach(epic.getSubTaskIds()::remove);
        });

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
        if (hasOverlappingTasks(task)) {
            throw new TaskIntersectionOfTimeException("The intersection of time was detected");
        }

        task.setId(nextTaskId());
        tasks.put(task.getId(), task);

        if (task.getStartTime() != null) {
            tasksSortedByStartTime.add(task);
        }

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
        if (hasOverlappingTasks(subTask)) {
            throw new TaskIntersectionOfTimeException("The intersection of time was detected");
        }

        Integer epicId = subTask.getEpicId();

        if (epicId == null || !epics.containsKey(epicId)) {
            throw new IllegalArgumentException(String.format("The epic with id %d was not found", epicId));
        }

        subTask.setId(nextTaskId());
        subTasks.put(subTask.getId(), subTask);

        Epic epic = getEpic(epicId);
        epic.getSubTaskIds().add(subTask.getId());
        calculateEpicStatus(epic);

        if (subTask.getStartTime() != null) {
            tasksSortedByStartTime.add(subTask);
        }
        calculateEpicWorkTime(epic);

        return subTask.getId();
    }

    @Override
    public void updateTask(Task task) {
        if (task == null || !tasks.containsKey(task.getId())) {
            return;
        }

        if (task.getStartTime() != null) {
            tasksSortedByStartTime.add(task);
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

        if (subTask.getStartTime() != null) {
            tasksSortedByStartTime.add(subTask);
        }
        calculateEpicWorkTime(epic);
    }

    @Override
    public void removeTask(Integer id) {
        Task task = tasks.get(id);
        tasksSortedByStartTime.remove(task);
        tasks.remove(id);
        historyManager.remove(id);
    }

    @Override
    public void removeEpic(Integer id) {
        Epic epic = getEpic(id);

        List<SubTask> subTasks = getSubTasksByEpicId(epic.getId());
        subTasks.stream()
                .peek(tasksSortedByStartTime::remove)
                .map(SubTask::getId)
                .peek(historyManager::remove)
                .forEach(this::removeSubTask);

        historyManager.remove(id);
        epics.remove(id);
    }

    @Override
    public void removeSubTask(Integer id) {
        Epic epic = getEpic(getSubTask(id).getEpicId());
        epic.getSubTaskIds().remove(id);
        SubTask subTask = subTasks.get(id);

        historyManager.remove(id);
        subTasks.remove(id);
        tasksSortedByStartTime.remove(subTask);
        calculateEpicStatus(epic);
        calculateEpicWorkTime(epic);
    }

    @Override
    public List<SubTask> getSubTasksByEpicId(Integer id) {
        if (id == null || !epics.containsKey(id)) {
            return Collections.emptyList();
        }

        Epic epic = getEpic(id);

        return Optional.ofNullable(epic.getSubTaskIds())
                .orElse(Collections.emptyList())
                .stream()
                .map(this::getSubTask)
                .toList();
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public Set<Task> getPrioritizedTasks() {
        return tasksSortedByStartTime;
    }

    protected boolean isTaskExist(Integer id) {
        return tasks.containsKey(id) || epics.containsKey(id) || subTasks.containsKey(id);
    }

    private boolean allSubTasksHaveStatus(List<SubTask> subTasks, Status status) {
        return subTasks.stream().allMatch(task -> task.getStatus().equals(status));
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

    private void calculateEpicWorkTime(Epic epic) {
        TreeSet<SubTask> subTasks = tasksSortedByStartTime.stream()
                .filter(task -> task instanceof SubTask)
                .map(task -> (SubTask) task)
                .filter(subTask -> subTask.getEpicId().equals(epic.getId()))
                .collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(Task::getStartTime))));

        Duration epicDuration = subTasks.stream()
                .map(SubTask::getDuration)
                .reduce(Duration.ZERO, Duration::plus);

        epic.setDuration(epicDuration);

        if (!subTasks.isEmpty()) {
            epic.setStartTime(subTasks.first().getStartTime());
            epic.setEndTime(subTasks.last().getEndTime());
        }
    }

    private boolean hasOverlappingTasks(Task task) {
        if (task.getStartTime() == null) {
            return false;
        }

        for (Task currentTask : tasksSortedByStartTime) {
            if (currentTask.getEndTime().isAfter(task.getStartTime())) {
                return true;
            }
        }

        return false;
    }
}
