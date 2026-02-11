package com.ns.webserver.handlers;

import com.ns.tcpframework.HTTPRequest;
import com.ns.tcpframework.HTTPResponse;
import com.ns.webserver.models.ToDo;
import org.json.JSONArray;
import org.json.JSONObject;
import com.ns.tcpframework.exceptions.NotFoundException;
import com.ns.tcpframework.logger.LogDestination;
import com.ns.tcpframework.logger.ServerLogger;
import com.ns.tcpframework.reqeustHandlers.RouteBasedHandler;
import com.ns.tcpframework.logger.Loglevel;

import java.util.HashMap;
import java.util.UUID;

/**
 * RESTful API handler for managing ToDo items with full CRUD operations.
 * <p>
 * This handler extends {@link RouteBasedHandler} to provide a complete REST API for
 * ToDo list management. It maintains an in-memory store of ToDo items and supports
 * all standard CRUD operations (Create, Read, Update, Delete) along with proper
 * CORS headers for cross-origin requests.
 * <p>
 * Supported operations:
 * <ul>
 *   <li><b>GET /api/todos</b> - Retrieve all ToDo items</li>
 *   <li><b>POST /api/todos</b> - Create a new ToDo item</li>
 *   <li><b>PUT /api/todos/:id</b> - Update an existing ToDo item by ID</li>
 *   <li><b>DELETE /api/todos/:id</b> - Delete a ToDo item by ID</li>
 *   <li><b>OPTIONS /api/todos</b> - CORS preflight for collection endpoint</li>
 *   <li><b>OPTIONS /api/todos/:id</b> - CORS preflight for item endpoint</li>
 * </ul>
 * <p>
 * Data model: Each ToDo item contains:
 * <ul>
 *   <li><b>id</b>: UUID - Unique identifier (auto-generated)</li>
 *   <li><b>title</b>: String - Description of the task</li>
 *   <li><b>completed</b>: boolean - Completion status</li>
 * </ul>
 * <p>
 * CORS support: All endpoints include Access-Control-Allow-Origin: * headers,
 * enabling requests from any origin. This is suitable for development but should
 * be restricted in production environments.
 * <p>
 * Storage: This implementation uses in-memory storage (HashMap), meaning all data
 * is lost when the server restarts. For production use, consider integrating with
 * a persistent database.
 * <p>
 * Example usage:
 * <pre>
 * // Create a new ToDo
 * POST /api/todos
 * Content-Type: application/json
 *
 * {"title": "Buy groceries", "completed": false}
 *
 * // Response: 201 Created
 * {"id": "123e4567-e89b-12d3-a456-426614174000", "title": "Buy groceries", "completed": false}
 *
 * // Get all ToDos
 * GET /api/todos
 *
 * // Response: 200 OK
 * [{"id": "123e4567-e89b-12d3-a456-426614174000", "title": "Buy groceries", "completed": false}]
 * </pre>
 * <p>
 * Thread-safety: This handler is NOT thread-safe due to the use of HashMap without
 * synchronization. Concurrent modifications may lead to race conditions. For production
 * use, consider using ConcurrentHashMap or external synchronization.
 *
 * @see RouteBasedHandler
 * @see ToDo
 * @see HTTPRequest
 * @see HTTPResponse
 */
public class ToDoHandler extends RouteBasedHandler {
    /**
     * In-memory storage for ToDo items, keyed by their unique UUID identifiers.
     * <p>
     * This HashMap provides O(1) lookup, insertion, and deletion performance.
     * Keys are UUIDs generated when ToDo items are created, and values are
     * immutable {@link ToDo} record instances.
     * <p>
     * Warning: This is not thread-safe. Consider using ConcurrentHashMap for
     * production environments with concurrent access.
     * <p>
     * Data persistence: All data stored here is volatile and will be lost on
     * server restart. This is suitable for development and testing but not for
     * production use.
     */
    private final HashMap<UUID, ToDo> toDoStore;


