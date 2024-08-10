package io.github.h2kb.task.dto.mapper;

import io.github.h2kb.task.Epic;
import io.github.h2kb.task.Status;
import io.github.h2kb.task.SubTask;
import io.github.h2kb.task.Task;
import io.github.h2kb.task.TaskType;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.StringJoiner;
import org.junit.platform.commons.util.StringUtils;

public class TaskMapper {

    public static final String FILE_HEADER = "id,type,name,status,description,startTime,duration,epic";

    private TaskMapper() {
    }

    public static String mapTaskToString(Task task) {
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
                .add(epic)
                .add(task.getDuration() == null ? " " : String.valueOf(task.getDuration().toMinutes()))
                .add(task.getStartTime() == null ? " " : task.getStartTime().toString());

        return joiner.toString();
    }

    public static Task mapTaskFromString(String line) {
        String[] taskData = line.split(",");
        TaskType taskType = TaskType.from(taskData[1]);

        switch (taskType) {
            case EPIC -> {
                return new Epic(taskData[2], taskData[4], Status.valueOf(taskData[3]));
            }
            case SUBTASK -> {
                Integer epicId = Integer.valueOf(taskData[5]);
                Duration duration = StringUtils.isBlank(taskData[6]) ? Duration.ZERO :
                        Duration.ofMinutes(Long.parseLong(taskData[6]));
                LocalDateTime startTime = StringUtils.isBlank(taskData[7]) ? null :
                        LocalDateTime.parse(taskData[7], DateTimeFormatter.ISO_DATE_TIME);
                return new SubTask(taskData[2], taskData[4], Status.valueOf(taskData[3]), epicId, duration, startTime);
            }
            case TASK -> {
                Duration duration = StringUtils.isBlank(taskData[6]) ? Duration.ZERO :
                        Duration.ofMinutes(Long.parseLong(taskData[6]));
                LocalDateTime startTime = StringUtils.isBlank(taskData[7]) ? null :
                        LocalDateTime.parse(taskData[7], DateTimeFormatter.ISO_DATE_TIME);
                return new Task(taskData[2], taskData[4], Status.valueOf(taskData[3]), duration, startTime);
            }
            default -> throw new IllegalArgumentException("Invalid task type: " + taskType);
        }
    }

    public static String getHeader() {
        return FILE_HEADER;
    }
}
