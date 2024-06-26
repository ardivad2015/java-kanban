package service;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatuses;
import util.Managers;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    private int idGen = 0;
    private final Map<Integer, Task> simpleTasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    private final HistoryManager historyManager = Managers.getDefaultHistory();

    @Override
    public void addSimpleTask(Task task) {
        addTaskStorage(simpleTasks,task);
    }

    @Override
    public void addEpic(Epic epic) {
        addTaskStorage(epics,epic);
    }

    @Override
    public void addSubtask(Subtask subtask) {
        addTaskStorage(subtasks,subtask);
        editEpicSubIdsList(EpicSubsActions.ADD,subtask);
    }

    @Override
    public void updateSimpleTask(Task task) {
        if (!Objects.isNull(task)) {
            simpleTasks.put(task.getId(), task);
        }
    }

    @Override
    public void updateEpic(Epic newEpic) {
        if (Objects.isNull(newEpic)) {
            return;
        }

        final int epicId = newEpic.getId();
        final List<Integer> newEpicSubIds = newEpic.getSubtasksId();
        final Epic currentEpic = epics.get(epicId);
        final List<Integer> subsIdToRemove = new ArrayList<>();
        final List<Integer> subsIdsToAdd = new ArrayList<>();

        if (newEpic == currentEpic) {
            return;  //Передали тот же объект, обрабатывать не нужно
        }
        epics.put(epicId, newEpic);
        if (!Objects.isNull(currentEpic)) {
            final List<Integer> currentEpicSubIds = currentEpic.getSubtasksId();
            subsIdToRemove.addAll(currentEpicSubIds);
            subsIdToRemove.removeAll(newEpicSubIds);
            subsIdsToAdd.addAll(newEpicSubIds);
            subsIdsToAdd.removeAll(currentEpicSubIds);
        }
        for (int subsId : subsIdToRemove) {
            subtasks.remove(subsId);
            historyManager.remove(subsId);
        }
        for (int subsId : subsIdsToAdd) {
            Subtask subtask = subtasks.get(subsId);
            if (Objects.isNull(subtask)) {
                newEpicSubIds.remove((Integer) subsId);
            }
            if (epicId != subtask.getEpicId()) {
                editEpicSubIdsList(EpicSubsActions.REMOVE, subtask);
                subtask.setEpicId(epicId);
            }
        }
        updateEpicStatus(newEpic);
    }

    @Override
    public void updateSubtask(Subtask newSubtask) {
        if (Objects.isNull(newSubtask)) {
            return;
        }

        final int subtaskId = newSubtask.getId();
        final Subtask currentSubtask = subtasks.get(subtaskId);

        subtasks.put(subtaskId,newSubtask.copy());
        if (!Objects.isNull(currentSubtask)) {
            if (currentSubtask.getEpicId() != newSubtask.getEpicId()) {
                editEpicSubIdsList(EpicSubsActions.REMOVE,currentSubtask);
                editEpicSubIdsList(EpicSubsActions.ADD,newSubtask);
            } else {
                Epic epic = epics.get(newSubtask.getEpicId());
                updateEpicStatus(epic);
            }
        } else {
            editEpicSubIdsList(EpicSubsActions.ADD,newSubtask);
        }
    }

    @Override
    public List<Task> getSimpleTaskList() {
        return getTaskListFromStorage(simpleTasks);
    }

    @Override
    public List<Epic> getEpicList() {
        return getTaskListFromStorage(epics);
    }

    @Override
    public List<Subtask> getSubtaskList() {
        return getTaskListFromStorage(subtasks);
    }

    @Override
    public void removeAllSimpleTasks() {
        clearHistory(simpleTasks);
        simpleTasks.clear();
    }

    @Override
    public void removeAllEpics() {
        clearHistory(epics);
        clearHistory(subtasks);
        epics.clear();
        subtasks.clear();
    }

    @Override
    public void removeAllSubtasks() {
        clearHistory(subtasks);
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.getSubtasksId().clear();
            updateEpicStatus(epic);
        }
    }

    @Override
    public Task getSimpleTask(int id) {
        return getTaskFromStorage(simpleTasks, id);
    }

    @Override
    public Epic getEpic(int id) {
        return getTaskFromStorage(epics, id);
    }

    @Override
    public Subtask getSubtask(int id) {
        return getTaskFromStorage(subtasks, id);
    }

    @Override
    public void removeSimpleTask(int id) {
        simpleTasks.remove(id);
        historyManager.remove(id);
    }

    @Override
    public void removeEpic(int id) {
        final Epic epic = epics.get(id);

        if (Objects.isNull(epic)) {
            return;
        }

       final List<Integer> epicSubIds = epic.getSubtasksId();

        if (!epicSubIds.isEmpty()) {
            for (int subId : epicSubIds) {
               subtasks.remove(subId);
               historyManager.remove(subId);
           }
        }
        epics.remove(id);
        historyManager.remove(id);
    }

    @Override
    public void removeSubtask(int id) {
        final Subtask subtask = subtasks.get(id);

        if (Objects.isNull(subtask)) {
            return;
        }
        editEpicSubIdsList(EpicSubsActions.REMOVE,subtask);
        subtasks.remove(id);
        historyManager.remove(id);
    }

    @Override
    public List<Subtask> getEpicSubtasks(Epic epic) {
        final List<Subtask> epicSubtasks = new ArrayList<>();

        if (Objects.isNull(epic)) {
            return epicSubtasks;
        }

        final List<Integer> epicSubIds = epic.getSubtasksId();

        if (!epicSubIds.isEmpty()) {
            for (int subId : epicSubIds) {

                Subtask subtask = subtasks.get(subId);

               if (!Objects.isNull(subtask)) {
                    epicSubtasks.add(subtask.copy());
                }
            }
        }
        return epicSubtasks;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    private void updateEpicStatus(Epic epic) {
        if (Objects.isNull(epic)) {
            return;
        }

        TaskStatuses newStatus;
        final List<Subtask> epicSubtasks = getEpicSubtasks(epic);

        if (!epicSubtasks.isEmpty()) {

            boolean allIsDone = true;
            boolean allIsNew = true;

            for (Subtask subtask : epicSubtasks) {
                if (subtask.getStatus() == TaskStatuses.IN_PROGRESS) {
                    allIsDone = false;
                    allIsNew  = false;
                    break;
                }
                if (subtask.getStatus() == TaskStatuses.DONE) {
                    allIsNew = false;
                }
                if (subtask.getStatus() == TaskStatuses.NEW) {
                    allIsDone = false;
                }
            }
            if (allIsDone) {
                newStatus = TaskStatuses.DONE;
            } else if (allIsNew) {
                newStatus = TaskStatuses.NEW;
            } else {
                newStatus = TaskStatuses.IN_PROGRESS;
            }
        } else {
            newStatus = TaskStatuses.NEW;
        }
        if (!(epic.getStatus() == newStatus)) {
            epic.setStatus(newStatus);
        }
    }

    private void editEpicSubIdsList(EpicSubsActions action, Subtask subtask) {
        if (Objects.isNull(subtask)) {
            return;
        }

        final int subId = subtask.getId();
        final Epic epic = epics.get(subtask.getEpicId());

        if (Objects.isNull(epic)) {
            return;
        }

        final List<Integer> epicSubsId = epic.getSubtasksId();
        boolean isContains = epicSubsId.contains(subId);
        boolean isModified = false;

        if (isContains && action == EpicSubsActions.REMOVE) {
            epicSubsId.remove((Integer) subId);
            isModified = true;
        } else if (!isContains && action == EpicSubsActions.ADD) {
            epicSubsId.add(subId);
            isModified = true;
        }
        if (isModified) {
            updateEpicStatus(epic);
        }
    }

    private int setTaskId(Task task) {
        if (Objects.isNull(task)) {
            return -1;
        }
        idGen = idGen + 1;
        task.setId(idGen);
        return idGen;
    }

    private <T extends Task> void addTaskStorage(Map<Integer, T> storage, T task) {
        final int taskId = setTaskId(task);

        if (taskId > 0) {
            storage.put(taskId, (T) task.copy());
        }
    }

    private <T extends Task> T getTaskFromStorage(Map<Integer, T> storage, int id) {
        T task = storage.get(id);
        if (!Objects.isNull(task)) {
            T taskCopy = (T) task.copy();
            historyManager.add(taskCopy);
            return taskCopy;
        }
        return null;
    }

    private <T extends Task> List<T> getTaskListFromStorage(Map<Integer, T> storage) {
       List<T> taskList = new ArrayList<>();
       for (T task : storage.values()) {
           taskList.add((T) task.copy());
       }
       return taskList;
    }

    private <T extends Task> void clearHistory(Map<Integer, T> storage) {
       for (int taskId : storage.keySet()) {
           historyManager.remove(taskId);
       }
    }
}
