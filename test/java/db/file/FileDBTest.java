package db.file;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class FileDBTest {

    @Test
    void loadFromFileNoErrorReturnDataTransfer() {
        try {
            final File file = File.createTempFile("fileTaskManager", ".tmp");
            assertEquals(FileDB.loadFromFile(file).getClass(), FileDB.DataTransfer.class);
            file.delete();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    void loadFromInvalidFileThrowsIOException() {
        final File file = new File("notexist");
        assertFalse(file.exists());
        assertThrows(IOException.class, () -> FileDB.loadFromFile(file));
    }
}