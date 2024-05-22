package model;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {

    private final List<Integer> subtasksId = new ArrayList<>();

    public Epic(Task task) {
        super(task);
    }

    public List<Integer> getSubtasksId() {
        return subtasksId;
    }
}
