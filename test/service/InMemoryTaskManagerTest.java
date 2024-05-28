package service;

import model.Epic;
import model.Subtask;
import model.Task;

import model.TaskStatuses;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    private TaskManager taskManager;

    @BeforeEach
    void BeforeEach() {
        taskManager = new InMemoryTaskManager();
    }
    @Test
    void addSimpleTask() {
        final Task task = new Task("Простая задача ","Описание");

        task.setId(100);
        taskManager.addSimpleTask(task);
        assertEquals(1,task.getId(),"Неверный id");
        assertEquals(task,taskManager.getSimpleTask(1),"Получение задачи по id. Задачи не совпадают");

        final List<Task> tasks = taskManager.getSimpleTaskList();

        assertNotNull(tasks, "Задачи не возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(task, tasks.get(0), "Получение списка задач. Задачи не совпадают.");
    }

    @Test
    void addSubtask() {
        final Epic epic  = new Epic(new Task("Эпик 1",""));

        taskManager.addEpic(epic);

        final Subtask subtask1 = new Subtask(epic.getId(),new Task("Эпик 1 подзадача 1",""));

        taskManager.addSubtask(subtask1);

        final List<Integer> subsIds = epic.getSubtasksId();

        assertEquals(epic.getId(),subtask1.getEpicId(),"Неверный id эпика");
        assertEquals(1,subsIds.size(),"Неверное количество подзадач эпика");
        assertTrue(subsIds.contains(subtask1.getId()), "Эпик не содержит id подзадачи");
    }

    @Test
    void updateSimpleTask() {
        final Task task = new Task("Простая задача ","Описание");
        final Task task2 = new Task("Простая задача обновленная","Описание обновленное");

        taskManager.addSimpleTask(task);

        final int taskId = task.getId();

        task2.setId(taskId);
        taskManager.updateSimpleTask(task2);

        final Task task3 = taskManager.getSimpleTask(taskId);

        assertSame(task3, task2, "Не обновлена задача в хранилище"); //должны быть равны именно ссылки
    }

    @Test
    void updateEpic() {
        final Epic epic1  = new Epic(new Task("Эпик 1",""));
        final Epic epic2  = new Epic(new Task("Эпик 2",""));
        final Epic epic1Upd  = new Epic(new Task("Эпик 1 обновленный",""));


        taskManager.addEpic(epic1);
        taskManager.addEpic(epic2);

        int epic1Id = epic1.getId();
        int epic2Id = epic2.getId();
        final Subtask subtask1 = new Subtask(epic1Id,new Task("Подзадача 1",""));
        final Subtask subtask2 = new Subtask(epic1Id,new Task("Подзадача 2",""));
        final Subtask subtask3 = new Subtask(epic2Id,new Task("Подзадача 3",""));

        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        taskManager.addSubtask(subtask3);

        epic1Upd.setId(epic1Id);

        final List<Integer> subsUpdIds = epic1Upd.getSubtasksId();

        subsUpdIds.add(subtask2.getId());
        subsUpdIds.add(subtask3.getId());

        taskManager.updateEpic(epic1Upd);

        assertSame(epic1Upd, taskManager.getEpic(epic1Id), "Не обновляется эпик в хранилище");
        assertNull(taskManager.getSubtask(subtask1.getId()),"Не удаляется подзадача");
        assertEquals(epic1Id,subtask3.getEpicId(),"Не обновляется id эпика добавленной подздачаи");
        assertFalse(epic2.getSubtasksId().contains(subtask3.getId()),"Не удаляется id перенесенной подзадачи");
    }

    @Test
    void updateSubtask() {
        final Epic epic1  = new Epic(new Task("Эпик 1",""));
        final Epic epic2  = new Epic(new Task("Эпик 2",""));

        taskManager.addEpic(epic1);
        taskManager.addEpic(epic2);

        int epic1Id = epic1.getId();
        int epic2Id = epic2.getId();
        final Subtask subtask1 = new Subtask(epic1Id,new Task("Подзадача 1",""));
        final Subtask subtaskUpd = new Subtask(epic2Id,new Task("Подзадача 1 обновленная",""));

        taskManager.addSubtask(subtask1);
        subtaskUpd.setId(subtask1.getId());
        taskManager.updateSubtask(subtaskUpd);

        assertSame(subtaskUpd, taskManager.getSubtask(subtask1.getId()), "Не обновляется подздача в" +
                " хранилище");
        assertFalse(epic1.getSubtasksId().contains(subtask1.getId()),"Не удаляется id из старого экпика");
        assertTrue(epic2.getSubtasksId().contains(subtask1.getId()),"Не добавляется id в новый эпик");
    }

    @Test
    void updateEpicStatus() {
        final Epic epic1  = new Epic(new Task("Эпик 1",""));

        taskManager.addEpic(epic1);

        int epic1Id = epic1.getId();
        final Subtask subtask1 = new Subtask(epic1Id,new Task("Подзадача 1",""));
        final Subtask subtask2 = new Subtask(epic1Id,new Task("Подзадача 2",""));

        taskManager.addSubtask(subtask1);
        subtask2.setStatus(TaskStatuses.IN_PROGRESS);
        taskManager.addSubtask(subtask2);

        assertEquals(TaskStatuses.IN_PROGRESS,epic1.getStatus(),"Не устанавливается статус IN_PROGRESS, если " +
                        "подзадача IN_PROGRESS");

        subtask2.setStatus(TaskStatuses.DONE);
        taskManager.updateSubtask(subtask2);

        assertEquals(TaskStatuses.IN_PROGRESS,epic1.getStatus(),"Не устанавливается статус IN_PROGRESS, если " +
                "подзадача DONE");

        subtask1.setStatus(TaskStatuses.DONE);
        taskManager.updateSubtask(subtask1);

        assertEquals(TaskStatuses.DONE,epic1.getStatus(),"Не устанавливается статус DONE");
    }

    @Test
    void removeAllEpics() {
        final Epic epic1  = new Epic(new Task("Эпик 1",""));
        final Epic epic2  = new Epic(new Task("Эпик 2",""));

        taskManager.addEpic(epic1);
        taskManager.addEpic(epic2);

        final Subtask subtask1 = new Subtask(epic1.getId(),new Task("Подзадача 1",""));
        final Subtask subtask2 = new Subtask(epic2.getId(),new Task("Подзадача 2",""));

        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        taskManager.removeAllEpics();

        assertEquals(0,taskManager.getEpicList().size(),"Не удаляются эпики");
        assertEquals(0,taskManager.getSubtaskList().size(),"Не удаляются подзадачи");
    }

    @Test
    void removeAllSubtasks() {
        final Epic epic1  = new Epic(new Task("Эпик 1",""));
        final Epic epic2  = new Epic(new Task("Эпик 2",""));

        taskManager.addEpic(epic1);
        taskManager.addEpic(epic2);

        final Subtask subtask1 = new Subtask(epic1.getId(),new Task("Подзадача 1",""));
        final Subtask subtask2 = new Subtask(epic2.getId(),new Task("Подзадача 2",""));

        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        taskManager.removeAllSubtasks();

        assertEquals(0,taskManager.getSubtaskList().size(),"Не удаляются подзадачи");
        assertEquals(0,epic1.getSubtasksId().size(),"Не удаляются Id из эпиков");
    }

    @Test
    void tasksStateInHistory() {
        final Task task1 = new Task("Задача1","Описание");
        final Task task1upd = new Task("Задача1 обновленная","Описание обновленное");

        taskManager.addSimpleTask(task1);
        task1upd.setId(task1.getId());

        taskManager.getSimpleTask(1);
        taskManager.updateSimpleTask(task1upd);
        taskManager.getSimpleTask(1);

        final List<Task> history = taskManager.getHistory();

        assertEquals(2,history.size(),"Некорректное количесто задач в истории");
        assertEquals(history.get(0),history.get(1),"Не сохраняются одинаковые задачи");
        assertNotEquals(history.get(0).getTopic(),history.get(1).getTopic(),"Не сохраняется состояние задачи");
    }
}