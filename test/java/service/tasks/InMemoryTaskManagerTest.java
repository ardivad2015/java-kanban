package service.tasks;

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
    void beforeEach() {
        taskManager = new InMemoryTaskManager();
    }

    @Test
    void addSimpleTask() {
        final Task task = new Task("Простая задача ","Описание");

        task.setId(100);
        taskManager.addSimpleTask(task);
        assertEquals(1,task.getId(), "Неверный id");
        assertEquals(task,taskManager.getSimpleTask(1),"Получение задачи по id. Задачи не совпадают");

        final List<Task> tasks = taskManager.getSimpleTaskList();

        assertNotNull(tasks, "Задачи не возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(task, tasks.get(0), "Получение списка задач. Задачи не совпадают.");
    }

    @Test
    void changeStateOfTaskShouldNotAffectToTaskinManager() {
        final Task task = new Task("Простая задача ","Описание");

        taskManager.addSimpleTask(task);
        task.setTopic("Новое описание");

        assertNotEquals(task.getTopic(),taskManager.getSimpleTask(task.getId()),"Изменилось состояние задачи в" +
                " менеджере");
    }

    @Test
    void addSubtask() {
        final Epic epic  = new Epic(new Task("Эпик 1",""));

        taskManager.addEpic(epic);

        final Subtask subtask1 = new Subtask(epic.getId(),new Task("Эпик 1 подзадача 1",""));

        taskManager.addSubtask(subtask1);

        final List<Integer> subsIds = taskManager.getEpic(epic.getId()).getSubtasksId();

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

        assertEquals(task3.getTopic(), task2.getTopic(), "Не обновлена задача в хранилище");
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

        assertNull(taskManager.getSubtask(subtask1.getId()),"Не удаляется подзадача");
        assertEquals(epic1Id,taskManager.getSubtask(subtask3.getId()).getEpicId(),"Не обновляется id эпика добавленной подздачаи");
        assertFalse(taskManager.getEpic(epic2Id).getSubtasksId().contains(subtask3.getId()),"Не удаляется id перенесенной подзадачи");
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

        taskManager.addSubtask(subtask1);
        subtask1.setTopic("Подзадача 1 обновленная");
        subtask1.setEpicId(epic2Id);
        taskManager.updateSubtask(subtask1);

        assertEquals(subtask1.getTopic(), taskManager.getSubtask(subtask1.getId()).getTopic(), "Не " +
                "обновляется подздача в хранилище");
        assertFalse(taskManager.getEpic(epic1Id).getSubtasksId().contains(subtask1.getId()),"Не удаляется id из старого экпика");
        assertTrue(taskManager.getEpic(epic2Id).getSubtasksId().contains(subtask1.getId()),"Не добавляется id в новый эпик");
    }

    @Test
    void updateEpicStatus() {
        final Epic epic1  = new Epic(new Task("Эпик 1",""));

        taskManager.addEpic(epic1);

        int epic1Id = epic1.getId();
        final Subtask subtask1 = new Subtask(epic1Id,new Task("Подзадача 1",""));
        final Subtask subtask2 = new Subtask(epic1Id,new Task("Подзадача 2",""));

        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        subtask1.setStatus(TaskStatuses.IN_PROGRESS);
        taskManager.updateSubtask(subtask1);

        assertEquals(TaskStatuses.IN_PROGRESS,taskManager.getEpic(epic1Id).getStatus(),"Не устанавливается " +
                "статус IN_PROGRESS, если подзадача IN_PROGRESS");

        subtask1.setStatus(TaskStatuses.DONE);
        taskManager.updateSubtask(subtask1);

        assertEquals(TaskStatuses.IN_PROGRESS,taskManager.getEpic(epic1Id).getStatus(),"Не устанавливается " +
                "статус IN_PROGRESS, если подзадача DONE");

        subtask2.setStatus(TaskStatuses.DONE);
        taskManager.updateSubtask(subtask2);

        assertEquals(TaskStatuses.DONE,taskManager.getEpic(epic1Id).getStatus(),"Не устанавливается статус DONE");
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
        assertEquals(0,taskManager.getEpic(epic1.getId()).getSubtasksId().size(),"Не удаляются Id из эпиков");
    }

    @Test
    void getTaskShouldIncreaseHistorySize() {
        final Task task = new Task("Задача1","Описание");

        taskManager.addSimpleTask(task);
        taskManager.getSimpleTask(task.getId());

        assertEquals(1,taskManager.getHistory().size(),"Неверное количество задач в истории");
    }

    @Test
    void removeTaskShouldDecreaseHistorySize() {
        final Task task = new Task("Задача1","Описание");

        taskManager.addSimpleTask(task);
        taskManager.getSimpleTask(task.getId());
        taskManager.removeSimpleTask(task.getId());

        assertEquals(0,taskManager.getHistory().size(),"Неверное количество задач в истории");
    }
}