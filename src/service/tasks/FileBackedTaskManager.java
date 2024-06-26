package service.tasks;

import data.FileStorage;
import model.Epic;
import model.Subtask;
import model.Task;

import java.io.File;
import java.util.Set;

public class FileBackedTaskManager extends InMemoryTaskManager implements TaskManager {

    private File file;

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager();
        FileStorage.DataTransfer dataTransfer = FileStorage.loadFromFile(file);

        for (Task task : dataTransfer.getTasks()) {
            if (task instanceof Epic) {
                fileBackedTaskManager.epics.put(task.getId(),(Epic) task);
            } else if (task instanceof Subtask) {
                fileBackedTaskManager.subtasks.put(task.getId(),(Subtask) task);
            } else {
                fileBackedTaskManager.simpleTasks.put(task.getId(),task);
            }
        }
        fileBackedTaskManager.idGen = dataTransfer.getIdGen();
        fileBackedTaskManager.file = file;
        return fileBackedTaskManager;
    }

    public void saveToFile() {
        FileStorage.DataTransfer dataTransfer = new FileStorage.DataTransfer();
        Set<Task> tasks = dataTransfer.getTasks();

        tasks.addAll(simpleTasks.values());
        tasks.addAll(epics.values());
        tasks.addAll(subtasks.values());
        dataTransfer.setIdGen(idGen);

        FileStorage.writeToFile(dataTransfer,file);
    }

    public static void main(String[] args) {
        String currentDir = System.getProperty("user.dir");

        File fileTest = new File(currentDir+"\\taskManager.txt");
        FileBackedTaskManager fileBackedTaskManager = FileBackedTaskManager.loadFromFile(fileTest);

        Task task1 = new Task("Простая задача 1","");
        Task task2 = new Task("Простая задача 2","");

        fileBackedTaskManager.addSimpleTask(task1);
        fileBackedTaskManager.addSimpleTask(task2);

        System.out.println("Задачи");
        System.out.println(fileBackedTaskManager.getSimpleTaskList());

        System.out.println("Эпики");
        System.out.println(fileBackedTaskManager.getEpicList());

        System.out.println("Подзадачи");
        System.out.println(fileBackedTaskManager.getSubtaskList());
    }

    @Override
    public void addSimpleTask(Task task) {
        super.addSimpleTask(task);
        saveToFile();
    }

    @Override
    public void addEpic(Epic epic) {
        super.addEpic(epic);
        saveToFile();
    }

    @Override
    public void addSubtask(Subtask subtask) {
        super.addSubtask(subtask);
        saveToFile();
    }

    @Override
    public void updateSimpleTask(Task task) {
        super.updateSimpleTask(task);
        saveToFile();
    }

    @Override
    public void updateEpic(Epic newEpic) {
        super.updateEpic(newEpic);
        saveToFile();
    }

    @Override
    public void updateSubtask(Subtask newSubtask) {
        super.updateSubtask(newSubtask);
        saveToFile();
    }

    @Override
    public void removeAllSimpleTasks() {
        super.removeAllSimpleTasks();
        saveToFile();
    }

    @Override
    public void removeAllEpics() {
        super.removeAllEpics();
        saveToFile();
    }

    @Override
    public void removeAllSubtasks() {
        super.removeAllSubtasks();
        saveToFile();
    }

    @Override
    public void removeSimpleTask(int id) {
        super.removeSimpleTask(id);
        saveToFile();
    }

    @Override
    public void removeEpic(int id) {
        super.removeEpic(id);
        saveToFile();
    }

    @Override
    public void removeSubtask(int id) {
        super.removeSubtask(id);
        saveToFile();
    }
}
