package web.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.Subtask;
import service.exceptions.IntersectionOfTasksException;
import service.exceptions.NullTaskInArgument;
import service.exceptions.TaskNotFoundException;
import service.tasks.TaskManager;
import web.extentions.InvalidIdInPath;
import web.extentions.NotAJsonObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SubtaskHandler extends BaseHandler implements HttpHandler  {
    public SubtaskHandler(TaskManager taskManager) {
        super(taskManager);
    }

    private Endpoint getEndpoint(String requestPath, String requestMethod) {
        final String[] pathParts = requestPath.split("/");

        if (pathParts.length == 2 && pathParts[1].equals("subtasks")) {
            if (requestMethod.equals("GET")) {
                return Endpoint.GET_SUBTASKS;
            }
            if (requestMethod.equals("POST")) {
                return Endpoint.POST_SUBTASK;
            }
        }
        if (pathParts.length == 3 && pathParts[1].equals("subtasks")) {
            if (requestMethod.equals("GET")) {
                return Endpoint.GET_SUBTASK_BY_ID;
            }
            if (requestMethod.equals("DELETE")) {
                return Endpoint.DELETE_SUBTASK;
            }
        }
        return Endpoint.UNKNOWN;
    }

    private void handleGetSubtasks(HttpExchange exchange) throws IOException {
        writeResponse(exchange, taskManager.getSubtaskList(), 200);
    }

    private void handleGetSubtaskById(HttpExchange exchange) throws IOException {
        final int taskId = getTaskIdFromPath(exchange);

        writeResponse(exchange, taskManager.getSubtask(taskId), 200);
    }

    private void handlePostSubtask(HttpExchange exchange) throws IOException {
        final String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        final Subtask subtask = taskFromRequestBody(requestBody, Subtask.class);

        if (jsonHasId(requestBody)) {
            taskManager.updateSubtask(subtask);
            writeResponse(exchange, "Задача обновлена", 201);
        } else {
            taskManager.addSubtask(subtask);
            writeResponse(exchange, new AddTaskResponse(subtask.getId()), 200);
        }
    }

    private void handleDeleteSubtask(HttpExchange exchange) throws IOException {
       final int taskId = getTaskIdFromPath(exchange);

        taskManager.removeSubtask(taskId);
        writeResponse(exchange, "Подзадача удалена", 200);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        final Endpoint endpoint = getEndpoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());

        try {
            switch (endpoint) {
                case GET_SUBTASKS: {
                    handleGetSubtasks(exchange);
                    break;
                }
                case GET_SUBTASK_BY_ID: {
                    handleGetSubtaskById(exchange);
                    break;
                }
                case POST_SUBTASK: {
                    handlePostSubtask(exchange);
                    break;
                }
                case DELETE_SUBTASK: {
                    handleDeleteSubtask(exchange);
                    break;
                }
                default:
                    writeResourceNotFoundResponse(exchange);
            }
        } catch (TaskNotFoundException e) {
            writeResponse(exchange, "Не найдена подзадача с id " + e.getMessage(), 404);
        } catch (IntersectionOfTasksException e) {
            writeResponse(exchange, e.getMessage(), 406);
        } catch (NullTaskInArgument e) {
            writeNullTaskInArgumentResponse(exchange);
        } catch (NotAJsonObject e) {
            writeNotAJsonObjectResponse(exchange);
        } catch (InvalidIdInPath e) {
            writeInvalidIdInPathResponse(exchange);
        } catch (IOException e) {
            writeResponse(exchange, e.getMessage(), 400);
        }

    }
}
