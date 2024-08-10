package io.github.h2kb.task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {

    private final List<Integer> subTaskIds = new ArrayList<>();
    private LocalDateTime endTime;

    public Epic(String name, String description, Status status) {
        super(name, description, status);
    }

    public List<Integer> getSubTaskIds() {
        return subTaskIds;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public void setDuration(Duration duration) {
        super.setDuration(duration);
    }
}
