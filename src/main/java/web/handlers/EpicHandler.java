package web.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.Epic;
import service.exceptions.NullTaskInArgument;
import service.exceptions.TaskNotFoundException;
import service.tasks.TaskManager;
import web.extentions.InvalidIdInPath;
import web.extentions.NotAJsonObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class EpicHandler extends BaseHandler implements HttpHandler  {
    public EpicHandler(TaskManager taskManager) {
        super(taskManager);
    }

    private void handleGetEpics(HttpExchange exchange) throws IOException {
        writeResponse(exchange, taskManager.getEpicList(), 200);
    }

    private void handleGetEpicById(HttpExchange exchange) throws IOException {
       final int taskId = getTaskIdFromPath(exchange);
       writeResponse(exchange, taskManager.getEpic(taskId), 200);
    }

    private void handlePostEpic(HttpExchange exchange) throws IOException {
        final String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        final Epic epic = taskFromRequestBody(requestBody,Epic.class);

        if (jsonHasId(requestBody)) {
            taskManager.updateEpic(epic);
            writeResponse(exchange, "Эпик обновлен", 201);
        } else {
            taskManager.addEpic(epic);
            writeResponse(exchange, new AddTaskResponse(epic.getId()), 200);
        }
    }

    private void handleGetEpicSubtasks(HttpExchange exchange) throws IOException {
        final int taskId = getTaskIdFromPath(exchange);
        final Epic epic = taskManager.getEpic(taskId);

        writeResponse(exchange, taskManager.getEpicSubtasks(epic), 200);
    }

    private void handleDeleteEpic(HttpExchange exchange) throws IOException {
        final int taskId = getTaskIdFromPath(exchange);

        taskManager.removeEpic(taskId);
        writeResponse(exchange, "Эпик удален", 200);
    }

    private Endpoint getEndpoint(String requestPath, String requestMethod) {
        final String[] pathParts = requestPath.split("/");

        if (pathParts.length == 2 && pathParts[1].equals("epics")) {
            if (requestMethod.equals("GET")) {
                return Endpoint.GET_EPICS;
            }
            if (requestMethod.equals("POST")) {
                return Endpoint.POST_EPIC;
            }
        }
        if (pathParts.length == 3 && pathParts[1].equals("epics")) {
            if (requestMethod.equals("GET")) {
                return Endpoint.GET_EPIC_BY_ID;
            }
            if (requestMethod.equals("DELETE")) {
                return Endpoint.DELETE_EPIC;
            }
        }
        if (pathParts.length == 4 && pathParts[1].equals("epics") && pathParts[3].equals("subtasks")) {
            if (requestMethod.equals("GET")) {
                return Endpoint.GET_EPIC_SUBTASKS;
            }
        }
        return Endpoint.UNKNOWN;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        final Endpoint endpoint = getEndpoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());
        try {
            switch (endpoint) {
                case GET_EPICS: {
                    handleGetEpics(exchange);
                    break;
                }
                case GET_EPIC_BY_ID: {
                    handleGetEpicById(exchange);
                    break;
                }
                case POST_EPIC: {
                    handlePostEpic(exchange);
                    break;
                }
                case DELETE_EPIC: {
                    handleDeleteEpic(exchange);
                    break;
                }
                case GET_EPIC_SUBTASKS: {
                    handleGetEpicSubtasks(exchange);
                    break;
                }
                default:
                    writeResourceNotFoundResponse(exchange);
            }
        } catch (TaskNotFoundException e) {
            writeResponse(exchange, "Не найден эпик с id " + e.getMessage(), 404);
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
