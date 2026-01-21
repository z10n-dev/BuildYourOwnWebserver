package com.ns.webserver.handlers;

import com.ns.webserver.models.ToDo;
import org.json.JSONArray;
import org.json.JSONObject;
import tcpframework.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;
import java.util.function.Function;

public class ToDoHandler extends RequestHandler {
    private final String prefixPath;
    private final HashMap<String, ToDo> toDoStore = new HashMap<>();
    HashMap<String, RouteCommand> routes = new HashMap<>();

    public ToDoHandler(String prefixPath) {
        this.prefixPath = prefixPath;
        toDoStore.put("1", new ToDo("1", "Sample ToDo 1", false));
        routes.put("GET /api/todos", this::sendAllToDos);
        routes.put("POST /api/todos", this::addTodo);

    }

    @Override
    public void handle(HTTPRequest request, Socket socket) throws Exception {
        System.out.println("Handling ToDo request: " + request.getMethod() + " " + request.getPath());

        System.out.print("Request handled by ToDoHandler\n");
        routes.get(request.getRequestHeadString()).run(socket, request);


    }

    private void sendAllToDos(Socket socket, HTTPRequest request) throws Exception {
        JSONArray jsonArray = new JSONArray();
        for (ToDo todo : toDoStore.values()) {
            JSONObject json = new JSONObject();
            json.put("id", todo.getId());
            json.put("title", todo.getTitle());
            json.put("completed", todo.isCompleted());
            jsonArray.put(json);
        }

        sendResponse(socket, "application/json", jsonArray.toString().getBytes());
    }

    private void addTodo(Socket socket, HTTPRequest request) throws Exception {

        JSONObject json = new JSONObject(request.getBody());

        ToDo newToDo = new ToDo(json.getString("id"), json.getString("title"), json.getBoolean("completed"));
        toDoStore.put(newToDo.getId(), newToDo);
        for (ToDo todo : toDoStore.values()) {
            System.out.println("ToDo ID: " + todo.getId() + ", Title: " + todo.getTitle() + ", Completed: " + todo.isCompleted());
        }

        System.out.println("Added ToDo: " + newToDo.getId());

        sendResponse(socket, "text/plain", "ToDo created".getBytes());
    }

    @Override
    protected void handlePutRequest(HTTPRequest request, Socket socket) throws Exception {

    }

    @Override
    protected void handleDeleteRequest(HTTPRequest request, Socket socket) throws Exception {

    }

    private String getApiPath(String path) {
        return path.substring(prefixPath.length());
    }
}
