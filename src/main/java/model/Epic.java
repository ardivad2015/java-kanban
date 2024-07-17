package model;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Epic extends Task {

    private final List<Integer> subtasksId = new ArrayList<>();
    private LocalDateTime endTime;

    public Epic(Task task) {
        super(task);
        this.setType(TaskTypes.EPIC);
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public void setEndTime(String endTime) {
        try {
            this.endTime = LocalDateTime.parse(endTime,DATE_TIME_FORMATTER);
        } catch (DateTimeException e) {
            System.out.println(e.getMessage());
        }
    }

    public List<Integer> getSubtasksId() {
        return subtasksId;
    }

    public Epic copy() {
        Epic epic = new Epic(this);
        epic.subtasksId.addAll(this.getSubtasksId());
        epic.endTime = this.endTime;
        return epic;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    @Override
    public String getFormattedEndTime() {
        if (Objects.isNull(endTime)) {
            return "";
        }
        try {
            return endTime.format(DATE_TIME_FORMATTER);
        } catch (DateTimeException e) {
            return "";
        }
    }
}
