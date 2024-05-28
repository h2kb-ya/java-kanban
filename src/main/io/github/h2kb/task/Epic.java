package io.github.h2kb.task;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {

    private final List<Integer> children = new ArrayList<>();

    public Epic(String name, String description, TaskStatus status) {
        super(name, description, status);
    }

    public List<Integer> getChildren() {
        return children;
    }
}
