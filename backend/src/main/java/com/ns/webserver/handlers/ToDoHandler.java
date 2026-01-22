package com.ns.webserver.handlers;

import com.ns.webserver.models.ToDo;
import org.json.JSONArray;
import org.json.JSONObject;
import tcpframework.*;
import tcpframework.exceptions.NotFoundException;

import java.net.Socket;
import java.util.HashMap;

public class ToDoHandler extends RequestHandler {
    private final String prefixPath;
    private HashMap<String, ToDo> toDoStore;
    HashMap<String, RouteCommand> routes = new HashMap<>();
    private int idCounter = 1;

    public ToDoHandler(String prefixPath) {
        this.prefixPath = prefixPath;
//        toDoStore.put("1", new ToDo("1", "Sample ToDo 1", false));
        toDoStore = new HashMap<>();
        routes.put("GET /api/todos", this::sendAllToDos);
        routes.put("POST /api/todos", this::addTodo);
        routes.put("PUT /api/todos/:id", this::updateTodo);
        routes.put("DELETE /api/todos/:id", this::deleteTodo);
        routes.put("OPTIONS /api/todos", this::handleOptions);
        routes.put("OPTIONS /api/todos/:id", this::handleOptions);
    }


    @Override
    public void handle(HTTPRequest request, Socket socket) throws Exception {
        String normalizedPath = request.getRequestHeadString().substring(0,request.getRequestHeadString().lastIndexOf("/"))+"/:id";
//        System.out.println("ToDoHandler normalizedPath:"+normalizedPath);

        if (!routes.containsKey(request.getRequestHeadString())){
            routes.get(normalizedPath).run(socket, request);
        } else {
            routes.get(request.getRequestHeadString()).run(socket, request);
        }


    }

    private void handleOptions(Socket socket, HTTPRequest request) throws Exception {
        HTTPResponse response = new HTTPResponse(204, "No Content");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setHeader("Access-Control-Max-Age", "86400");
        response.send(socket);
    }

    private void sendAllToDos(Socket socket, HTTPRequest request) throws Exception {
        JSONArray jsonArray = new JSONArray();
        for (ToDo todo : toDoStore.values()) {
            System.out.println("ToDo ID: " + todo.getId() + ", Title: " + todo.getTitle() + ", Completed: " + todo.isCompleted());
            JSONObject json = new JSONObject();
            json.put("id", todo.getId());
            json.put("title", todo.getTitle());
            json.put("completed", todo.isCompleted());
            jsonArray.put(json);
        }

        HTTPResponse response = new HTTPResponse(200, "OK");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setBody(jsonArray.toString().getBytes(), "application/json");
        response.send(socket);
    }

    private void addTodo(Socket socket, HTTPRequest request) throws Exception {

        JSONObject json = new JSONObject(request.getBody());

        ToDo newToDo = new ToDo(String.valueOf(idCounter), json.getString("title"), json.getBoolean("completed"));
        toDoStore.put(newToDo.getId(), newToDo);
        idCounter++;
        for (ToDo todo : toDoStore.values()) {
            System.out.println("ToDo ID: " + todo.getId() + ", Title: " + todo.getTitle() + ", Completed: " + todo.isCompleted());
        }

        System.out.println("Added ToDo: " + newToDo.getId());

        HTTPResponse response = new HTTPResponse(200, "OK");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.send(socket);
    }

    private void updateTodo(Socket socket, HTTPRequest request) throws Exception {
        String id = request.getRequestHeadString().substring(request.getRequestHeadString().lastIndexOf("/") + 1);
        System.out.println("Updating ToDo ID: " + id);
        JSONObject json = new JSONObject(request.getBody());
        ToDo existingToDo = toDoStore.get(id);
        System.out.print(existingToDo);
        if (existingToDo != null) {
            existingToDo.setTitle(json.getString("title"));
            existingToDo.setCompleted(json.getBoolean("completed"));
            System.out.println("ToDo after update: " + toDoStore.get(id).getTitle() + ", Completed: " + toDoStore.get(id).isCompleted());

            HTTPResponse response = new HTTPResponse(200, "OK");
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.send(socket);
        } else {
            throw new NotFoundException("ToDo not found: " + id);
        }
    }

    private void deleteTodo(Socket socket, HTTPRequest request) throws Exception {
        String id = request.getRequestHeadString().substring(request.getRequestHeadString().lastIndexOf("/") + 1);
        ToDo removedToDo = toDoStore.remove(id);
        if (removedToDo != null) {
            System.out.println("Deleted ToDo: " + removedToDo.getId());

            HTTPResponse response = new HTTPResponse(200, "OK");
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.send(socket);

        } else {
            throw new NotFoundException("ToDo not found: " + id);
        }
    }
}
