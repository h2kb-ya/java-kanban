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
        for (int i = 0; i <= 15; i++) {
            Task task = new Task("Task " + i, "Description task " + i, Status.NEW);
            task.setId(i);
            historyManager.add(task);
        }

        assertEquals(10, historyManager.getHistory().size());
        assertEquals("Task 6", historyManager.getHistory().getFirst().getName());
        assertEquals("Task 15", historyManager.getHistory().getLast().getName());
    }

    @Test
    void addHistory_tryToAddTheSameTask_historyContainsOnlyOneInstance() {
        Task task1 = new Task("Task 1", "Description task 1", Status.NEW);
        task1.setId(1);
        Task task2 = new Task("Task 2", "Description task 2", Status.NEW);
        task2.setId(2);
        Task taskDouble = new Task("Double of task", "Double of Task", Status.NEW);
        taskDouble.setId(1);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(taskDouble);

        assertEquals(2, historyManager.getHistory().size());
        assertEquals("Task 2", historyManager.getHistory().getFirst().getName());
        assertEquals("Double of task", historyManager.getHistory().getLast().getName());
    }
}