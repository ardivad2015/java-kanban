package service.tasks;

import model.*;
import service.exceptions.IntersectionOfTasksException;
import service.exceptions.NullTaskInArgument;
import service.exceptions.TaskNotFoundException;
import service.history.HistoryManager;
import util.Managers;

import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    protected int idGen = 0;
    protected final Map<Integer, Task> simpleTasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();
    protected final TreeSet<Task> sortedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime));
    private final HistoryManager historyManager = Managers.getDefaultHistory();

    private void updateEpicDynamicFields(Epic epic) {
        if (Objects.isNull(epic)) {
            return;
        }
        TaskStatuses newStatus;
        final List<Subtask> epicSubtasks = getEpicSubtasks(epic);
        LocalDateTime epicStartTime = epic.getStartTime();
        LocalDateTime epicEndTime = epic.getEndTime();

        if (!epicSubtasks.isEmpty()) {

            boolean allIsDone = true;
            boolean allIsNew = true;

            for (Subtask subtask : epicSubtasks) {
                LocalDateTime subtaskStartTime = subtask.getStartTime();
                LocalDateTime subtaskEndTime = subtask.getEndTime();

                if (Objects.nonNull(subtaskStartTime) && (Objects.isNull(epicStartTime)
                        || subtaskStartTime.isBefore(epicStartTime))) {
                    epicStartTime = subtaskStartTime;
                }
                if (Objects.nonNull(subtaskEndTime) && (Objects.isNull(epicEndTime)
                        || epicEndTime.isBefore(subtaskEndTime))) {
                    epicEndTime = subtaskEndTime;
                }
                if (subtask.getStatus() == TaskStatuses.IN_PROGRESS) {
                    allIsDone = false;
                    allIsNew  = false;
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
            epicStartTime = null;
            epicEndTime = null;
        }
        if (!(epic.getStatus() == newStatus)) {
            epic.setStatus(newStatus);
        }
        epic.setStartTime(epicStartTime);
        epic.setEndTime(epicEndTime);
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
            updateEpicDynamicFields(epic);
        }
    }

    private int setTaskId(Task task) {
        idGen = idGen + 1;
        task.setId(idGen);
        return idGen;
    }

    private Optional<Task> validateTaskInterval(Task task) {
        final TaskTypes taskType = task.getType();
        Optional<Task> conflictTask = Optional.empty();

        if (Objects.nonNull(task.getStartTime()) && task.getType() != TaskTypes.EPIC) {
            TreeSet<Task> sortedTasksCopy = new TreeSet<>(sortedTasks);
            Task currentTask;
            if (taskType == TaskTypes.SIMPLE_TASK) {
                currentTask = simpleTasks.get(task.getId());
            } else {
                currentTask = subtasks.get(task.getId());
            }
            if (Objects.nonNull(currentTask)) {
                sortedTasksCopy.remove(currentTask);
            }
            Task lowerTask = sortedTasksCopy.floor(task);
            Task higherTask = sortedTasksCopy.ceiling(task);

            if (Objects.nonNull(lowerTask) && lowerTask.getEndTime().isAfter(task.getStartTime())) {
                conflictTask = Optional.of(lowerTask);
            }
            if (Objects.nonNull(higherTask) && task.getEndTime().isAfter(higherTask.getStartTime())) {
                conflictTask = Optional.of(higherTask);
            }
        }
        return conflictTask;
    }

    protected void addToSortedTasks(Task task) {
        final TaskTypes taskType = task.getType();
        Task currentTask;
        if (taskType == TaskTypes.SIMPLE_TASK) {
            currentTask = simpleTasks.get(task.getId());
        } else {
            currentTask = subtasks.get(task.getId());
        }
        if (Objects.nonNull(currentTask) && Objects.nonNull(currentTask.getStartTime())) {
            sortedTasks.remove(currentTask);
        }
        if (Objects.nonNull(task.getStartTime())) {
            sortedTasks.add(task);
        }
    }

    private void addTaskStorage(Task task) {
        if (Objects.isNull(task)) {
            throw new NullTaskInArgument();
        }

        final int taskId = setTaskId(task);
        final Task taskCopy = task.copy();
        final Optional<Task> conflictTask = validateTaskInterval(task);

        if (conflictTask.isEmpty()) {
            switch (taskCopy.getType()) {
                case SIMPLE_TASK -> {
                    simpleTasks.put(taskId, taskCopy);
                    addToSortedTasks(taskCopy);
                }
                case EPIC -> epics.put(taskId, (Epic) taskCopy);
                case SUBTASK -> {
                    subtasks.put(taskId, (Subtask) taskCopy);
                    addToSortedTasks(taskCopy);
                }
            }
        } else {
            throw new IntersectionOfTasksException("Интервал выполнения задачи пересекается с задачей " +
                    conflictTask.get());
        }
    }

    private <T extends Task> T getTaskFromStorage(Map<Integer, T> storage, int id) {
        T task = storage.get(id);
        if (!Objects.isNull(task)) {
            T taskCopy = (T) task.copy();
            historyManager.add(taskCopy);
            return taskCopy;
        }
        throw new TaskNotFoundException(String.valueOf(id));
    }

    private <T extends Task> List<T> getTaskListFromStorage(Map<Integer, T> storage) {
        return storage.values().stream().toList();
    }

    @Override
    public void addSimpleTask(Task task) {
        addTaskStorage(task);
    }

    @Override
    public void addEpic(Epic epic) {
        addTaskStorage(epic);
    }

    @Override
    public void addSubtask(Subtask subtask) {
        addTaskStorage(subtask);
        editEpicSubIdsList(EpicSubsActions.ADD,subtask);
    }

    @Override
    public void updateSimpleTask(Task task) {
        if (Objects.isNull(task)) {
            throw new NullTaskInArgument();
        }
        Optional<Task> conflictTask = validateTaskInterval(task);

        if (conflictTask.isPresent()) {
            throw new IntersectionOfTasksException("Интервал выполнения задачи пересекается с задачей " +
                    conflictTask.get());
        }
        final Task taskCopy = task.copy();
        simpleTasks.put(task.getId(), taskCopy);
        addToSortedTasks(taskCopy);
    }

    @Override
    public void updateEpic(Epic newEpic) {
        if (Objects.isNull(newEpic)) {
            throw new NullTaskInArgument();
        }
        final int epicId = newEpic.getId();
        final List<Integer> newEpicSubIds = newEpic.getSubtasksId();
        final Epic currentEpic = epics.get(epicId);
        final List<Integer> subsIdToRemove = new ArrayList<>();
        final List<Integer> subsIdsToAdd = new ArrayList<>();
        final Epic newEpicCopy = newEpic.copy();

        if (newEpic == currentEpic) {
            return;  //Передали тот же объект, обрабатывать не нужно
        }
        epics.put(epicId, newEpicCopy);
        if (!Objects.isNull(currentEpic)) {
            final List<Integer> currentEpicSubIds = currentEpic.getSubtasksId();
            subsIdToRemove.addAll(currentEpicSubIds);
            subsIdToRemove.removeAll(newEpicSubIds);
            subsIdsToAdd.addAll(newEpicSubIds);
            subsIdsToAdd.removeAll(currentEpicSubIds);
        }
        subsIdToRemove.stream()
                .peek(subId -> {
                    Subtask subtask = subtasks.get(subId);
                    if (Objects.nonNull(subtask)) {
                        sortedTasks.remove(subtask);
                    }
                })
                .forEach(subId -> {
                    subtasks.remove(subId);
                    historyManager.remove(subId);
                });
        subsIdsToAdd.forEach(subId -> {
            Subtask subtask = subtasks.get(subId);
            if (Objects.isNull(subtask)) {
                newEpicSubIds.remove((Integer) subId);
            }
            if (epicId != subtask.getEpicId()) {
                editEpicSubIdsList(EpicSubsActions.REMOVE, subtask);
                subtask.setEpicId(epicId);
            }
        });
        updateEpicDynamicFields(newEpic);
    }

    @Override
    public void updateSubtask(Subtask newSubtask) {
        if (Objects.isNull(newSubtask)) {
            throw new NullTaskInArgument();
        }
        Optional<Task> conflictTask = validateTaskInterval(newSubtask);

        if (conflictTask.isPresent()) {
            throw new IntersectionOfTasksException("Интервал выполнения задачи пересекается с задачей " +
                    conflictTask.get());
        }
        final int subtaskId = newSubtask.getId();
        final Subtask currentSubtask = subtasks.get(subtaskId);
        final Subtask newSubtaskCopy = newSubtask.copy();

        subtasks.put(subtaskId,newSubtaskCopy);
        addToSortedTasks(newSubtaskCopy);
        if (!Objects.isNull(currentSubtask)) {
            if (currentSubtask.getEpicId() != newSubtask.getEpicId()) {
                editEpicSubIdsList(EpicSubsActions.REMOVE,currentSubtask);
                editEpicSubIdsList(EpicSubsActions.ADD,newSubtask);
            } else {
                Epic epic = epics.get(newSubtask.getEpicId());
                updateEpicDynamicFields(epic);
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
        simpleTasks.entrySet().stream()
                .peek(entry -> historyManager.remove(entry.getKey()))
                .map(Map.Entry::getValue)
                .filter(Objects::nonNull)
                .forEach(sortedTasks::remove);
        simpleTasks.clear();
    }

    @Override
    public void removeAllEpics() {
        epics.keySet().forEach(historyManager::remove);
        subtasks.entrySet().stream()
                .peek(entry -> historyManager.remove(entry.getKey()))
                .map(Map.Entry::getValue)
                .filter(Objects::nonNull)
                .forEach(sortedTasks::remove);
        epics.clear();
        subtasks.clear();
    }

    @Override
    public void removeAllSubtasks() {
        subtasks.entrySet().stream()
                .peek(entry -> historyManager.remove(entry.getKey()))
                .map(Map.Entry::getValue)
                .filter(Objects::nonNull)
                .forEach(sortedTasks::remove);
        subtasks.clear();
        epics.values().stream()
                .filter(Objects::nonNull)
                .forEach(epic -> {
                    epic.getSubtasksId().clear(); updateEpicDynamicFields(epic);
                });
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
        final Task task = simpleTasks.get(id);

        if (Objects.isNull(task)) {
            throw new TaskNotFoundException(String.valueOf(id));
        }
        simpleTasks.remove(id);
        historyManager.remove(id);
        sortedTasks.remove(task);
    }

    @Override
    public void removeEpic(int id) {
        final Epic epic = epics.get(id);

        if (Objects.isNull(epic)) {
            throw new TaskNotFoundException(String.valueOf(id));
        }
        final List<Integer> epicSubIds = epic.getSubtasksId();

        if (!epicSubIds.isEmpty()) {
            epicSubIds.stream()
                    .peek(subId -> {
                        Task subtask = subtasks.get(subId);
                        if (Objects.nonNull(subtask)) {
                            sortedTasks.remove(subtask);
                        }
                    })
                    .forEach(subId -> {
                        subtasks.remove(subId);
                        historyManager.remove(subId);
                    });
        }
        epics.remove(id);
        historyManager.remove(id);
    }

    @Override
    public void removeSubtask(int id) {
        final Subtask subtask = subtasks.get(id);

        if (Objects.isNull(subtask)) {
            throw new TaskNotFoundException(String.valueOf(id));
        }
        editEpicSubIdsList(EpicSubsActions.REMOVE,subtask);
        subtasks.remove(id);
        historyManager.remove(id);
        sortedTasks.remove(subtask);
    }

    @Override
    public List<Subtask> getEpicSubtasks(Epic epic) {
        final List<Subtask> epicSubtasks = new ArrayList<>();

        if (Objects.isNull(epic)) {
            return epicSubtasks;
        }

        final List<Integer> epicSubIds = epic.getSubtasksId();

        if (!epicSubIds.isEmpty()) {
            return epicSubIds.stream()
                    .map(subtasks::get)
                    .filter(Objects::nonNull)
                    .map(Subtask::copy).toList();
        }
        return epicSubtasks;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(sortedTasks);
    }
}
