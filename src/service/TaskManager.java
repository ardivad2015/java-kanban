package service;

import model.Epic;
import model.Subtask;
import model.Task;

import java.util.List;

public interface TaskManager {
    void addSimpleTask(Task task);

    void addEpic(Epic epic);

    void addSubtask(Subtask subtask);

    void updateSimpleTask(Task task);

    void updateEpic(Epic epic);

    void updateSubtask(Subtask subtask);

    List<Task> getSimpleTaskList();

    List<Epic> getEpicList();

    List<Subtask> getSubtaskList();

    void removeAllSimpleTasks();

    void removeAllEpics();

    void removeAllSubtasks();

    Task getSimpleTask(int id);

    Epic getEpic(int id);

    Subtask getSubtask(int id);

    void removeSimpleTask(int id);

    void removeEpic(int id);

    void removeSubtask(int id);

    List<Subtask> getEpicSubtasks(Epic epic);

    List<Task> getHistory();
}
