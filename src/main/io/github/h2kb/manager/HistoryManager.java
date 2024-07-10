package io.github.h2kb.manager;

import io.github.h2kb.task.Task;
import java.util.List;

public interface HistoryManager {

    void add(Task task);

    void remove(int taskId);

    List<Task> getHistory();

}
