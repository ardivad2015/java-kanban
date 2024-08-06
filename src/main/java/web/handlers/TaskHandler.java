package web.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.Task;
import service.exceptions.IntersectionOfTasksException;
import service.exceptions.NullTaskInArgument;
import service.exceptions.TaskNotFoundException;
import service.tasks.TaskManager;
import web.extentions.InvalidIdInPath;
import web.extentions.NotAJsonObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TaskHandler extends BaseHandler implements HttpHandler  {
    public TaskHandler(TaskManager taskManager) {
        super(taskManager);
    }

    private Endpoint getEndpoint(String requestPath, String requestMethod) {
        String[] pathParts = requestPath.split("/");

        if (pathParts.length == 2 && pathParts[1].equals("tasks")) {
            if (requestMethod.equals("GET")) {
                return Endpoint.GET_TASKS;
            }
            if (requestMethod.equals("POST")) {
                return Endpoint.POST_TASK;
            }
        }
        if (pathParts.length == 3 && pathParts[1].equals("tasks")) {
            if (requestMethod.equals("GET")) {
                return Endpoint.GET_TASK_BY_ID;
            }
            if (requestMethod.equals("DELETE")) {
                return Endpoint.DELETE_TASK;
            }
        }
        return Endpoint.UNKNOWN;
    }

    private void handleGetTasks(HttpExchange exchange) throws IOException {
        writeResponse(exchange, taskManager.getSimpleTaskList(), 200);
    }

    private void handleGetTaskById(HttpExchange exchange) throws IOException {
        final int taskId = getTaskIdFromPath(exchange);

        writeResponse(exchange, taskManager.getSimpleTask(taskId), 200);
    }

    private void handlePostTask(HttpExchange exchange) throws IOException {
        final String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        final Task task = taskFromRequestBody(requestBody,Task.class);

        if (jsonHasId(requestBody)) {
            taskManager.updateSimpleTask(task);
            writeResponse(exchange, "Задача обновлена", 201);
        } else {
            taskManager.addSimpleTask(task);
            writeResponse(exchange, new AddTaskResponse(task.getId()), 200);
        }
    }

    private void handleDeleteTask(HttpExchange exchange) throws IOException {
       final int taskId = getTaskIdFromPath(exchange);

        taskManager.removeSimpleTask(taskId);
        writeResponse(exchange, "Задача удалена", 200);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        final Endpoint endpoint = getEndpoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());

        try {
            switch (endpoint) {
                case GET_TASKS: {
                    handleGetTasks(exchange);
                    break;
                }
                case GET_TASK_BY_ID: {
                    handleGetTaskById(exchange);
                    break;
                }
                case POST_TASK: {
                    handlePostTask(exchange);
                    break;
                }
                case DELETE_TASK: {
                    handleDeleteTask(exchange);
                    break;
                }
                default:
                    writeResourceNotFoundResponse(exchange);
            }
        } catch (TaskNotFoundException e) {
            writeResponse(exchange, "Не найдена задача с id " + e.getMessage(), 404);
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
