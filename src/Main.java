
import model.*;
import util.Managers;
import service.TaskManager;

public class Main {

    public static void main(String[] args) {

        TaskManager taskManager = Managers.getDefaultTaskManager();

        Task task1 = new Task("Простая задача 1","");
        Task task2 = new Task("Простая задача 2","");

        taskManager.addSimpleTask(task1);
        taskManager.addSimpleTask(task2);

        Epic epic1 = new Epic(new Task("Эпик1",""));
        Epic epic2 = new Epic(new Task("Эпик2",""));

        taskManager.addEpic(epic1);
        taskManager.addEpic(epic2);

        int epic1Id = epic1.getId();

        Subtask subtask1 = new Subtask(epic1Id,new Task("Подзадача 1",""));
        Subtask subtask2 = new Subtask(epic1Id,new Task("Подзадача 2",""));
        Subtask subtask3 = new Subtask(epic1Id,new Task("Подзадача 3",""));

        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        taskManager.addSubtask(subtask3);

        taskManager.getSimpleTask(task1.getId());
        taskManager.getSubtask(subtask1.getId());
        taskManager.getSubtask(subtask3.getId());
        taskManager.getSubtask(subtask2.getId());
        taskManager.getEpic(epic1Id);
        taskManager.getSimpleTask(task2.getId());
        taskManager.getEpic(epic2.getId());
        printHistory(taskManager);

        taskManager.getSubtask(subtask3.getId());
        taskManager.getEpic(epic1Id);
        taskManager.getSimpleTask(task1.getId());
        printHistory(taskManager);

        taskManager.removeSimpleTask(task2.getId());
        printHistory(taskManager);

        taskManager.removeEpic(epic1Id);
        printHistory(taskManager);
    }

    public static void printHistory(TaskManager taskManager) {
        System.out.println("=====История=====");
        for (Task task : taskManager.getHistory()) {
            System.out.println(task);
        }
        System.out.println("=====Конец=====");
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
