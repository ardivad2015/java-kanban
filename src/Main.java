
import model.*;
import util.Managers;
import service.TaskManager;

public class Main {


    public static void main(String[] args) {

        Managers manager = new Managers();
        TaskManager taskManager = manager.getDefault();

        for (int i = 1; i <= 10; i++) {
        taskManager.addSimpleTask(new Task("Простая задача "+i,""));
        }

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

        System.out.println(taskManager.getEpicSubtasks(epic2));
        printTasks(taskManager);

        subtask2.setStatus(TaskStatuses.DONE);
        taskManager.updateSubtask(subtask2);

        printTasks(taskManager);

        subtask1.setStatus(TaskStatuses.DONE);
        subtask3.setStatus(TaskStatuses.DONE);


        Epic epic2up  = new Epic(new Task("Эпик 2 обновленный",""));
        epic2up.getSubtasksId().add(subtask1.getId());
        epic2up.getSubtasksId().add(subtask2.getId());
        epic2up.getSubtasksId().add(subtask3.getId());
        epic2up.setId(epic2.getId());
        taskManager.updateEpic(epic2up);

        System.out.println(taskManager.getEpicSubtasks(epic2));
        System.out.println(taskManager.getEpicSubtasks(epic1));
        printTasks(taskManager);

        taskManager.getSimpleTaskList();

        taskManager.getSimpleTask(1);

        System.out.println(taskManager.getHistory());

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
