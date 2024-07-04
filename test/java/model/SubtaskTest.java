package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {

    @Test
    void subtasksIsEqualsByIds() {
        Subtask sub1 = new Subtask(1,new Task("sub1",""));
        Subtask sub2 = new Subtask(1,new Task("sub2",""));

        sub1.setId(1);
        sub2.setId(1);
        assertEquals(sub1,sub2,"Подзадачи с одинаковым id не равны");
    }
}