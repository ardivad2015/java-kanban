package service;

import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.history.HistoryManager;
import service.history.InMemoryHistoryManager;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    private HistoryManager historyManager;

    @BeforeEach
    void beforeEach() {
        historyManager = new InMemoryHistoryManager();
    }

    @Test
    void addUniqueShouldIncreaseHistorySize() {
        final Task task1 = new Task("","");
        final Task task2 = new Task("","");

        task1.setId(1);
        task2.setId(2);

        historyManager.add(task1);
        historyManager.add(task2);

        assertEquals(2,historyManager.getHistory().size(),"Неверное количество задач в истории");
    }

    @Test
    void addDuplicateShouldNotIncreaseHistorySize() {
        final Task task1 = new Task("","");
        final Task task2 = new Task("","");

        task1.setId(1);
        task2.setId(1);

        historyManager.add(task1);
        historyManager.add(task2);

        assertEquals(1,historyManager.getHistory().size(),"Неверное количество задач в истории");
    }

    @Test
    void addDuplicateShouldChangeHistoryOrder() {
        final Task task1 = new Task("","");
        final Task task2 = new Task("","");
        final Task task3 = new Task("","");

        task1.setId(1);
        task2.setId(2);
        task3.setId(1);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        List<Task> expectedHistory = Arrays.asList(task3, task2);
        List<Task> history = historyManager.getHistory();

        assertEquals(expectedHistory, history, "Неверный порядок истории");
    }

    @Test
    void removeShouldChangeHistorySize() {
        final HistoryManager historyManager = new InMemoryHistoryManager();
        final Task task1 = new Task("","");

        task1.setId(1);
        historyManager.add(task1);
        historyManager.remove(1);

        assertEquals(0,historyManager.getHistory().size(), "Неверное количество задач в истории");
    }

    @Test
    void removeShouldChangeHistoryOrder() {
        final HistoryManager historyManager = new InMemoryHistoryManager();
        final Task task1 = new Task("","");
        final Task task2 = new Task("","");
        final Task task3 = new Task("","");

        task1.setId(1);
        task2.setId(2);
        task3.setId(3);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(2);

        List<Task> expectedHistory = Arrays.asList(task3, task1);
        List<Task> history = historyManager.getHistory();

        assertEquals(expectedHistory, history, "Неверный порядок истории");
    }
}