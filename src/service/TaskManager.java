package service;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatuses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TaskManager {

    private int idGen = 0;
    private final Map<Integer, Task> simpleTasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    private final Map<Subtask, Epic> linkSubtaskEpic = new HashMap<>();
    private final Map<Epic, ArrayList<Subtask>> linkEpicSubtasks = new HashMap<>();

    public Task createSimpleTask(String topic, String body) {

        final Task simpleTask = newTask(topic, body);
        simpleTasks.put(simpleTask.getId(),simpleTask);

        return simpleTask;
    }

    public Epic createEpic(String topic, String body) {

        final Epic epic = new Epic(newTask(topic, body));

        epics.put(epic.getId(),epic);
        linkEpicSubtasks.put(epic,new ArrayList<>());

        return epic;
    }

    public Subtask createSubtask(Epic epic,String topic, String body) {

        if (Objects.isNull(epic)) {
            epic = createEpic("Auto epic","Auto epic");
        }

        final Subtask subtask = new Subtask(epic,newTask(topic, body));

        subtasks.put(subtask.getId(),subtask);

        linkTasks(epic,subtask);
        updateEpicStatus(epic);

        return subtask;
    }

    public void updateSimpleTask(Task task) {

        if (Objects.isNull(task)) {
            return;
        }

        Task currentSimpleTask = simpleTasks.get(task.getId());
        if (Objects.isNull(currentSimpleTask)) {
            return;
        }

        if (!(currentSimpleTask == task)) {
            updateTask(currentSimpleTask, task, true);
        }
    }

    public void updateSubtask(Subtask subtask) {

        if (Objects.isNull(subtask)) {
            return;
        }

        Subtask currentSubtask = subtasks.get(subtask.getId());
        if (Objects.isNull(currentSubtask)) {
            return;
        }

        if (!(currentSubtask == subtask)) {
            updateTask(currentSubtask, subtask, true);
        }

        Epic epic = linkSubtaskEpic.get(currentSubtask);
        if (Objects.isNull(epic)) {
            return;
        }

        updateEpicStatus(epic);
    }

    public void updateEpic(Epic epic) {

        if (Objects.isNull(epic)) {
            return;
        }

        Epic currentEpic = epics.get(epic.getId());

        if (currentEpic == null || currentEpic == epic) {
            return;
        }

        updateTask(currentEpic,epic,false);
    }


    public ArrayList<Task> getSimpleTaskList() {

        return new ArrayList<>(simpleTasks.values());
    }

    public ArrayList<Epic> getEpicList() {

        return new ArrayList<>(epics.values());
    }

    public ArrayList<Subtask> getSubtaskList() {

        return new ArrayList<>(subtasks.values());
    }

    public void removeAllSimpleTasks() {
        simpleTasks.clear();
    }

    public void removeAllEpics() {

        epics.clear();
        linkEpicSubtasks.clear();
        linkSubtaskEpic.clear();
        subtasks.clear();
    }

    public void removeAllSubtasks() {

        subtasks.clear();
        linkEpicSubtasks.clear();
        linkSubtaskEpic.clear();

        for (Epic epic : epics.values()) {
            updateEpicStatus(epic);
        }
    }

    public Task getSimpleTask(int id) {
        return simpleTasks.get(id);
    }

    public Epic getEpic(int id) {
        return epics.get(id);
    }

    public Subtask getSubtask(int id) {
        return subtasks.get(id);
    }

    public void removeSimpleTask(int id) {
        simpleTasks.remove(id);
    }

    public void removeEpic(int id) {

        Epic epic = epics.get(id);

        if (Objects.isNull(epic)) {
            return;
        }

        ArrayList<Subtask> epicSubtasks = getEpicSubtasks(epic);

        if (!epicSubtasks.isEmpty()) {

            for (Subtask subtask : epicSubtasks) {

                if (Objects.isNull(subtask)) {
                   continue;
               }

               subtasks.remove(subtask.getId());
               linkSubtaskEpic.remove(subtask);
           }
        }

        linkEpicSubtasks.remove(epic);
        epics.remove(id);
    }

    public void removeSubtask(int id) {

        Subtask subtask = subtasks.get(id);

        if (Objects.isNull(subtask)) {
            return;
        }

        Epic epic = linkSubtaskEpic.get(subtask);

        if (Objects.isNull(epic)) {
            return;
        }

        ArrayList<Subtask> epicSubtasks = getEpicSubtasks(epic);

        if (!(epicSubtasks.isEmpty())) {
            epicSubtasks.remove(subtask);
        }

        linkSubtaskEpic.remove(subtask);
        subtasks.remove(id);

        updateEpicStatus(epic);
    }

    public ArrayList<Subtask> getEpicSubtasks(Epic epic) {

        ArrayList<Subtask> epicSubtasks = linkEpicSubtasks.get(epic);

        if (Objects.isNull(epicSubtasks)) {
            epicSubtasks = new ArrayList<>();
        }
        return  epicSubtasks;
    }

    private Task newTask(String topic, String body) {

       int id = nextId();

       return new Task(id, topic, body);
    }

    private void linkTasks(Epic epic,Subtask subtask) {

        if (Objects.isNull(epic) || Objects.isNull(subtask)) {
            return;
        }

        ArrayList<Subtask> epicSubtasks = getEpicSubtasks(epic);

        if (!epicSubtasks.contains(subtask)) {
            epicSubtasks.add(subtask);
        }

        linkSubtaskEpic.put(subtask,epic);
    }

    private TaskStatuses valueEpicStatus(Epic epic) {

        if (Objects.isNull(epic)) {
            return TaskStatuses.NEW;
        }

        ArrayList<Subtask> epicSubtasks = getEpicSubtasks(epic);

        if (epicSubtasks.isEmpty()) {
            return TaskStatuses.NEW;
        }

        boolean allIsDone = true;
        boolean allIsNew = true;

        for (Subtask subtask : epicSubtasks) {
            if (subtask.getStatus() == TaskStatuses.IN_PROGRESS) {
                return TaskStatuses.IN_PROGRESS;
            }
            if (subtask.getStatus() == TaskStatuses.DONE) {
                allIsNew = false;
            }
            if (subtask.getStatus() == TaskStatuses.NEW) {
                allIsDone = false;
            }
        }
        if (allIsDone) {
            return TaskStatuses.DONE;
        }
        if (allIsNew) {
            return TaskStatuses.NEW;
        }
        return TaskStatuses.IN_PROGRESS;
    }

    private void updateEpicStatus(Epic epic) {

        if (Objects.isNull(epic)) {
            return;
        }

        TaskStatuses newEpicStatus = valueEpicStatus(epic);

        if (!(epic.getStatus() == newEpicStatus)) {
            epic.setStatus(newEpicStatus);
        }
    }

    private void updateTask(Task task,Task newTask,boolean updateStatus) {

        if (Objects.isNull(task) || Objects.isNull(newTask)) {
            return;
        }

        task.setTopic(newTask.getTopic());
        task.setBody(newTask.getBody());

        if (updateStatus) {
            task.setStatus(newTask.getStatus());
        }
    }

    private int nextId() {
        idGen = idGen + 1;
        return idGen;
    }
}
