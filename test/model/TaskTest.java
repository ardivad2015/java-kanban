package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    void tasksIsEqualsByIds() {
        Task task1 = new Task("Task1","");
        Task task2 = new Task("Task2","");

        task1.setId(1);
        task2.setId(1);
        assertEquals(task1,task2,"Задачи с одинаковым id не равны");

    }
}