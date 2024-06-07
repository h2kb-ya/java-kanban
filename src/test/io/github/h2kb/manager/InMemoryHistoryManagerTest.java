package io.github.h2kb.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.h2kb.task.Status;
import io.github.h2kb.task.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InMemoryHistoryManagerTest {
    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = Managers.getDefaultHistory();
    }

    @Test
    void createdHistoryManager_happyPath_managerNotNull() {
        assertNotNull(historyManager);
    }

    @Test
    void createdHistoryManager_hasNoAddedHistory_returnEmptyList() {
        assertTrue(historyManager.getHistory().isEmpty());
    }

    @Test
    void addHistory_tryToAddElevenRecords_returnAddedHistorySizeIsTen() {
        Task firstTask = new Task("First task", "First task description", Status.NEW);
        Task secondTask = new Task("Second task", "Second task description", Status.NEW);

        for (int i = 0; i <= 10; i++) {
            historyManager.add(firstTask);
        }
        historyManager.add(secondTask);

        assertEquals(10, historyManager.getHistory().size());
        assertEquals(secondTask.getName(), historyManager.getHistory().getFirst().getName());
        assertEquals(firstTask.getName(), historyManager.getHistory().get(1).getName());
    }
}