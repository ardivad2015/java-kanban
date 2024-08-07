package model;

public class Subtask extends Task {

    private int epicId;

    public Subtask(int epicId, Task task) {
        super(task);
        this.epicId = epicId;
        this.setType(TaskTypes.SUBTASK);
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    public Subtask copy() {
        return new Subtask(this.getEpicId(),this);
    }
}
