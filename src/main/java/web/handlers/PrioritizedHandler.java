package web.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import service.tasks.TaskManager;

import java.io.IOException;

public class PrioritizedHandler extends BaseHandler implements HttpHandler  {
    public PrioritizedHandler(TaskManager taskManager) {
        super(taskManager);
    }

    private Endpoint getEndpoint(String requestPath, String requestMethod) {
        final String[] pathParts = requestPath.split("/");

        if (requestMethod.equals("GET") && pathParts.length == 2 && pathParts[1].equals("prioritized")) {
                return Endpoint.GET_PRIORITIZED_TASKS;
        }
        return Endpoint.UNKNOWN;
    }

    private void handleGetPrioritized(HttpExchange exchange) throws IOException {
        writeResponse(exchange, taskManager.getPrioritizedTasks(), 200);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        final Endpoint endpoint = getEndpoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());

        try {
            if (endpoint == Endpoint.GET_PRIORITIZED_TASKS) {
                handleGetPrioritized(exchange);
            } else {
                writeResourceNotFoundResponse(exchange);
            }
        } catch (IOException e) {
            writeResponse(exchange, e.getMessage(), 400);
        }
    }
}
