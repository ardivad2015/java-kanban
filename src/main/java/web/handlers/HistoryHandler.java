package web.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import service.tasks.TaskManager;
import java.io.IOException;

public class HistoryHandler extends BaseHandler implements HttpHandler  {
    public HistoryHandler(TaskManager taskManager) {
        super(taskManager);
    }

    private Endpoint getEndpoint(String requestPath, String requestMethod) {
       final String[] pathParts = requestPath.split("/");

        if (requestMethod.equals("GET") && pathParts.length == 2 && pathParts[1].equals("history")) {
                return Endpoint.GET_HISTORY;
        }
        return Endpoint.UNKNOWN;
    }

    private void handleGetHistory(HttpExchange exchange) throws IOException {
        writeResponse(exchange, taskManager.getHistory(), 200);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        final Endpoint endpoint = getEndpoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());

        try {
            if (endpoint == Endpoint.GET_HISTORY) {
                handleGetHistory(exchange);
            } else {
                writeResourceNotFoundResponse(exchange);
            }
        } catch (IOException e) {
            writeResponse(exchange, e.getMessage(), 400);
        }
    }
}
