package model;

import java.util.Objects;

public class Task {

    private final int id;
    private String topic;
    private String body;
    private TaskStatuses status;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public int getId() {
        return id;
    }

    public TaskStatuses getStatus() {
        return status;
    }

    public void setStatus(TaskStatuses status) {
        this.status = status;
    }

    public Task(int id, String topic, String body) {
        if (Objects.isNull(topic)) {
            topic = "";
        }
        if (Objects.isNull(body)) {
            body = "";
        }
        this.id = id;
        this.topic = topic;
        this.body = body;
        this.status = TaskStatuses.NEW;
    }

    public Task(Task task) {
        this.id = task.id;
        this.topic = task.topic;
        this.body = task.body;
        this.status = TaskStatuses.NEW;
    }

    @Override
    public String toString() {
        return "{" +
                "id=" + id +
                ", topic='" + topic + '\'' +
                ", body='" + body + '\'' +
                ", status=" + status +
                '}';
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
