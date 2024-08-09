package io.github.h2kb.manager;

import io.github.h2kb.exception.ManagerLoadException;
import io.github.h2kb.exception.ManagerSaveException;
import io.github.h2kb.task.Epic;
import io.github.h2kb.task.SubTask;
import io.github.h2kb.task.Task;
import io.github.h2kb.task.TaskType;
import io.github.h2kb.task.dto.mapper.TaskMapper;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {

    public static final int HEADER_LINE_NUMBER = 1;
    private final Path storageFile;

    public FileBackedTaskManager(Path storageFile) {
        this.storageFile = storageFile;
        loadFromFile();
    }

    @Override
    public Integer createTask(Task task) {
        Integer taskId = super.createTask(task);
        save();

        return taskId;
    }

    @Override
    public Integer createEpic(Epic epic) {
        Integer epicId = super.createEpic(epic);
        save();

        return epicId;
    }

    @Override
    public Integer createSubTask(SubTask subTask) {
        Integer subTaskId = super.createSubTask(subTask);
        save();

        return subTaskId;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubTask(SubTask subTask) {
        super.updateSubTask(subTask);
        save();
    }

    @Override
    public void removeTask(Integer id) {
        super.removeTask(id);
        save();
    }

    @Override
    public void removeEpic(Integer id) {
        super.removeEpic(id);
        save();
    }

    @Override
    public void removeSubTask(Integer id) {
        super.removeSubTask(id);
        save();
    }

    private void save() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(storageFile.toFile()))) {
            bw.write(TaskMapper.getHeader());
            bw.newLine();

            List<Task> tasks = getAllTasks();
            for (Task task : tasks) {
                bw.write(TaskMapper.mapTaskToString(task));
                bw.newLine();
            }

            List<Epic> epics = getAllEpics();
            for (Epic epic : epics) {
                bw.write(TaskMapper.mapTaskToString(epic));
                bw.newLine();
            }

            List<SubTask> subTasks = getAllSubTasks();
            for (SubTask subTask : subTasks) {
                bw.write(TaskMapper.mapTaskToString(subTask));
                bw.newLine();
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Error occurred while manager saving.", e);
        }
    }

    private void loadFromFile() {
        try {

            if (!Files.exists(storageFile)) {
                return;
            }

            List<String> lines = Files.readAllLines(storageFile);

            lines.stream()
                    .skip(HEADER_LINE_NUMBER)
                    .map(TaskMapper::mapTaskFromString)
                    .forEach(task -> {
                        switch (getTaskType(task)) {
                            case EPIC -> createEpic((Epic) task);
                            case SUBTASK -> createSubTask((SubTask) task);
                            case TASK -> createTask(task);
                        }
                    });
        } catch (IOException e) {
            throw new ManagerLoadException("Error occurred while manager loading.", e);
        }
    }

    private TaskType getTaskType(Task task) {
        return TaskType.from(task.getClass().getSimpleName());
    }
}
