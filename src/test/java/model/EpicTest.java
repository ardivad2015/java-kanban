package model;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {

    private static Epic epic;

    @BeforeAll
    static void beforeAll() {
        epic = new Epic(new Task("Epic1", ""));
        epic.setId(1);
    }
    @Test
    void getSubtasksId() {
        List<Integer> subtasksId = epic.getSubtasksId();
        assertEquals(0, subtasksId.size(), "Неверное количество задач.");
    }

    @Test
    void epicsIsEqualsByIds() {
        Epic epic2 = new Epic(new Task("Epic2",""));

        epic2.setId(1);
        assertEquals(epic,epic2,"Эпики с одинаковым id не равны");
    }
}