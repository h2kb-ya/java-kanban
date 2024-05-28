package io.github.h2kb.task;

public class SubTask extends Task {

    private final Integer epicId;

    public SubTask(String name, String description, Status status, Integer epicId) {
        super(name, description, status);
        this.epicId = epicId;
    }

    public Integer getEpicId() {
        return epicId;
    }
}
