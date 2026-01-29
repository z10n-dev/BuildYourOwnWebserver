package com.ns.webserver.handlers;

import com.ns.webserver.models.ToDo;
import org.json.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;
import tcpframework.*;
import tcpframework.exceptions.NotFoundException;
import tcpframework.reqeustHandlers.MethodeBasedHandler;
import tcpframework.reqeustHandlers.RouteBasedHandler;

import java.net.Socket;
import java.util.HashMap;
import java.util.UUID;

/**
 * The ToDoHandler class is responsible for handling HTTP requests related to
 * ToDo operations such as retrieving, adding, updating, and deleting ToDo items.
 * It extends the RequestHandler class and provides route-specific logic.
 */
public class ToDoHandler extends RouteBasedHandler {
    private final HashMap<UUID, ToDo> toDoStore;


    /**
     * Constructor for ToDoHandler.
     * Initializes the toDoStore and sets up the routes for handling ToDo-related HTTP requests.
     */
    public ToDoHandler() {
        toDoStore = new HashMap<>();
        routes.put("GET /api/todos", this::sendAllToDos);
        routes.put("POST /api/todos", this::addTodo);
        routes.put("PUT /api/todos/:id", this::updateTodo);
        routes.put("DELETE /api/todos/:id", this::deleteTodo);
        routes.put("OPTIONS /api/todos", this::handleOptions);
        routes.put("OPTIONS /api/todos/:id", this::handleOptions);
    }

    /**
     * Handles HTTP OPTIONS requests by sending a response with allowed methods and headers.
     *
     * @param request The HTTP request.
     * @throws Exception If an error occurs during response creation or sending.
     */
    private HTTPResponse handleOptions(HTTPRequest request) throws Exception {
        HTTPResponse response = new HTTPResponse(204, "No Content");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", request.getPath().endsWith("/todos") ? "GET, POST, OPTIONS" : "PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setHeader("Access-Control-Max-Age", "86400");
        return response;
    }

    /**
     * Sends all ToDo items as a JSON array in the HTTP response.
     *
     * @param request The HTTP request.
     * @throws Exception If an error occurs during response creation or sending.
     */
    private HTTPResponse sendAllToDos(HTTPRequest request) throws Exception {
        JSONArray jsonArray = new JSONArray();
        for (ToDo todo : toDoStore.values()) {
            JSONObject json = new JSONObject();
            json.put("id", todo.id().toString());
            json.put("title", todo.title());
            json.put("completed", todo.completed());
            jsonArray.put(json);
        }

        HTTPResponse response = new HTTPResponse(200, "OK");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setBody(jsonArray.toString().getBytes(), "application/json");
        return response;
    }

    /**
     * Adds a new ToDo item to the store and sends a response.
     *
     * @param request The HTTP request containing the ToDo data in the body.
     * @throws Exception If an error occurs during response creation or sending.
     */
    private HTTPResponse addTodo(HTTPRequest request) throws Exception {

        JSONObject json = new JSONObject(request.getBody());

        ToDo newToDo = new ToDo(UUID.randomUUID(), json.getString("title"), json.getBoolean("completed"));
        toDoStore.put(newToDo.id(), newToDo);

        System.out.println("Added ToDo: " + newToDo.id().toString());
        ServerLogger.getInstance().newLog("Added ToDo: " + newToDo.id().toString());

        HTTPResponse response = new HTTPResponse(201, "Created");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Location", "/api/todos/" + newToDo.id().toString());
        response.setBody(new JSONObject()
                .put("id", newToDo.id().toString())
                .put("title", newToDo.title())
                .put("completed", newToDo.completed())
                .toString()
                .getBytes(), "application/json");
        return response;
    }

    /**
     * Updates an existing ToDo item in the store and sends a response.
     *
     * @param request The HTTP request containing the updated ToDo data in the body.
     * @throws Exception If the ToDo item is not found or an error occurs during response creation or sending.
     */
    private HTTPResponse updateTodo(HTTPRequest request) throws Exception {
        String id = request.getRequestHead().substring(request.getRequestHead().lastIndexOf("/") + 1);
        JSONObject json = new JSONObject(request.getBody());
        ToDo existingToDo = toDoStore.get(UUID.fromString(id));
        System.out.print(existingToDo);
        if (existingToDo != null) {
            ToDo newToDo = new ToDo(existingToDo.id(), json.getString("title"), json.getBoolean("completed"));
            toDoStore.put(existingToDo.id(), newToDo);

            ServerLogger.getInstance().newLog("Updated ToDo: " + newToDo.id().toString());

            HTTPResponse response = new HTTPResponse(200, "OK");
            response.setHeader("Access-Control-Allow-Origin", "*");
            return response;
        } else {
            throw new NotFoundException("ToDo not found: " + id);
        }
    }

    /**
     * Deletes a ToDo item from the store and sends a response.
     *
     * @param request The HTTP request containing the ID of the ToDo to delete.
     * @throws Exception If the ToDo item is not found or an error occurs during response creation or sending.
     */
    private HTTPResponse deleteTodo(HTTPRequest request) throws Exception {
        String id = request.getRequestHead().substring(request.getRequestHead().lastIndexOf("/") + 1);
        ToDo removedToDo = toDoStore.remove(UUID.fromString(id));
        if (removedToDo != null) {

            ServerLogger.getInstance().newLog("Deleted ToDo: " + removedToDo.id().toString());

            HTTPResponse response = new HTTPResponse(200, "OK");
            response.setHeader("Access-Control-Allow-Origin", "*");
            return response;

        } else {
            throw new NotFoundException("ToDo not found: " + id);
        }


    }
}
