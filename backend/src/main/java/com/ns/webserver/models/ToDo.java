package com.ns.webserver.models;

import java.util.UUID;

/**
 * Immutable data model representing a ToDo item in the task management system.
 * <p>
 * This record encapsulates the essential attributes of a ToDo task, including a unique
 * identifier, descriptive title, and completion status. As a Java record (introduced in
 * Java 14), it provides automatic implementations of constructor, accessors, equals(),
 * hashCode(), and toString() methods, ensuring immutability and value-based semantics.
 * <p>
 * Key characteristics:
 * <ul>
 *   <li><b>Immutability</b>: All fields are final; instances cannot be modified after creation</li>
 *   <li><b>Value-based equality</b>: Two ToDo instances are equal if all their fields are equal</li>
 *   <li><b>Automatic accessors</b>: id(), title(), and completed() methods are generated</li>
 *   <li><b>Thread-safe</b>: Immutability guarantees thread safety for read operations</li>
 * </ul>
 * <p>
 * Usage patterns:
 * <pre>
 * // Create a new ToDo item
 * UUID id = UUID.randomUUID();
 * ToDo todo = new ToDo(id, "Buy groceries", false);
 *
 * // Access fields using generated accessors
 * String title = todo.title();        // "Buy groceries"
 * boolean done = todo.completed();    // false
 *
 * // Create an updated version (immutable update pattern)
 * ToDo completedTodo = new ToDo(todo.id(), todo.title(), true);
 * </pre>
 * <p>
 * JSON Representation: This record is typically serialized to JSON in the following format:
 * <pre>
 * {
 *   "id": "123e4567-e89b-12d3-a456-426614174000",
 *   "title": "Buy groceries",
 *   "completed": false
 * }
 * </pre>
 * <p>
 * Integration: This model is used by {@link com.ns.webserver.handlers.ToDoHandler}
 * to manage ToDo items through a RESTful API, with operations for creating, reading,
 * updating, and deleting tasks.
 *
 * @param id        The unique identifier for the ToDo item, generated using {@link UUID#randomUUID()}.
 *                  This ID is used for referencing, updating, and deleting specific ToDo items.
 *                  Must not be null.
 * @param title     The title or description of the ToDo task. This is the main textual content
 *                  displayed to users describing what needs to be done. Should not be null or empty
 *                  for meaningful tasks, though no validation is enforced at this level.
 * @param completed A boolean flag indicating whether the ToDo item has been completed.
 *                  {@code true} indicates the task is done, {@code false} indicates it is pending.
 *                  This field supports task state tracking and filtering.
 * @see com.ns.webserver.handlers.ToDoHandler
 * @see UUID
 * @since 1.0
 */
public record ToDo(UUID id, String title, boolean completed) {

}
