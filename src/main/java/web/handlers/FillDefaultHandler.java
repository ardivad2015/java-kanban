package web.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.Epic;
import model.Subtask;
import model.Task;
import service.tasks.TaskManager;

import java.io.IOException;

public class FillDefaultHandler extends BaseHandler implements HttpHandler  {
    public FillDefaultHandler(TaskManager taskManager) {
        super(taskManager);
    }

    private Endpoint getEndpoint(String requestPath, String requestMethod) {
        final String[] pathParts = requestPath.split("/");

        if (requestMethod.equals("POST") && pathParts.length == 2 && pathParts[1].equals("filldefault")) {
                return Endpoint.FILL_DEFAULT;
        }
        return Endpoint.UNKNOWN;
    }

    private void fillDefault() {
        final Task task1 = new Task("Простая задача 1", "", "01.01.24 00:00", 30);
        final Task task2 = new Task("Простая задача 2", "", "02.01.24 00:00", 30);

        taskManager.addSimpleTask(task1);
        taskManager.addSimpleTask(task2);

        final Epic epic1 = new Epic(new Task("Эпик1", ""));
        final Epic epic2 = new Epic(new Task("Эпик2", ""));

        taskManager.addEpic(epic1);
        taskManager.addEpic(epic2);

        final int epic1Id = epic1.getId();

        final Subtask subtask1 = new Subtask(epic1Id, new Task("Подзадача 1", "", "03.01.24 00:00",
                30));
        final Subtask subtask2 = new Subtask(epic1Id, new Task("Подзадача 2", "", "04.01.24 00:00",
                30));
        final Subtask subtask3 = new Subtask(epic1Id, new Task("Подзадача 3", "", "05.01.24 00:00",
                30));

        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        taskManager.addSubtask(subtask3);
    }

    private void handleFillDefault(HttpExchange exchange) throws IOException {
        fillDefault();
        writeResponse(exchange, "Менеджер заполнен по умолчанию", 200);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        final Endpoint endpoint = getEndpoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());

        try {
            if (endpoint == Endpoint.FILL_DEFAULT) {
                handleFillDefault(exchange);
            } else {
                writeResourceNotFoundResponse(exchange);
            }
        } catch (IOException e) {
            writeResponse(exchange, e.getMessage(), 400);
        }
    }
}
