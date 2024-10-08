package io.github.h2kb.manager;

import io.github.h2kb.task.Task;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {

    private static final int MAX_HISTORY_SIZE = 10;
    private Node<Task> first;
    private Node<Task> last;
    private final Map<Integer, Node<Task>> historyList = new HashMap<>();

    @Override
    public void add(Task task) {
        if (historyList.containsKey(task.getId())) {
            remove(task.getId());
        }

        if (historyList.size() >= MAX_HISTORY_SIZE) {
            removeNode(first);
        }

        historyList.put(task.getId(), linkLast(task));
        checkForCycles();
    }

    @Override
    public void remove(int taskId) {
        Node<Task> node = historyList.remove(taskId);

        if (node == null) {
            return;
        }

        removeNode(node);
        checkForCycles();
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    private Node<Task> linkLast(Task task) {
        final Node<Task> l = last;
        final Node<Task> newNode = new Node<>(l, task, null);
        last = newNode;

        if (l == null) {
            first = newNode;
        } else {
            l.next = newNode;
        }

        return newNode;
    }

    private void removeNode(Node<Task> node) {
        if (node == null) {
            return;
        }

        final Node<Task> next = node.next;
        final Node<Task> prev = node.prev;

        if (prev == null) {
            first = next;
        } else {
            prev.next = next;
            node.prev = null;
        }

        if (next == null) {
            last = prev;
        } else {
            next.prev = prev;
            node.next = null;
        }

        node.item = null;
    }

    private List<Task> getTasks() {
        List<Task> tasks = new ArrayList<>();
        Node<Task> node = first;

        if (node == null) {
            return tasks;
        }

        while (node != null) {
            tasks.add(node.item);
            node = node.next;
        }

        return tasks;
    }

    private void checkForCycles() {
        if (first == null) {
            return;
        }

        Node<Task> slow = first;
        Node<Task> fast = first;

        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;

            if (slow == fast) {
                throw new IllegalStateException("Detected a cycle in the linked list.");
            }
        }
    }

    private static class Node<E extends Task> {

        E item;
        Node<E> next;
        Node<E> prev;

        Node(Node<E> prev, E item, Node<E> next) {
            this.item = item;
            this.next = next;
            this.prev = prev;
        }
    }
}
