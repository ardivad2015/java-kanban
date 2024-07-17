package service.tasks;

import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    @BeforeEach
    void beforeEach() {
        try {
            File file = File.createTempFile("fileTaskManager", ".tmp");
            taskManager = FileBackedTaskManager.loadFromFile(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ManagerLoadSaveException e) {
            throw new ManagerLoadSaveException(e);
        }
    }

    @Test
    void loadedFromFileTaskManagerIsEqualsOriginalWhenItIsEmpty() {
        try {
            File file = File.createTempFile("fileTaskManager", ".tmp");
            FileBackedTaskManager fileTaskManager = FileBackedTaskManager.loadFromFile(file);
            fileTaskManager.saveToFile();
            FileBackedTaskManager loadedFileTaskManager = FileBackedTaskManager.loadFromFile(file);
            assertEquals(0, loadedFileTaskManager.getSimpleTaskList().size());
            assertEquals(0, loadedFileTaskManager.getEpicList().size());
            assertEquals(0, loadedFileTaskManager.getSubtaskList().size());
            file.delete();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void loadedFromFileTaskManagerIsEqualsOriginalWhenItIsNotEmpty() {
        try {
            File file = File.createTempFile("fileTaskManager", ".tmp");
            FileBackedTaskManager fileTaskManager = FileBackedTaskManager.loadFromFile(file);

            Task task1 = new Task("Простая задача 1","");
            fileTaskManager.addSimpleTask(task1);

            Epic epic1 = new Epic(new Task("Эпик1",""));
            fileTaskManager.addEpic(epic1);

            int epic1Id = epic1.getId();
            Subtask subtask1 = new Subtask(epic1Id,new Task("Подзадача 1",""));
            Subtask subtask2 = new Subtask(epic1Id,new Task("Подзадача 2",""));

            fileTaskManager.addSubtask(subtask1);
            fileTaskManager.addSubtask(subtask2);

            FileBackedTaskManager loadedFileTaskManager = FileBackedTaskManager.loadFromFile(file);
            assertEquals(fileTaskManager.getSimpleTaskList().size(), loadedFileTaskManager.getSimpleTaskList().size());
            assertEquals(fileTaskManager.getEpicList().size(), loadedFileTaskManager.getEpicList().size());
            assertEquals(fileTaskManager.getSubtaskList().size(), loadedFileTaskManager.getSubtaskList().size());
            assertTrue(fileTaskManager.getEpic(epic1Id).getSubtasksId()
                    .containsAll(loadedFileTaskManager.getEpic(epic1Id).getSubtasksId()));
            assertEquals(epic1Id,loadedFileTaskManager.getSubtask(subtask1.getId()).getEpicId());
            file.delete();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void loadFromInvalidFileThenThrowsManagerLoadSaveException() {
        final File file = new File("notexist");
        assertThrows(ManagerLoadSaveException.class,
                () -> FileBackedTaskManager.loadFromFile(file));
    }
}