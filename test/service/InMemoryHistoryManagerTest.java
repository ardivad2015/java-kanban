package service;

import model.Task;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    @Test
    void add() {
        final HistoryManager historyManager = new InMemoryHistoryManager();
        final Task task = new Task("Простая задача ","");

        historyManager.add(task);
        assertEquals(1,historyManager.getHistory().size(),"Неверное количество задач в истории");

    }

    @Test
    void deleteFirstIfOvermax() {
        final HistoryManager historyManager = new InMemoryHistoryManager();
        final Task task1 = new Task("","");
        final Task task11 = new Task("","");
        historyManager.add(task1);
        for (int i = 1; i < 10; i++) {
            historyManager.add(new Task("",""));
        }
        historyManager.add(task11);

        List<Task> history = historyManager.getHistory();

        assertEquals(10, history.size(), "Элементов истори больше 10");
        assertNotSame(task1, history.get(0), "Не удаляется первый элемент");
        assertSame(task11,history.get(9),"Не добавляется задача при переполнении");
    }
}