package io.github.h2kb.task;

public class SubTask extends Task {

    private final Integer parentId;

    public SubTask(String name, String description, TaskStatus status, Integer parentId) {
        super(name, description, status);
        this.parentId = parentId;
    }

    public Integer getParentId() {
        return parentId;
    }
}
