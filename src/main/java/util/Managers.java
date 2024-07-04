package util;

import service.history.HistoryManager;
import service.history.InMemoryHistoryManager;
import service.tasks.InMemoryTaskManager;
import service.tasks.TaskManager;

public class Managers {

    public static TaskManager getDefaultTaskManager() {
        return new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
