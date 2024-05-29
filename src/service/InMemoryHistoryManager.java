package service;

import model.Task;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class InMemoryHistoryManager implements HistoryManager{

    private static final int DEFAULT_CAPACITY = 10;
    private final List<Task> historyList = new LinkedList<>();

    @Override
    public void add(Task task) {
        if (Objects.isNull(task)) {
            return;
        }

        if (historyList.size() == DEFAULT_CAPACITY) {
            historyList.remove(0);
        }
        historyList.add(task);
    }

    @Override
    public List<Task> getHistory() {
        return historyList;
    }
}
