package service;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatuses;

import java.util.*;

public class TaskManager {

    private int idGen = 0;
    private final Map<Integer, Task> simpleTasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();

    public void createSimpleTask(Task task) {
        updateSimpleTask(task);
    }

    public void createEpic(Epic epic) {
        if (Objects.isNull(epic)) {
            return;
        }
        epics.put(epic.getId(), epic);
    }

    public void createSubtask(Subtask subtask) {
        if (Objects.isNull(subtask)) {
           return;
        }
        subtasks.put(subtask.getId(),subtask);
        editEpicSubIdsList(EpicSubsActions.ADD,subtask);
    }

    public void updateSimpleTask(Task task) {
        if (!Objects.isNull(task)) {
            simpleTasks.put(task.getId(), task);
        }
    }

    public void updateEpic(Epic epic) {
        if (Objects.isNull(epic)) {
            return;
        }

        final int epicId = epic.getId();
        final ArrayList<Integer> epicSubIds = epic.getSubtasksId();
        final Epic currentEpic = epics.get(epicId);
        final ArrayList<Integer> subsIdToRemove = new ArrayList<>();
        final ArrayList<Integer> subsIdsToAdd = new ArrayList<>();

        epics.put(epicId, epic);
        if (!Objects.isNull(currentEpic)) {
            final ArrayList<Integer> currentEpicSubIds = currentEpic.getSubtasksId();
            subsIdToRemove.addAll(currentEpicSubIds);
            subsIdToRemove.removeAll(epicSubIds);
            subsIdsToAdd.addAll(epicSubIds);
            subsIdsToAdd.removeAll(currentEpicSubIds);
        }
        for (int subsId : subsIdToRemove) {
            subtasks.remove(subsId);
        }
        for (int subsId : subsIdsToAdd) {
            Subtask subtask = subtasks.get(subsId);
            if (Objects.isNull(subtask)) {
                epicSubIds.remove((Integer) subsId);
            }
            if (epicId != subtask.getEpicId()) {
                editEpicSubIdsList(EpicSubsActions.REMOVE, subtask);
                subtask.setEpicId(epicId);
            }
        }
        updateEpicStatus(epic);
    }

    public void updateSubtask(Subtask subtask) {
        if (Objects.isNull(subtask)) {
            return;
        }

        final int subtaskId = subtask.getId();
        final Subtask currentSubtask = subtasks.get(subtaskId);

        subtasks.put(subtaskId,subtask);
        if (!Objects.isNull(currentSubtask)) {
           if (currentSubtask.getEpicId() != subtask.getEpicId()) {
               editEpicSubIdsList(EpicSubsActions.REMOVE,currentSubtask);
           } else {
               Epic epic = epics.get(subtask.getEpicId());
               updateEpicStatus(epic);
           }
        } else {
            editEpicSubIdsList(EpicSubsActions.ADD,subtask);
        }
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
        subtasks.clear();
    }

    public void removeAllSubtasks() {
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.getSubtasksId().clear();
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
        final Epic epic = epics.get(id);

        if (Objects.isNull(epic)) {
            return;
        }

       final ArrayList<Integer> epicSubIds = epic.getSubtasksId();

        if (!epicSubIds.isEmpty()) {
            for (int subId : epicSubIds) {
               subtasks.remove(subId);
           }
        }
        epics.remove(id);
    }

    public void removeSubtask(int id) {
        final Subtask subtask = subtasks.get(id);

        if (Objects.isNull(subtask)) {
            return;
        }
        editEpicSubIdsList(EpicSubsActions.REMOVE,subtask);
        subtasks.remove(id);
    }

    public ArrayList<Subtask> getEpicSubtasks(Epic epic) {
        final ArrayList<Subtask> epicSubtasks = new ArrayList<>();

        if (Objects.isNull(epic)) {
            return epicSubtasks;
        }

        final ArrayList<Integer> epicSubIds = epic.getSubtasksId();

        if (!epicSubIds.isEmpty()) {
            for (int subId : epicSubIds) {

                Subtask subtask = subtasks.get(subId);

               if (!Objects.isNull(subtask)) {
                    epicSubtasks.add(subtask);
                }
            }
        }
        return epicSubtasks;
    }

    private void updateEpicStatus(Epic epic) {
        if (Objects.isNull(epic)) {
            return;
        }

        TaskStatuses newStatus;
        final ArrayList<Subtask> epicSubtasks = getEpicSubtasks(epic);

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

        final ArrayList<Integer> epicSubsId = epic.getSubtasksId();
        boolean isContains = epicSubsId.contains(subId);
        boolean isModified = false;

        if(isContains && action == EpicSubsActions.REMOVE) {
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

    public int nextId() {
        idGen = idGen + 1;
        return idGen;
    }
}