    /**
     * Constructs a ToDoHandler and initializes the routing configuration.
     * <p>
     * This constructor performs two primary operations:
     * <ol>
     *   <li>Initializes an empty in-memory store for ToDo items</li>
     *   <li>Registers six route mappings for the REST API endpoints</li>
     * </ol>
     * <p>
     * Registered routes:
     * <ul>
     *   <li>GET /api/todos → {@link #sendAllToDos(HTTPRequest)}</li>
     *   <li>POST /api/todos → {@link #addTodo(HTTPRequest)}</li>
     *   <li>PUT /api/todos/:id → {@link #updateTodo(HTTPRequest)}</li>
     *   <li>DELETE /api/todos/:id → {@link #deleteTodo(HTTPRequest)}</li>
     *   <li>OPTIONS /api/todos → {@link #handleOptions(HTTPRequest)}</li>
     *   <li>OPTIONS /api/todos/:id → {@link #handleOptions(HTTPRequest)}</li>
     * </ul>
     * <p>
     * Route keys combine HTTP method and path (e.g., "GET /api/todos") and map
     * to method references that handle the corresponding requests. The :id parameter
     * in routes is resolved by the parent RouteBasedHandler.
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
     * Handles CORS preflight OPTIONS requests for both collection and item endpoints.
     * <p>
     * This method responds to OPTIONS requests sent by browsers as part of the CORS
     * preflight mechanism. It informs the browser which HTTP methods and headers are
     * allowed for cross-origin requests.
     * <p>
     * Response headers:
     * <ul>
     *   <li><b>Status</b>: 204 No Content - Standard for OPTIONS with no body</li>
     *   <li><b>Access-Control-Allow-Origin</b>: * - Allows requests from any origin</li>
     *   <li><b>Access-Control-Allow-Methods</b>: Varies by endpoint:
     *     <ul>
     *       <li>/api/todos: GET, POST, OPTIONS</li>
     *       <li>/api/todos/:id: PUT, DELETE, OPTIONS</li>
     *     </ul>
     *   </li>
     *   <li><b>Access-Control-Allow-Headers</b>: Content-Type - Allows Content-Type header</li>
     *   <li><b>Access-Control-Max-Age</b>: 86400 - Caches preflight result for 24 hours</li>
     * </ul>
     * <p>
     * The allowed methods vary based on the request path: collection endpoints (/api/todos)
     * allow GET and POST, while item endpoints (/api/todos/:id) allow PUT and DELETE.
     *
     * @param request The HTTP OPTIONS request. The path is examined to determine allowed methods.
     * @return An HTTPResponse with status 204 and appropriate CORS headers. Never returns null.
     * @throws Exception Not thrown in normal operation, but signature allows for future enhancements.
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
     * Retrieves all ToDo items and returns them as a JSON array.
     * <p>
     * This method handles GET requests to /api/todos and returns all ToDo items
     * in the store as a JSON array. Each ToDo is serialized with its id, title,
     * and completed status.
     * <p>
     * Response format:
     * <pre>
     * [
     *   {"id": "uuid-1", "title": "Task 1", "completed": false},
     *   {"id": "uuid-2", "title": "Task 2", "completed": true}
     * ]
     * </pre>
     * <p>
     * Response characteristics:
     * <ul>
     *   <li><b>Status</b>: 200 OK - Successful retrieval</li>
     *   <li><b>Content-Type</b>: application/json</li>
     *   <li><b>CORS</b>: Access-Control-Allow-Origin: *</li>
     *   <li><b>Body</b>: JSON array of all ToDo objects</li>
     * </ul>
     * <p>
     * If the store is empty, returns an empty JSON array ([]).
     *
     * @param request The HTTP GET request. Request content is not used.
     * @return An HTTPResponse with status 200 containing all ToDo items as JSON. Never returns null.
     * @throws Exception If JSON serialization fails, though this is unlikely with the simple data structure.
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
     * Creates a new ToDo item from the request body and adds it to the store.
     * <p>
     * This method handles POST requests to /api/todos. It parses the JSON request body,
     * generates a unique UUID for the new item, stores it, and returns the created item
     * with its assigned ID.
     * <p>
     * Expected request body format:
     * <pre>
     * {
     *   "title": "Task description",
     *   "completed": false
     * }
     * </pre>
     * <p>
     * Response format:
     * <pre>
     * {
     *   "id": "generated-uuid",
     *   "title": "Task description",
     *   "completed": false
     * }
     * </pre>
     * <p>
     * Response characteristics:
     * <ul>
     *   <li><b>Status</b>: 201 Created - Indicates successful resource creation</li>
     *   <li><b>Location</b>: /api/todos/:id - URL of the newly created resource</li>
     *   <li><b>Content-Type</b>: application/json</li>
     *   <li><b>CORS</b>: Access-Control-Allow-Origin: *</li>
     *   <li><b>Body</b>: JSON representation of the created ToDo including its ID</li>
     * </ul>
     * <p>
     * The method logs the current count of ToDo items after creation for debugging purposes.
     *
     * @param request The HTTP POST request containing the ToDo data as JSON in the body.
     * @return An HTTPResponse with status 201 containing the created ToDo item with its assigned ID.
     *         Never returns null.
     * @throws Exception If JSON parsing fails (e.g., malformed JSON, missing required fields) or
     *                   if an I/O error occurs.
     */
    private HTTPResponse addTodo(HTTPRequest request) throws Exception {

        JSONObject json = new JSONObject(request.getBody());

        ToDo newToDo = new ToDo(UUID.randomUUID(), json.getString("title"), json.getBoolean("completed"));
        toDoStore.put(newToDo.id(), newToDo);

        ServerLogger.getInstance().log(Loglevel.DEBUG, "Current ToDos: " + toDoStore.size(), LogDestination.EVERYWHERE);

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
     * Updates an existing ToDo item with new data from the request body.
     * <p>
     * This method handles PUT requests to /api/todos/:id. It extracts the ID from the
     * request path, parses the JSON request body, and replaces the existing ToDo item
     * with the updated data while preserving the original ID.
     * <p>
     * Expected request body format:
     * <pre>
     * {
     *   "title": "Updated task description",
     *   "completed": true
     * }
     * </pre>
     * <p>
     * ID extraction: The ToDo ID is extracted from the last segment of the request path
     * (e.g., from "PUT /api/todos/123e4567-e89b-12d3-a456-426614174000", extracts
     * "123e4567-e89b-12d3-a456-426614174000").
     * <p>
     * Response characteristics:
     * <ul>
     *   <li><b>Status</b>: 200 OK - Successful update</li>
     *   <li><b>CORS</b>: Access-Control-Allow-Origin: *</li>
     *   <li><b>Body</b>: Empty (only status confirmation)</li>
     * </ul>
     * <p>
     * The method logs the updated ToDo ID for debugging purposes.
     *
     * @param request The HTTP PUT request containing the updated ToDo data as JSON in the body.
     *                The ID is extracted from the request path.
     * @return An HTTPResponse with status 200 indicating successful update. Never returns null.
     * @throws NotFoundException If no ToDo item exists with the specified ID.
     * @throws Exception If JSON parsing fails, UUID parsing fails, or an I/O error occurs.
     */
    private HTTPResponse updateTodo(HTTPRequest request) throws Exception {
        String id = request.getRequestHead().substring(request.getRequestHead().lastIndexOf("/") + 1);
        JSONObject json = new JSONObject(request.getBody());
        ToDo existingToDo = toDoStore.get(UUID.fromString(id));
        if (existingToDo != null) {
            ToDo newToDo = new ToDo(existingToDo.id(), json.getString("title"), json.getBoolean("completed"));
            toDoStore.put(existingToDo.id(), newToDo);

            ServerLogger.getInstance().log(Loglevel.DEBUG, "Updated ToDo: " + newToDo.id().toString(), LogDestination.EVERYWHERE);

            HTTPResponse response = new HTTPResponse(200, "OK");
            response.setHeader("Access-Control-Allow-Origin", "*");
            return response;
        } else {
            throw new NotFoundException("ToDo not found: " + id);
        }
    }

    /**
     * Deletes a ToDo item from the store by its ID.
     * <p>
     * This method handles DELETE requests to /api/todos/:id. It extracts the ID from
     * the request path, removes the corresponding ToDo item from the store, and returns
     * a success response if the item existed.
     * <p>
     * ID extraction: The ToDo ID is extracted from the last segment of the request path
     * (e.g., from "DELETE /api/todos/123e4567-e89b-12d3-a456-426614174000", extracts
     * "123e4567-e89b-12d3-a456-426614174000").
     * <p>
     * Response characteristics:
     * <ul>
     *   <li><b>Status</b>: 200 OK - Successful deletion</li>
     *   <li><b>CORS</b>: Access-Control-Allow-Origin: *</li>
     *   <li><b>Body</b>: Empty (only status confirmation)</li>
     * </ul>
     * <p>
     * The method logs the deleted ToDo ID for debugging and audit purposes.
     * <p>
     * Idempotency note: Following REST conventions, attempting to delete a non-existent
     * item throws a NotFoundException rather than succeeding silently. Some REST APIs
     * choose to return 204 No Content for idempotent deletes.
     *
     * @param request The HTTP DELETE request. The ID is extracted from the request path.
     * @return An HTTPResponse with status 200 indicating successful deletion. Never returns null.
     * @throws NotFoundException If no ToDo item exists with the specified ID.
     * @throws Exception If UUID parsing fails or an I/O error occurs.
     */
    private HTTPResponse deleteTodo(HTTPRequest request) throws Exception {
        String id = request.getRequestHead().substring(request.getRequestHead().lastIndexOf("/") + 1);
        ToDo removedToDo = toDoStore.remove(UUID.fromString(id));
        if (removedToDo != null) {

            ServerLogger.getInstance().log(Loglevel.DEBUG, "Deleted: " + removedToDo.id().toString(), LogDestination.EVERYWHERE);

            HTTPResponse response = new HTTPResponse(200, "OK");
            response.setHeader("Access-Control-Allow-Origin", "*");
            return response;

        } else {
            throw new NotFoundException("ToDo not found: " + id);
        }


    }
}
