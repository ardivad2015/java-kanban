import service.TaskManager;
import model.*;

import java.util.ArrayList;

public class Main {

    public static TaskManager taskManager = new TaskManager();

    public static void main(String[] args) {

        taskManager.createSimpleTask(new Task(taskManager.nextId(),"Простая задача 1",""));
        taskManager.createEpic(new Epic(new Task(taskManager.nextId(),"Эпик 1","")));
        taskManager.createSubtask(new Subtask(2,new Task(taskManager.nextId(),"Эпик 1 подзадача 1","")));
        taskManager.createEpic(new Epic(new Task(taskManager.nextId(),"Эпик 2","")));
        taskManager.createSubtask(new Subtask(4,new Task(taskManager.nextId(),"Эпик 2 подзадача 1","")));
        taskManager.createSubtask(new Subtask(4,new Task(taskManager.nextId(),"Эпик 2 подзадача 2","")));

        printTasks();

        Subtask subtask = new Subtask(4,new Task(5,"Эпик 2 подзадача 2",""));
        subtask.setStatus(TaskStatuses.DONE);
        taskManager.updateSubtask(subtask);

        printTasks();

        Epic epic = new Epic(new Task(4,"Эпик 2 обновленный",""));
        ArrayList<Integer> epicSubsId = epic.getSubtasksId();
        epicSubsId.add(5);
        taskManager.updateEpic(epic);

        printTasks();

        taskManager.removeEpic(4);
        printTasks();
    }

    public static void printTasks() {
        System.out.println("Задачи");
        System.out.println(taskManager.getSimpleTaskList());

        System.out.println("Эпики");
        System.out.println(taskManager.getEpicList());

        System.out.println("Подзадачи");
        System.out.println(taskManager.getSubtaskList());
    }
}
