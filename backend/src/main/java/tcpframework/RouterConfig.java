package tcpframework;

import java.util.HashMap;
import java.util.Map;

public class RouterConfig {
    private final Map<String,RequestHandler> routes = new HashMap<>();
    private final RequestHandler defaultHandler;

    public RouterConfig(String staticPath){
        this.defaultHandler = new StaticFileHandler(staticPath);
    }

    public void register(String pathPattern, RequestHandler handler){
        routes.put(pathPattern, handler);
    }

    public RequestHandler findHandler(HTTPRequest request) {
        String path = request.getPath();

        if (routes.containsKey(path)) {
            return routes.get(path);
        }

        for (String pattern : routes.keySet()) {
            if (mathPattern(path, pattern)) {
                return routes.get(pattern);
            }
        }

        return defaultHandler;
    }

    private boolean mathPattern(String path, String pattern) {
        return path.matches(pattern.replace("*", ".*"));
    }
}
