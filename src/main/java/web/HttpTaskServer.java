package web;

import com.sun.net.httpserver.HttpServer;

import service.tasks.TaskManager;
import util.Managers;
import web.handlers.*;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {

    private static final int PORT = 8080;
    private final HttpServer httpServer;

    public void start(TaskManager taskManager) throws IOException {
        httpServer.createContext("/tasks", new TaskHandler(taskManager));
        httpServer.createContext("/epics", new EpicHandler(taskManager));
        httpServer.createContext("/subtasks", new SubtaskHandler(taskManager));
        httpServer.createContext("/history", new HistoryHandler(taskManager));
        httpServer.createContext("/prioritized",
                new PrioritizedHandler(taskManager));
        httpServer.createContext("/filldefault",
                new FillDefaultHandler(taskManager));
        httpServer.start();
    }

    public HttpTaskServer() throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
    }

    public void stop() {
        httpServer.stop(1);
    }

    public static void main(String[] args) {
        TaskManager taskManager = Managers.getDefaultTaskManager();

        try {
            HttpTaskServer taskServer = new HttpTaskServer();
            taskServer.start(taskManager);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
