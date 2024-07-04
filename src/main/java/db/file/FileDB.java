package db.file;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatuses;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class FileDB {

    public final static String FIELDS_SEPARATOR = ";";
    public final static String INNER_SEPARATOR = ",";
    public final static int IDGEN_LINE_NUMBER = 1;
    public final static int BEGIN_TASK_DATA_LINE_NUMBER = 2;
    public final static Map<String, Integer> FIELDS = new LinkedHashMap<>();

    static {
        FIELDS.put("id", 0);
        FIELDS.put("type", 1);
        FIELDS.put("topic", 2);
        FIELDS.put("status", 3);
        FIELDS.put("body", 4);
        FIELDS.put("epic", 5);
        FIELDS.put("subtasks", 6);
    }

    public static DataTransfer loadFromFile(File file) throws IOException {
        final DataTransfer dataTransfer = new DataTransfer();
        final Set<Task> tasks = dataTransfer.getTasks();

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            int lineCount = 0;

            while (bufferedReader.ready()) {
                String line = bufferedReader.readLine();

                if (lineCount == IDGEN_LINE_NUMBER) {
                    dataTransfer.setIdGen(idGenFromString(line));
                } else if (lineCount >= BEGIN_TASK_DATA_LINE_NUMBER) {
                    tasks.add(taskFromString(line));
                }
                lineCount++;
            }
        } catch (IOException e) {
            throw e;
        }
        return dataTransfer;
    }

    public static void writeToFile(DataTransfer dataTransfer, File file) throws IOException {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            bufferedWriter.write(fileHead() + "\n");
            bufferedWriter.write(Integer.toString(dataTransfer.getIdGen()));

            for (Task task : dataTransfer.getTasks()) {
                bufferedWriter.write("\n" + taskToString(task));
            }
        } catch (IOException e) {
            throw e;
        }
    }

    private static String fileHead() {
        final StringBuilder sb = new StringBuilder();

        for (Map.Entry<String, Integer> field : FIELDS.entrySet()) {
            sb.append(field.getKey());
            sb.append(FIELDS_SEPARATOR);
        }
        return sb.toString();
    }

    private static String taskToString(Task task) {
        final Map<String, String> taskData = getTaskDataFromTask(task);
        final StringBuilder sb = new StringBuilder();

        for (Map.Entry<String, Integer> field : FIELDS.entrySet()) {
            if (!Objects.isNull(taskData.get(field.getKey()))) {
                sb.append(taskData.get(field.getKey()));
            }
            sb.append(FIELDS_SEPARATOR);
        }
        return sb.toString();
    }

    private static Task taskFromString(String fileString) {
        final Map<String, String> taskData = getTaskDataFromFileString(fileString);
        final Task task = new Task(taskData.get("topic"), taskData.get("body"));
        final String taskType = taskData.get("type");

        task.setStatus(TaskStatuses.valueOf(taskData.get("status")));
        task.setId(Integer.parseInt(taskData.get("id")));
        if ("2".equals(taskType)) {
            final Epic epic = new Epic(task);

            if (!taskData.get("subtasks").isBlank()) {
                String[] subsId = taskData.get("subtasks").split(INNER_SEPARATOR);

                for (String subId : subsId) {
                    epic.getSubtasksId().add(Integer.parseInt(subId));
                }
            }
            return epic;
        } else if ("3".equals(taskType)) {
            final int epicId = Integer.parseInt(taskData.get("epic"));

            return new Subtask(epicId, task);
        }
        return task;
    }

    private static int idGenFromString(String fileString) {
        final String[] values = fileString.split(FIELDS_SEPARATOR);

        return Integer.parseInt(values[FIELDS.get("id")]);
    }

    private static Map<String, String> getTaskDataFromFileString(String fileString) {
        final Map<String, String> taskData = new HashMap<>();
        final String[] values = fileString.split(FIELDS_SEPARATOR);

        for (Map.Entry<String, Integer> field : FIELDS.entrySet()) {
            String value;
            if (field.getValue() > values.length - 1) {
                value = "";
            } else {
                value = values[field.getValue()].trim();
            }
            taskData.put(field.getKey(), value);
        }
        return taskData;
    }

    private static Map<String, String> getTaskDataFromTask(Task task) {
        final Map<String, String> taskData = new HashMap<>();
        final String type;

        taskData.put("id", Integer.toString(task.getId()));
        taskData.put("topic", task.getTopic());
        taskData.put("body", task.getBody());
        taskData.put("status", task.getStatus().name());

        if (task instanceof Epic) {
            type = "2";
            final StringBuilder sb = new StringBuilder();

            for (int subId : ((Epic) task).getSubtasksId()) {
                if (!sb.isEmpty()) {
                    sb.append(INNER_SEPARATOR);
                }
                sb.append(subId);
            }
            taskData.put("subtasks", sb.toString());
        } else if (task instanceof Subtask) {
            type = "3";
            taskData.put("epic", Integer.toString(((Subtask) task).getEpicId()));
        } else {
            type = "1";
        }
        taskData.put("type", type);
        return taskData;
    }

    public static class DataTransfer {

        private int idGen = 0;
        private final Set<Task> tasks = new HashSet<>();

        public int getIdGen() {
            return idGen;
        }

        public void setIdGen(int idGen) {
            this.idGen = idGen;
        }

        public Set<Task> getTasks() {
            return tasks;
        }
    }
}

