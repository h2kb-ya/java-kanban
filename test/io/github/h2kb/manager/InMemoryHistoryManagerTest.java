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
        for (int i = 0; i <= 10; i++) {
            Task task = new Task("Task " + i, "Description task " + i, Status.NEW);
            task.setId(i);
            historyManager.add(task);
        }

        for (int i = 0; i <= 5; i++) {
            Task task = new Task("Task " + i, "Description task " + i, Status.NEW);
            task.setId(i);
            historyManager.add(task);
        }

        assertEquals(10, historyManager.getHistory().size());
        assertEquals("Task 4", historyManager.getHistory().getLast().getName());
        assertEquals("Task 6", historyManager.getHistory().getFirst().getName());
    }
}