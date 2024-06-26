package service;

import model.Task;

import java.io.File;
import java.util.Map;
import java.util.Set;

public class FileBackedTaskManager extends InMemoryTaskManager implements TaskManager {

    private File file;

    public static void printTasks(TaskManager taskManager) {
        System.out.println("Задачи");
        System.out.println(taskManager.getSimpleTaskList());

        System.out.println("Эпики");
        System.out.println(taskManager.getEpicList());

        System.out.println("Подзадачи");
        System.out.println(taskManager.getSubtaskList());
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager();

        fileBackedTaskManager.file = file;
        FileDriver.ManagerData managerData = FileDriver.readManagerDataFromFile(file);

        fillStorage(fileBackedTaskManager.simpleTasks, managerData.getSimpleTasks());
        fillStorage(fileBackedTaskManager.epics, managerData.getEpics());
        fillStorage(fileBackedTaskManager.subtasks, managerData.getSubtasks());
        fileBackedTaskManager.idGen = managerData.getIdGen();

        return fileBackedTaskManager;
    }

    private static <T extends Task> void fillStorage(Map<Integer, T> storage, Set<T> tasks) {
        for (T task : tasks) {
            storage.put(task.getId(),task);
        }
    }

    public static void main(String[] args) {
        File file1 = new File("C:\\Users\\Dima\\Documents\\taskManager1.txt");
        FileBackedTaskManager fileBackedTaskManager = FileBackedTaskManager.loadFromFile(file1);

        Task task1 = new Task("Простая задача 1","");
        Task task2 = new Task("Простая задача 2","");

        fileBackedTaskManager.addSimpleTask(task1);
        fileBackedTaskManager.addSimpleTask(task2);

        printTasks(fileBackedTaskManager);
    }
}
