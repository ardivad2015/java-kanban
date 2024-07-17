package model;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Task {

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm");
    private int id;
    private String topic;
    private String body;
    private TaskStatuses status;
    private TaskTypes type;
    private LocalDateTime startTime;
    private Duration duration = Duration.ZERO;

    public void setType(TaskTypes type) {
        this.type = type;
    }

    public TaskTypes getType() {
        return type;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }

    public String getBody() {
        return body;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setStatus(TaskStatuses status) {
        this.status = status;
    }

    public TaskStatuses getStatus() {
        return status;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setStartTime(String startTime) {
        try {
            this.startTime = LocalDateTime.parse(startTime,DATE_TIME_FORMATTER);
        } catch (DateTimeException ignored) {
        }
    }

    public LocalDateTime getStartTime() {
        if (Objects.isNull(startTime)) {
            return null;
        }
        return startTime;
    }

    public  String getFormattedStartTime() {
        if (Objects.isNull(startTime)) {
            return "";
        }
        try {
            return startTime.format(DATE_TIME_FORMATTER);
        } catch (DateTimeException e) {
            return "";
        }
    }

    public LocalDateTime getEndTime() {
        if (Objects.isNull(startTime)) {
            return null;
        }
        return startTime.plusMinutes(duration.toMinutes());
    }

    public  String getFormattedEndTime() {
        LocalDateTime endTime = getEndTime();
        if (Objects.isNull(endTime)) {
            return "";
        }
        try {
            return endTime.format(DATE_TIME_FORMATTER);
        } catch (DateTimeException e) {
            return "";
        }
    }

    public void setDuration(int durationMinutes) {
        this.duration = Duration.ofMinutes(durationMinutes);
    }

    public int getDuration() {
        return (int) (Objects.nonNull(duration) ? duration.toMinutes() : 0);
    }

    public Task(String topic, String body) {
        if (Objects.isNull(topic)) {
            topic = "";
        }
        if (Objects.isNull(body)) {
            body = "";
        }
        this.topic = topic;
        this.body = body;
        this.status = TaskStatuses.NEW;
        this.type = TaskTypes.SiMPLETASK;
    }

    public Task(String topic, String body, String startTime, int durationMinutes) {
        this(topic, body);
        try {
            this.startTime = LocalDateTime.parse(startTime,DATE_TIME_FORMATTER);
        } catch (DateTimeException e) {
            System.out.println(e.getMessage());
        }
        this.duration  = Duration.ofMinutes(durationMinutes);
    }

    public Task(Task task) {
        this.id = task.id;
        this.topic = task.topic;
        this.body = task.body;
        this.status = task.status;
        this.type = task.type;
        this.startTime = task.startTime;
        this.duration = task.duration;
    }

    public Task copy() {
        return new Task(this);
    }

    @Override
    public String toString() {
        return type +
                ": " +
                "id= " +
                id + ((Objects.nonNull(startTime)) ?
                ", start = " +
                        startTime.format(DATE_TIME_FORMATTER) : "") +
                ((Objects.nonNull(duration)) ?
                        ", duration (min)= " + duration.toMinutes() : "") +
                ", topic= " +
                topic +
                ", body= " +
                body +
                ", status= " +
                status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
