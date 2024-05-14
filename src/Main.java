import service.TaskManager;
import model.*;

public class Main {

    public static TaskManager taskManager = new TaskManager();

    public static void main(String[] args) {



        Task simpleTask1 = taskManager.createSimpleTask("Простая задача 1","");

        Epic epic1 =  taskManager.createEpic("Эпик 1","");
        Subtask subtask1 = taskManager.createSubtask(epic1,"Эпик 1 Подзадача 1","");

        Epic epic2 =  taskManager.createEpic("Эпик 2","");
        Subtask subtask2 = taskManager.createSubtask(epic2,"Эпик 2 Подзадача 1","");
        Subtask subtask3 = taskManager.createSubtask(epic2,"Эпик 2 Подзадача 2","");

        printTasks();

        simpleTask1.setStatus(TaskStatuses.DONE);
        taskManager.updateSimpleTask(simpleTask1);

        subtask1.setStatus(TaskStatuses.DONE);
        taskManager.updateSubtask(subtask1);

        subtask2.setStatus(TaskStatuses.DONE);
        taskManager.updateSubtask(subtask2);

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
