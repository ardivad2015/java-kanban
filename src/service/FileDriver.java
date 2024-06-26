package service;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatuses;

import java.io.*;
import java.util.*;

public class FileDriver {

    public final static String FIELDS_SEPARATOR = ";";
    public final static String INNER_SEPARATOR = "@@";
    public final static int IDGEN_LINE_NUMBER = 1;
    public final static int BEGIN_TASK_DATA_LINE_NUMBER = 2;
    public final static Map<String, Integer> FIELDS = new HashMap<>();

    static {
        FIELDS.put("id", 0);
        FIELDS.put("type", 1);
        FIELDS.put("topic", 2);
        FIELDS.put("status", 3);
        FIELDS.put("body", 4);
        FIELDS.put("epic", 5);
        FIELDS.put("subtasks", 6);
    }

    private static Task taskFromString(String fileString) {
        Map<String, String> taskData = getTaskDataFromFileString(fileString);
        Task task = new Task(taskData.get("topic"), taskData.get("body"));
        String taskType = taskData.get("type");

        task.setStatus(TaskStatuses.valueOf(taskData.get("status")));
        task.setId(Integer.parseInt(taskData.get("id")));
        if ("2".equals(taskType)) {
            Epic epic = new Epic(task);

            if (!taskData.get("subtasks").isBlank()) {
                String[] subsId = taskData.get("subtasks").split(INNER_SEPARATOR);

                for (String subId : subsId) {
                    epic.getSubtasksId().add(Integer.parseInt(subId));
                }
            }
            return epic;
        } else if ("3".equals(taskType)) {
            int epicId = Integer.parseInt(taskData.get("epic"));

            return new Subtask(epicId, task);
        }
        return task;
    }

    private static int idGenFromString(String fileString) {
        String[] values = fileString.split(FIELDS_SEPARATOR);

        return Integer.parseInt(values[FIELDS.get("id")]);
    }

    public static ManagerData readManagerDataFromFile(File file) {
        final ManagerData managerData = new ManagerData();

        try (Reader fileReader = new FileReader(file)) {
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            int lineCount = 0;

            while (bufferedReader.ready()) {
                String line = bufferedReader.readLine();

                if (lineCount == IDGEN_LINE_NUMBER) {
                    managerData.idGen = idGenFromString(line);
                } else if (lineCount >= BEGIN_TASK_DATA_LINE_NUMBER) {
                    Task task = taskFromString(line);

                    if (task instanceof Epic) {
                        managerData.epics.add((Epic) task);
                    } else if (task instanceof Subtask) {
                        managerData.subtasks.add((Subtask) task);
                    } else {
                        managerData.simpleTasks.add(task);
                    }
                }
                lineCount++;
            }
        } catch (FileNotFoundException e) {
            System.out.println("Файл не найден");
        } catch (IOException e) {
            System.out.println("Произошла ошибка во время чтения файла.");
        }
        return managerData;
    }

    private static Map<String, String> getTaskDataFromFileString(String fileString) {
        Map<String, String> taskData = new HashMap<>();
        String[] values = fileString.split(FIELDS_SEPARATOR);

        for (Map.Entry<String, Integer> field : FIELDS.entrySet()) {
            String value;
            if (field.getValue() > values.length - 1) {
                value = "";
            } else {
                value = values[field.getValue()];
            }
            taskData.put(field.getKey(), value);
        }
        return taskData;
    }

    static class ManagerData {

        private int idGen = 0;
        private final Set<Task> simpleTasks = new HashSet<>();
        private final Set<Epic> epics = new HashSet<>();
        private final Set<Subtask> subtasks = new HashSet<>();

        public int getIdGen() {
            return idGen;
        }

        public Set<Task> getSimpleTasks() {
            return simpleTasks;
        }

        public Set<Epic> getEpics() {
            return epics;
        }

        public Set<Subtask> getSubtasks() {
            return subtasks;
        }
    }
}

