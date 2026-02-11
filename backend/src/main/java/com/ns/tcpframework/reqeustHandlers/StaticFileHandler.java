package com.ns.tcpframework.reqeustHandlers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.ns.tcpframework.HTTPRequest;
import com.ns.tcpframework.HTTPResponse;
import com.ns.tcpframework.exceptions.InternalServerErrorException;
import com.ns.tcpframework.exceptions.NotFoundException;

/**
 * A request handler for serving static files from a specified root directory.
 * <p>
 * This class extends {@link MethodeBasedHandler} and provides implementations for handling
 * HTTP GET and HEAD requests to serve static files. It supports:
 * <ul>
 *   <li>Serving files from a configurable root directory</li>
 *   <li>Automatic MIME type detection based on file extensions</li>
 *   <li>Default serving of index.html for directory requests</li>
 *   <li>HEAD requests for retrieving file metadata without body content</li>
 * </ul>
 * <p>
 * Security considerations:
 * <ul>
 *   <li>All file paths are resolved relative to the configured root directory</li>
 *   <li>Directory traversal is prevented by the Path resolution mechanism</li>
 *   <li>Only existing files can be served; directories without index.html return 404</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>
 * StaticFileHandler handler = new StaticFileHandler("/var/www/html");
 * // Requests to /styles.css will serve /var/www/html/styles.css
 * // Requests to / will serve /var/www/html/index.html
 * </pre>
 *
 * @see MethodeBasedHandler
 * @see HTTPRequest
 * @see HTTPResponse
 */
public class StaticFileHandler extends MethodeBasedHandler {

    /**
     * The root directory path from which static files will be served.
     * <p>
     * All file requests are resolved relative to this path. The path should be
     * an absolute file system path to ensure consistent behavior across different
     * working directories.
     */
    private final String rootPath;

    /**
     * Constructs a StaticFileHandler with the specified root directory.
     * <p>
     * The root directory serves as the base path for all file serving operations.
     * File requests will be resolved relative to this directory, providing isolation
     * and preventing access to files outside the designated static content area.
     * <p>
     * Example:
     * <pre>
     * // Serve files from /var/www/html
     * StaticFileHandler handler = new StaticFileHandler("/var/www/html");
     *
     * // Request to /images/logo.png will resolve to /var/www/html/images/logo.png
     * </pre>
     *
     * @param rootPath The absolute or relative root directory path from which files will be served.
     *                 Should be a valid directory path on the file system.
     */
    public StaticFileHandler(String rootPath) {
        this.rootPath = rootPath;
    }

    /**
     * Handles HTTP GET requests by serving static files from the root directory.
     * <p>
     * This method implements the following logic:
     * <ol>
     *   <li>Resolves the requested path relative to the root directory</li>
     *   <li>If the path is a directory, attempts to serve "index.html" from that directory</li>
     *   <li>Validates that the resolved path exists and is a file (not a directory)</li>
     *   <li>Reads the file contents into memory</li>
     *   <li>Detects the MIME type automatically or defaults to "application/octet-stream"</li>
     *   <li>Returns an HTTP 200 response with the file contents and appropriate Content-Type</li>
     * </ol>
     * <p>
     * Directory handling:
     * <ul>
     *   <li>Request to {@code /} → serves {@code /index.html}</li>
     *   <li>Request to {@code /docs/} → serves {@code /docs/index.html}</li>
     *   <li>If index.html doesn't exist, throws {@link NotFoundException}</li>
     * </ul>
     * <p>
     * MIME type detection is performed using {@link Files#probeContentType(Path)}, which
     * uses the file extension to determine the content type (e.g., .html → text/html,
     * .css → text/css, .js → application/javascript).
     *
     * @param request The HTTP GET request containing the path of the file to serve.
     * @return An HTTPResponse with status 200 containing the file contents and appropriate headers.
     * @throws NotFoundException If the file is not found, is a directory without index.html,
     *                          or the path is invalid.
     * @throws InternalServerErrorException If an I/O error occurs while reading the file
     *                                      (e.g., permission denied, disk error).
     * @throws Exception If any other unexpected error occurs during request handling.
     */
    @Override
    protected HTTPResponse handleGetRequest(HTTPRequest request) throws Exception {
        String path = request.getPath();

        Path filePath = Paths.get(rootPath, path);

        if(Files.isDirectory(filePath)){
            filePath = filePath.resolve("index.html");
        }

        if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
            throw new NotFoundException("File not found: " + filePath);
        }

        try {
            byte[] body = Files.readAllBytes(filePath);

            String mimeType = Files.probeContentType(filePath);

            HTTPResponse response = new HTTPResponse(200, "OK");

            response.setBody(body, mimeType != null ? mimeType : "application/octet-stream");

            return response;

        } catch (IOException e) {
            throw new InternalServerErrorException("Failed to read file: " + filePath);
        }

    }

    /**
     * Handles HTTP HEAD requests by returning file metadata without the file contents.
     * <p>
     * The HTTP HEAD method is identical to GET except that the server MUST NOT return
     * a message body in the response. This method is commonly used to:
     * <ul>
     *   <li>Check if a file exists without downloading it</li>
     *   <li>Retrieve file metadata (size, content type) for caching decisions</li>
     *   <li>Verify links without transferring large amounts of data</li>
     * </ul>
     * <p>
     * This method implements the following logic:
     * <ol>
     *   <li>Resolves the requested path relative to the root directory</li>
     *   <li>If the path is a directory, attempts to check for "index.html" in that directory</li>
     *   <li>Validates that the resolved path exists and is a file (not a directory)</li>
     *   <li>Retrieves file size using {@link Files#size(Path)}</li>
     *   <li>Detects the MIME type automatically or defaults to "application/octet-stream"</li>
     *   <li>Returns an HTTP 200 response with Content-Length and Content-Type headers, but NO body</li>
     * </ol>
     * <p>
     * The returned response includes the same headers that would be sent in a GET request,
     * allowing clients to make decisions about whether to fetch the full resource.
     *
     * @param request The HTTP HEAD request containing the path of the file to check.
     * @return An HTTPResponse with status 200 containing only headers (Content-Length, Content-Type),
     *         without any message body.
     * @throws NotFoundException If the file is not found, is a directory without index.html,
     *                          or the path is invalid.
     * @throws InternalServerErrorException If an I/O error occurs while accessing file metadata
     *                                      (e.g., permission denied, disk error).
     * @throws Exception If any other unexpected error occurs during request handling.
     */
    @Override
    protected HTTPResponse handleHeadRequest(HTTPRequest request) throws Exception {
        String path = request.getPath();

        Path filePath = Paths.get(rootPath, path);

        if(Files.isDirectory(filePath)){
            filePath = filePath.resolve("index.html");
        }

        if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
            throw new NotFoundException("File not found: " + filePath);
        }

        try {
            long contentLength = Files.size(filePath);

            String mimeType = Files.probeContentType(filePath);

            HTTPResponse response = new HTTPResponse(200, "OK");

            response.setHeader("Content-Length", String.valueOf(contentLength));

            response.setHeader("Content-Type", mimeType != null ? mimeType : "application/octet-stream");

            return response;

        } catch (IOException e) {
            throw new InternalServerErrorException("Failed to read file: " + filePath);
        }
    }
}
