package io.github.h2kb.task;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {

    private final List<Integer> subTaskIds = new ArrayList<>();

    public Epic(String name, String description, Status status) {
        super(name, description, status);
    }

    public List<Integer> getSubTaskIds() {
        return subTaskIds;
    }
}
