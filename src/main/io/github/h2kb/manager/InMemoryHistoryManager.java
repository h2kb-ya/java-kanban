package io.github.h2kb.manager;

import io.github.h2kb.task.Task;
import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    public static final int HISTORY_SIZE = 10;
    private final List<Task> historyList = new ArrayList<>();

    @Override
    public void add(Task task) {
        if (historyList.size() >= HISTORY_SIZE) {
            historyList.removeFirst();
            historyList.addFirst(task);
        } else {
            historyList.add(task);
        }
    }

    @Override
    public List<Task> getHistory() {
        return historyList;
    }
}
