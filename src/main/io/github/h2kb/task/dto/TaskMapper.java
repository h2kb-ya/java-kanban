package io.github.h2kb.task.dto;

import io.github.h2kb.task.Epic;
import io.github.h2kb.task.Status;
import io.github.h2kb.task.SubTask;
import io.github.h2kb.task.Task;
import io.github.h2kb.task.TaskType;
import java.util.StringJoiner;

public class TaskMapper {

    private TaskMapper() {
    }

    public static String toString(Task task) {
        StringJoiner joiner = new StringJoiner(",");

        TaskType taskType = TaskType.from(task.getClass().getSimpleName());
        String epic = "";

        if (taskType.equals(TaskType.SUBTASK)) {
            epic = ((SubTask) task).getEpicId().toString();
        }

        joiner.add(task.getId().toString())
                .add(taskType.toString())
                .add(task.getName())
                .add(task.getStatus().toString())
                .add(task.getDescription())
                .add(epic);

        return joiner.toString();
    }

    public static Task fromString(String line) {
        String[] taskData = line.split(",");
        TaskType taskType = TaskType.from(taskData[1]);

        switch (taskType) {
            case EPIC -> {
                return new Epic(taskData[2], taskData[4], Status.valueOf(taskData[3]));
            }
            case SUBTASK -> {
                return new SubTask(taskData[2], taskData[4], Status.valueOf(taskData[3]), Integer.valueOf(taskData[5]));
            }
            case TASK -> {
                return new Task(taskData[2], taskData[4], Status.valueOf(taskData[3]));
            }
            default -> throw new IllegalArgumentException("Invalid task type: " + taskType);
        }
    }

    public static String getHeader() {
        return "id,type,name,status,description,epic";
    }
}
