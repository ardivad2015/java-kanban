package web.handlers;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import model.Task;
import service.tasks.TaskManager;
import web.adapters.DurationTypeAdapter;
import web.adapters.LocalDateTimeTypeAdapter;
import web.extentions.InvalidIdInPath;
import web.extentions.NotAJsonObject;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;

public class BaseHandler {

    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    protected final TaskManager taskManager;

    public BaseHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    public static Gson gson() {
        return new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
                .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
                .create();
    }

    protected void writeResponse(HttpExchange exchange,
                               Object responseObject,
                               int responseCode) throws IOException {
        try (OutputStream os = exchange.getResponseBody()) {
            final String responseString =  gson().toJson(responseObject);
            exchange.sendResponseHeaders(responseCode, 0);
            os.write(responseString.getBytes(DEFAULT_CHARSET));
        }
        exchange.close();
    }

    public int getTaskIdFromPath(HttpExchange exchange) {
        final String[] pathParts = exchange.getRequestURI().getPath().split("/");

        try {
            return Integer.parseInt(pathParts[2]);
        } catch (NumberFormatException e) {
           throw new InvalidIdInPath();
        }
    }

    public <T extends Task> T taskFromRequestBody(String requestBody, Class<T> classType) throws IOException {
        return gson().fromJson(requestBody,classType);
    }

    public boolean jsonHasId(String requestBody) {
        final JsonElement jsonElement = JsonParser.parseString(requestBody);

        if (!jsonElement.isJsonObject()) {
            throw new NotAJsonObject();
        }
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        return jsonObject.has("id");
    }

    public void writeNullTaskInArgumentResponse(HttpExchange exchange) throws IOException {
        writeResponse(exchange, "Передан null", 400);
    }

    public void writeNotAJsonObjectResponse(HttpExchange exchange) throws IOException {
        writeResponse(exchange, "Ожидается Json в теле запроса", 400);
    }

    public void writeInvalidIdInPathResponse(HttpExchange exchange) throws IOException {
        writeResponse(exchange, "Некорректный id в пути запроcа", 400);
    }

    public void writeResourceNotFoundResponse(HttpExchange exchange) throws IOException {
        writeResponse(exchange, "Ресурс не найден", 404);
    }

    public class AddTaskResponse {
        public int id;

        AddTaskResponse(int id) {
            this.id = id;
        }
    }

    enum Endpoint {
        GET_TASKS, GET_TASK_BY_ID, POST_TASK, DELETE_TASK,
        GET_EPICS, GET_EPIC_BY_ID, POST_EPIC, DELETE_EPIC, GET_EPIC_SUBTASKS,
        GET_SUBTASKS, GET_SUBTASK_BY_ID, POST_SUBTASK, DELETE_SUBTASK,
        GET_HISTORY, GET_PRIORITIZED_TASKS, UNKNOWN, FILL_DEFAULT
    }
}

