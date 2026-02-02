package tcpframework;

import tcpframework.reqeustHandlers.MethodeBasedHandler;
import tcpframework.reqeustHandlers.RequestHandler;
import tcpframework.reqeustHandlers.StaticFileHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * The RouterConfig class is responsible for managing the routing configuration
 * of the application. It maps URL path patterns to their corresponding request
 * handlers and provides a mechanism to find the appropriate handler for a given
 * HTTP request.
 */
public class RouterConfig {
    private final Map<String, RequestHandler> routes = new HashMap<>();
    private final MethodeBasedHandler defaultHandler;

    /**
     * Constructs a RouterConfig instance with a default static file handler.
     *
     * @param staticPath The root path for serving static files.
     */
    public RouterConfig(String staticPath){
        this.defaultHandler = new StaticFileHandler(staticPath);
    }

    /**
     * Registers a new route with its corresponding request handler.
     *
     * @param pathPattern The URL path pattern for the route.
     * @param handler     The request handler for the route.
     */
    public void register(String pathPattern, RequestHandler handler){
        routes.put(pathPattern, handler);
    }


    /**
     * Finds the appropriate request handler for the given HTTP request.
     *
     * @param request The HTTP request to find a handler for.
     * @return The matching request handler, or the default handler if no match is found.
     */
    public RequestHandler findHandler(HTTPRequest request) {
        String path = request.getPath();

        if (routes.containsKey(path)) {
            return routes.get(path);
        }

        for (String pattern : routes.keySet()) {
            if (matchPattern(path, pattern)) {
                return routes.get(pattern);
            }
        }

        return defaultHandler;
    }

    /**
     * Checks if a given path matches a pattern.
     *
     * @param path    The path to check.
     * @param pattern The pattern to match against.
     * @return True if the path matches the pattern, false otherwise.
     */
    private boolean matchPattern(String path, String pattern) {
        return path.matches(pattern.replace("*", ".*"));
    }
}
