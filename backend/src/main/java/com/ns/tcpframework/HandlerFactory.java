package com.ns.tcpframework;

import org.reflections.Reflections;
import com.ns.tcpframework.reqeustHandlers.RequestHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Factory class for dynamically creating RequestHandler instances by name.
 * <p>
 * This factory uses reflection to discover all {@link RequestHandler} subclasses
 * in the {@code com.ns} package at application startup and caches them for efficient
 * instantiation. Handlers can be retrieved by either their simple class name or
 * fully qualified class name.
 * <p>
 * The factory performs the following operations:
 * <ul>
 *   <li>Scans the {@code com.ns} package for all RequestHandler subclasses at class loading time</li>
 *   <li>Caches discovered handlers in a map for O(1) lookup by name</li>
 *   <li>Provides both simple name (e.g., "StaticFileHandler") and fully qualified name
 *       (e.g., "com.ns.tcpframework.reqeustHandlers.StaticFileHandler") lookup</li>
 *   <li>Instantiates handlers dynamically using reflection with no-arg constructors</li>
 * </ul>
 * <p>
 * This pattern enables configuration-driven handler registration, allowing route
 * configurations to specify handlers by string name rather than requiring hardcoded
 * instantiation.
 * <p>
 * Example usage:
 * <pre>
 * RequestHandler handler = HandlerFactory.getRequestHandlerByName("StaticFileHandler");
 * // or
 * RequestHandler handler = HandlerFactory.getRequestHandlerByName("com.ns.tcpframework.reqeustHandlers.StaticFileHandler");
 * </pre>
 * <p>
 * Thread-safety: This class is thread-safe after initialization. The static cache is
 * populated once during class loading and is read-only thereafter.
 *
 * @see RequestHandler
 * @see ConfigLoader
 */
public class HandlerFactory {

    /**
     * Cache mapping handler class names to their corresponding Class objects.
     * <p>
     * This map contains entries for both simple names (e.g., "StaticFileHandler")
     * and fully qualified names (e.g., "com.ns.tcpframework.reqeustHandlers.StaticFileHandler")
     * of all discovered RequestHandler subclasses.
     * <p>
     * The cache is populated once during class initialization via a static initializer block
     * that uses the Reflections library to scan the {@code com.ns} package and its subpackages
     * for all classes that extend {@link RequestHandler}. For each discovered handler class,
     * two cache entries are created: one using the simple class name (e.g., "StaticFileHandler")
     * and one using the fully qualified class name (e.g., "com.ns.tcpframework.reqeustHandlers.StaticFileHandler").
     * This allows handlers to be referenced by either naming convention in configuration files.
     * <p>
     * The cache remains immutable for the lifetime of the application, providing efficient
     * O(1) lookup performance.
     */
    private static final Map<String, Class<? extends RequestHandler>> handlerCache = new HashMap<>();

    static {
        Reflections reflections = new Reflections("com.ns");
        Set<Class<? extends RequestHandler>> handlers = reflections.getSubTypesOf(RequestHandler.class);

        for (Class<? extends RequestHandler> handler : handlers) {
            String simpleName = handler.getSimpleName();
            handlerCache.put(simpleName, handler);
            handlerCache.put(handler.getName(), handler);
        }
    }

    /**
     * Creates and returns a new instance of a RequestHandler by class name.
     * <p>
     * This method looks up the handler class in the cache using the provided name
     * (which can be either a simple name or fully qualified name) and instantiates
     * it using reflection. Each call creates a new instance of the handler.
     * <p>
     * Supported name formats:
     * <ul>
     *   <li>Simple name: {@code "StaticFileHandler"}</li>
     *   <li>Fully qualified name: {@code "com.ns.tcpframework.reqeustHandlers.StaticFileHandler"}</li>
     * </ul>
     * <p>
     * Requirements for handler classes:
     * <ul>
     *   <li>Must extend {@link RequestHandler}</li>
     *   <li>Must have a public no-argument constructor</li>
     *   <li>Must be located in the {@code com.ns} package or its subpackages</li>
     * </ul>
     *
     * @param className The name of the handler class to instantiate. Can be either the
     *                  simple class name or the fully qualified class name.
     * @return A new instance of the specified RequestHandler class.
     * @throws ClassNotFoundException If no handler class with the given name is found in the cache.
     * @throws NoSuchMethodException If the handler class does not have a no-argument constructor.
     * @throws InstantiationException If the handler class is abstract or cannot be instantiated.
     * @throws IllegalAccessException If the no-argument constructor is not accessible.
     * @throws java.lang.reflect.InvocationTargetException If the constructor throws an exception.
     * @throws Exception If any other error occurs during handler instantiation.
     */
    public static RequestHandler getRequestHandlerByName(String className) throws Exception {
        Class<? extends RequestHandler> handlerClass = handlerCache.get(className);

        if (handlerClass == null) {
            throw new ClassNotFoundException("Handler class not found: " + className);
        }

        return handlerClass.getDeclaredConstructor().newInstance();
    }
}
