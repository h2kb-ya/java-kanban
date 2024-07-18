package io.github.h2kb.task;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum TaskType {
    TASK("Task"),
    EPIC("Epic"),
    SUBTASK("SubTask");

    private final String type;

    private static final Map<String, TaskType> TYPES = Arrays.stream(TaskType.values())
            .collect(Collectors.toMap(
                    TaskType::getType,
                    Function.identity(),
                    (existing, _) -> existing,
                    () -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER)
            ));

    TaskType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static TaskType from(String type) {
        TaskType taskType = TYPES.get(type);

        if (taskType == null) {
            throw new IllegalArgumentException("Unknown task type: " + type);
        }

        return taskType;
    }
}
