package com.ns.webserver.models;

import java.util.UUID;

/**
 * Represents a ToDo item with an ID, title, and completion status.
 *
 * @param id        The unique identifier for the ToDo item.
 * @param title     The title or description of the ToDo item.
 * @param completed A boolean indicating whether the ToDo item is completed.
 */
public record ToDo(UUID id, String title, boolean completed) {

}
