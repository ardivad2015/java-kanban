package service.tasks;

import org.junit.jupiter.api.BeforeEach;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @BeforeEach
    void beforeEach() {
        taskManager = new InMemoryTaskManager();
    }
}