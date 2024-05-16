package model;

import java.util.ArrayList;

public class Epic extends Task {

    private final ArrayList<Integer> subtasksId = new ArrayList<>();

    public Epic(Task task) {
        super(task);
    }

    public ArrayList<Integer> getSubtasksId() {
        return subtasksId;
    }
}
