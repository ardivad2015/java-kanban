package service.history;

import model.Task;
import service.history.HistoryManager;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {

    private final Map<Integer, Node> nodes = new HashMap<>();
    private Node lastViewed;

    private void removeNode(Node node) {
        if (Objects.isNull(node)) {
            return;
        }

        Node prev = node.prev;
        Node next = node.next;

        if (!Objects.isNull(prev)) {
            prev.next = next;
        } else {
            lastViewed = next;
        }
        if (!Objects.isNull(next)) {
            next.prev = prev;
        }
    }

    private void addAsLastViewed(Node node) {
        if (Objects.isNull(node)) {
            return;
        }

        node.prev = null;
        node.next = lastViewed;
        if (!Objects.isNull(lastViewed)) {
            lastViewed.prev = node;
        }
        lastViewed = node;
    }

    @Override
    public void add(Task task) {
        if (Objects.isNull(task)) {
            return;
        }

        int taskId = task.getId();
        Node node;

        node = nodes.get(taskId);
        if (Objects.isNull(node)) {
            node = new Node(task);
            addAsLastViewed(node);
            nodes.put(taskId, node);
        } else {
            removeNode(node);
            addAsLastViewed(node);
            node.task = task;
        }
    }

    @Override
    public void remove(int taskId) {
        Node node = nodes.get(taskId);

        if (!Objects.isNull(node)) {
            removeNode(node);
            nodes.remove(taskId);
        }
    }

    @Override
    public List<Task> getHistory() {
        List<Task> historyList = new ArrayList<>();

        if (Objects.isNull(lastViewed)) {
            return historyList;
        }

        Node node = lastViewed;

        historyList.add(lastViewed.task);
        while (Objects.nonNull(node.next)) {
            node = node.next;
            historyList.add(node.task);
        }
        return historyList;
    }

    private static class Node {
        Task task;
        Node prev;
        Node next;

        Node(Task task) {
            this.task = task;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node node = (Node) o;
            return task.equals(node.task);
        }

        @Override
        public int hashCode() {
            return Objects.hash(task);
        }
    }
}
