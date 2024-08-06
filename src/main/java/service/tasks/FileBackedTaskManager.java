package service.tasks;

import db.file.FileDB;
import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskTypes;
import service.exceptions.ManagerLoadSaveException;

import java.io.*;
import java.util.Set;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private File file;

    public static FileBackedTaskManager loadFromFile(File file) throws ManagerLoadSaveException {
        final FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager();
        final FileDB.DataTransfer dataTransfer;

        try {
            dataTransfer = FileDB.loadFromFile(file);
        } catch (IOException e) {
            throw new ManagerLoadSaveException(e);
        }
        for (Task task : dataTransfer.getTasks()) {
            if (task.getType() == TaskTypes.EPIC) {
                fileBackedTaskManager.epics.put(task.getId(), (Epic) task);
            } else if (task.getType() == TaskTypes.SUBTASK) {
                fileBackedTaskManager.subtasks.put(task.getId(), (Subtask) task);
                fileBackedTaskManager.addToSortedTasks(task);
            } else {
                fileBackedTaskManager.simpleTasks.put(task.getId(), task);
                fileBackedTaskManager.addToSortedTasks(task);
            }
        }
        fileBackedTaskManager.idGen = dataTransfer.getIdGen();
        fileBackedTaskManager.file = file;
        return fileBackedTaskManager;
    }

    public void saveToFile() throws ManagerLoadSaveException {
        final FileDB.DataTransfer dataTransfer = new FileDB.DataTransfer();
        final Set<Task> tasks = dataTransfer.getTasks();

        tasks.addAll(simpleTasks.values());
        tasks.addAll(epics.values());
        tasks.addAll(subtasks.values());
        dataTransfer.setIdGen(idGen);

        try {
            FileDB.writeToFile(dataTransfer, file);
        } catch (IOException e) {
            throw new ManagerLoadSaveException(e);
        }
    }

    public static void main(String[] args) {
        try {
            final File file = File.createTempFile("fileTaskManager", ".tmp");
            final FileBackedTaskManager taskManager = FileBackedTaskManager.loadFromFile(file);

            final Task task1 = new Task("Простая задача 1", "","01.01.24 00:00", 30);
            final Task task2 = new Task("Простая задача 2", "","02.01.24 00:00", 30);

            taskManager.addSimpleTask(task1);
            taskManager.addSimpleTask(task2);

            final Epic epic1 = new Epic(new Task("Эпик1", ""));
            final Epic epic2 = new Epic(new Task("Эпик2", ""));

            taskManager.addEpic(epic1);
            taskManager.addEpic(epic2);

            final int epic1Id = epic1.getId();

            final Subtask subtask1 = new Subtask(epic1Id, new Task("Подзадача 1", "","03.01.24 00:00", 30));
            final Subtask subtask2 = new Subtask(epic1Id, new Task("Подзадача 2", "","04.01.24 00:00", 30));
            final Subtask subtask3 = new Subtask(epic1Id, new Task("Подзадача 3", "","05.01.24 00:00", 30));

            taskManager.addSubtask(subtask1);
            taskManager.addSubtask(subtask2);
            taskManager.addSubtask(subtask3);

            final FileBackedTaskManager taskManager2 = FileBackedTaskManager.loadFromFile(file);

            System.out.println(taskManager2.getEpic(epic1Id).getSubtasksId());

            System.out.println("Задачи");
            System.out.println(taskManager2.getSimpleTaskList());

            System.out.println("Эпики");
            System.out.println(taskManager2.getEpicList());

            System.out.println("Подзадачи");
            System.out.println(taskManager2.getSubtaskList());
            file.delete();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
