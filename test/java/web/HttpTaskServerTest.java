package web;

import com.google.gson.*;
import model.Task;
import org.junit.jupiter.api.*;
import service.tasks.InMemoryTaskManager;
import service.tasks.TaskManager;
import web.handlers.BaseHandler;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpTaskServerTest {

    TaskManager taskManager;
    HttpTaskServer taskServer = new HttpTaskServer();
    Gson gson = BaseHandler.gson();
    static final String LOCAL_URI = "http://localhost:8080/";

    public HttpTaskServerTest() throws IOException {
    }

    @BeforeEach
    public void beforeEach() throws IOException {
        taskManager = new InMemoryTaskManager();
        taskServer.start(taskManager);
    }

    @AfterEach
    public void afterEach() throws IOException {
        taskServer.stop();
    }

    @Test
    public void addTask() throws IOException, InterruptedException {
        final Task task = new Task("Простая задача 1", "", "01.01.24 00:00", 30);
        final String taskJson = gson.toJson(task);

        final HttpClient client = HttpClient.newHttpClient();
        final URI url = URI.create(LOCAL_URI + "tasks");
        final HttpRequest request = HttpRequest.newBuilder().uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        List<Task> tasks = taskManager.getSimpleTaskList();

        assertEquals(1, tasks.size(), "Некорректное количество задач");
        assertEquals("Простая задача 1", tasks.get(0).getTopic(), "Некорректное имя задачи");
        assertEquals(0, tasks.get(0).getId(), "Некорректный id задачи");
    }

    @Test
    public void updateTask() throws IOException, InterruptedException {
        final Task task = new Task("Простая задача 1", "", "01.01.24 00:00", 30);
        taskManager.addSimpleTask(task);

        final String newTopic = "Задача обновлена";
        task.setTopic(newTopic);

        final String taskJson = gson.toJson(task);

        final HttpClient client = HttpClient.newHttpClient();
        final URI url = URI.create(LOCAL_URI + "tasks");
        final HttpRequest request = HttpRequest.newBuilder().uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        List<Task> tasks = taskManager.getSimpleTaskList();

        assertEquals(1, tasks.size(), "Некорректное количество задач");
        assertEquals(newTopic, taskManager.getSimpleTask(task.getId()).getTopic(), "Некорректное имя задачи");
    }

    @Test
    public void deleteTask() throws IOException, InterruptedException {
        final Task task = new Task("Простая задача 1", "", "01.01.24 00:00", 30);
        taskManager.addSimpleTask(task);

        final String taskJson = gson.toJson(task);

        final HttpClient client = HttpClient.newHttpClient();
        final URI url = URI.create(LOCAL_URI + "tasks/" + task.getId());
        final HttpRequest request = HttpRequest.newBuilder().uri(url)
                .DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        List<Task> tasks = taskManager.getSimpleTaskList();

        assertEquals(0, tasks.size(), "Некорректное количество задач");
    }

    @Test
    public void getTasks() throws IOException, InterruptedException {
        final Task task = new Task("Простая задача 1", "", "01.01.24 00:00", 30);
        final Task task2 = new Task("Простая задача 2", "", "01.01.24 00:31", 30);

        taskManager.addSimpleTask(task);
        taskManager.addSimpleTask(task2);
        final HttpClient client = HttpClient.newHttpClient();
        final URI url = URI.create(LOCAL_URI + "tasks");
        final HttpRequest request = HttpRequest.newBuilder().uri(url)
                .GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        JsonElement jsonElement = JsonParser.parseString(response.body());

        assertTrue(jsonElement.isJsonArray());

        JsonArray jsonArray = jsonElement.getAsJsonArray();

        assertEquals(2, jsonArray.size());
    }

    @Test
    public void statusCode406WhenIntersection() throws IOException, InterruptedException {
        final Task task = new Task("Простая задача 1", "", "01.01.24 00:00", 30);
        taskManager.addSimpleTask(task);

        final Task task2 = new Task("Простая задача 1", "", "01.01.24 00:00", 30);
        final String taskJson = gson.toJson(task2);

        final HttpClient client = HttpClient.newHttpClient();
        final URI url = URI.create(LOCAL_URI + "tasks");
        final HttpRequest request = HttpRequest.newBuilder().uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode());
    }

    @Test
    public void statusCode404WhenTaskNotFound() throws IOException, InterruptedException {
        final Task task = new Task("Простая задача 1", "", "01.01.24 00:00", 30);
        taskManager.addSimpleTask(task);

        final HttpClient client = HttpClient.newHttpClient();
        final URI url = URI.create(LOCAL_URI + "tasks/" + (task.getId() + 1));
        final HttpRequest request = HttpRequest.newBuilder().uri(url)
                .GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    public void statusCode400WhenTaskNotJsonInBody() throws IOException, InterruptedException {
        final HttpClient client = HttpClient.newHttpClient();
        final URI url = URI.create(LOCAL_URI + "tasks");
        final HttpRequest request = HttpRequest.newBuilder().uri(url)
                .POST(HttpRequest.BodyPublishers.noBody()).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
        assertEquals(gson.toJson("Ожидается Json в теле запроса"), response.body());
    }

    @Test
    public void statusCode400WhenInvalidIdInPath() throws IOException, InterruptedException {
        final HttpClient client = HttpClient.newHttpClient();
        final URI url = URI.create(LOCAL_URI + "tasks/test");
        final HttpRequest request = HttpRequest.newBuilder().uri(url)
                .GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
        assertEquals(gson.toJson("Некорректный id в пути запроcа"), response.body());
    }

    @Test
    public void getHistory() throws IOException, InterruptedException {
        final Task task = new Task("Простая задача 1", "", "01.01.24 00:00", 30);

        taskManager.addSimpleTask(task);
        taskManager.getSimpleTask(task.getId());

        final HttpClient client = HttpClient.newHttpClient();
        final URI url = URI.create(LOCAL_URI + "history");
        final HttpRequest request = HttpRequest.newBuilder().uri(url)
                .GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        JsonElement jsonElement = JsonParser.parseString(response.body());

        assertTrue(jsonElement.isJsonArray());

        JsonArray jsonArray = jsonElement.getAsJsonArray();

        assertEquals(1, jsonArray.size());
    }

    @Test
    public void getPrioritized() throws IOException, InterruptedException {
        final Task task = new Task("Простая задача 1", "", "01.01.24 00:00", 30);
        final Task task2 = new Task("Простая задача 1", "", "01.01.23 00:00", 30);

        taskManager.addSimpleTask(task);
        taskManager.addSimpleTask(task2);

        final HttpClient client = HttpClient.newHttpClient();
        final URI url = URI.create(LOCAL_URI + "prioritized");
        final HttpRequest request = HttpRequest.newBuilder().uri(url)
                .GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        JsonElement jsonElement = JsonParser.parseString(response.body());

        assertTrue(jsonElement.isJsonArray());

        JsonArray jsonArray = jsonElement.getAsJsonArray();

        assertEquals(2, jsonArray.size());
    }
}