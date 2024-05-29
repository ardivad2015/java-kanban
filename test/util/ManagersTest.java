package util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {

    @Test
    void getDefaultTaskManager() {
        assertNotNull(Managers.getDefaultTaskManager(),"Вместо Менеджера задач возвращает Null");
    }

    @Test
    void getDefaultHistory() {
        assertNotNull(Managers.getDefaultHistory(),"Вместо mенеджера истории возвращает Null");
    }
}