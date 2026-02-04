package com.ns.tcpframework;

import org.reflections.Reflections;
import com.ns.tcpframework.reqeustHandlers.RequestHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HandlerFactory {

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

    public static RequestHandler getRequestHandlerByName(String className) throws Exception {
        Class<? extends RequestHandler> handlerClass = handlerCache.get(className);

        if (handlerClass == null) {
            throw new ClassNotFoundException("Handler class not found: " + className);
        }

        return handlerClass.getDeclaredConstructor().newInstance();
    }
}
