import service.InMemoryTaskManager;
import model.*;
import service.Managers;
import service.TaskManager;

import java.util.ArrayList;
import java.util.List;

public class Main {


    public static void main(String[] args) {

        Managers manager = new Managers();
        TaskManager taskManager = manager.getDefault();

        Task simpleTask1 = new Task("Простая задача 1","");
        taskManager.addSimpleTask(simpleTask1);

        Epic epic1  = new Epic(new Task("Эпик 1",""));
        taskManager.addEpic(epic1);

        Subtask subtask1 = new Subtask(epic1.getId(),new Task("Эпик 1 подзадача 1",""));
        taskManager.addSubtask(subtask1);

        Epic epic2  = new Epic(new Task("Эпик 2",""));
        taskManager.addEpic(epic2);

        Subtask subtask2 = new Subtask(epic2.getId(),new Task("Эпик 2 подзадача 1",""));
        taskManager.addSubtask(subtask2);
        Subtask subtask3 = new Subtask(epic2.getId(),new Task("Эпик 2 подзадача 2",""));
        taskManager.addSubtask(subtask3);

        System.out.println(taskManager.getEpicSubtasks(taskManager.getEpic(epic2.getId())));
        printTasks(taskManager);

        subtask2.setStatus(TaskStatuses.DONE);
        taskManager.updateSubtask(subtask2);

        printTasks(taskManager);

        List<Integer> epicSubsId = epic2.getSubtasksId();
        epicSubsId.add(subtask1.getId());
        epicSubsId.add(subtask2.getId());
        epicSubsId.add(subtask3.getId());
        taskManager.updateEpic(epic2);

        System.out.println(taskManager.getEpicSubtasks(taskManager.getEpic(epic2.getId())));
        System.out.println(taskManager.getEpicSubtasks(taskManager.getEpic(epic1.getId())));
        printTasks(taskManager);


    }

    public static void printTasks(TaskManager taskManager) {
        System.out.println("Задачи");
        System.out.println(taskManager.getSimpleTaskList());

        System.out.println("Эпики");
        System.out.println(taskManager.getEpicList());

        System.out.println("Подзадачи");
        System.out.println(taskManager.getSubtaskList());
    }
}
