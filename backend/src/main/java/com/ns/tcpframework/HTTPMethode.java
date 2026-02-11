package com.ns.tcpframework;

/**
 * Enumeration representing the HTTP request methods supported by the framework.
 * <p>
 * This enum defines the standard HTTP methods as specified in RFC 7231 (HTTP/1.1)
 * and RFC 5789 (PATCH). Each method has a specific semantic meaning that defines
 * the intent of the request operation.
 * <p>
 * HTTP methods are used to indicate the desired action to be performed on the
 * identified resource. The method determines how the server should process the
 * request and what type of response to generate.
 * <p>
 * Method properties:
 * <ul>
 *   <li><b>Safe methods</b>: GET, HEAD, OPTIONS - Read-only operations that don't modify server state</li>
 *   <li><b>Idempotent methods</b>: GET, HEAD, PUT, DELETE, OPTIONS - Multiple identical requests
 *       have the same effect as a single request</li>
 *   <li><b>Cacheable methods</b>: GET, HEAD, POST - Responses may be cached by clients or proxies</li>
 * </ul>
 *
 * @see HTTPRequest
 * @see HTTPResponse
 */
public enum HTTPMethode {
    /**
     * GET method - Retrieves a representation of the specified resource.
     * <p>
     * The GET method requests a representation of the specified resource. Requests using
     * GET should only retrieve data and should have no other effect on the server state.
     * <p>
     * Characteristics:
     * <ul>
     *   <li>Safe: Yes (read-only operation)</li>
     *   <li>Idempotent: Yes (multiple requests return the same result)</li>
     *   <li>Cacheable: Yes</li>
     *   <li>Request has body: No (typically)</li>
     *   <li>Response has body: Yes</li>
     * </ul>
     * <p>
     * Common use cases: Fetching web pages, retrieving API resources, downloading files
     */
    GET,

    /**
     * POST method - Submits data to be processed by the specified resource.
     * <p>
     * The POST method submits an entity to the specified resource, often causing a change
     * in state or side effects on the server. This is commonly used for creating new resources,
     * submitting form data, or triggering server-side operations.
     * <p>
     * Characteristics:
     * <ul>
     *   <li>Safe: No (modifies server state)</li>
     *   <li>Idempotent: No (multiple requests may have different effects)</li>
     *   <li>Cacheable: Yes (with appropriate headers)</li>
     *   <li>Request has body: Yes</li>
     *   <li>Response has body: Yes</li>
     * </ul>
     * <p>
     * Common use cases: Creating new resources, submitting forms, uploading files, processing payments
     */
    POST,

    /**
     * PUT method - Replaces all current representations of the target resource with the request payload.
     * <p>
     * The PUT method replaces the entire target resource with the provided representation.
     * If the resource doesn't exist, it may be created. PUT is typically used for updates
     * where the entire resource is replaced.
     * <p>
     * Characteristics:
     * <ul>
     *   <li>Safe: No (modifies server state)</li>
     *   <li>Idempotent: Yes (multiple identical requests result in the same state)</li>
     *   <li>Cacheable: No</li>
     *   <li>Request has body: Yes</li>
     *   <li>Response has body: Typically no (may return success status)</li>
     * </ul>
     * <p>
     * Common use cases: Updating existing resources, replacing entire entities, uploading files to specific URLs
     */
    PUT,

    /**
     * DELETE method - Deletes the specified resource.
     * <p>
     * The DELETE method removes the specified resource from the server. Multiple identical
     * DELETE requests should have the same effect as a single request (idempotent).
     * <p>
     * Characteristics:
     * <ul>
     *   <li>Safe: No (modifies server state)</li>
     *   <li>Idempotent: Yes (deleting a resource multiple times results in the same state)</li>
     *   <li>Cacheable: No</li>
     *   <li>Request has body: May have (not typical)</li>
     *   <li>Response has body: May have (typically status confirmation)</li>
     * </ul>
     * <p>
     * Common use cases: Removing resources, deleting database records, cleaning up temporary files
     */
    DELETE,

    /**
     * HEAD method - Retrieves the headers for a resource without the response body.
     * <p>
     * The HEAD method is identical to GET except that the server must not return a message
     * body in the response. It's used to obtain metadata about a resource without transferring
     * its entire representation.
     * <p>
     * Characteristics:
     * <ul>
     *   <li>Safe: Yes (read-only operation)</li>
     *   <li>Idempotent: Yes (multiple requests return the same headers)</li>
     *   <li>Cacheable: Yes</li>
     *   <li>Request has body: No</li>
     *   <li>Response has body: No (headers only)</li>
     * </ul>
     * <p>
     * Common use cases: Checking if a resource exists, retrieving metadata (size, type, last-modified),
     * testing for broken links, caching validation
     */
    HEAD,

    /**
     * OPTIONS method - Describes the communication options for the target resource.
     * <p>
     * The OPTIONS method requests information about the communication options available
     * for the target resource. This is commonly used to discover which HTTP methods are
     * supported by the server for a specific resource (indicated by the Allow header).
     * <p>
     * Characteristics:
     * <ul>
     *   <li>Safe: Yes (read-only operation)</li>
     *   <li>Idempotent: Yes (multiple requests return the same options)</li>
     *   <li>Cacheable: No</li>
     *   <li>Request has body: No</li>
     *   <li>Response has body: Typically minimal (may describe options)</li>
     * </ul>
     * <p>
     * Common use cases: CORS preflight requests, discovering supported methods,
     * API capability discovery
     */
    OPTIONS,
}
