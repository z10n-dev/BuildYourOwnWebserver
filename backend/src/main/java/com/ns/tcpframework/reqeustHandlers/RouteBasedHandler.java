package com.ns.tcpframework.reqeustHandlers;

import com.ns.tcpframework.HTTPRequest;
import com.ns.tcpframework.HTTPResponse;
import com.ns.tcpframework.RouteCommand;

import java.util.HashMap;

public class RouteBasedHandler extends RequestHandler {
    protected HashMap<String, RouteCommand> routes = new HashMap<>();

    @Override
    public HTTPResponse handle(HTTPRequest request) throws Exception {
        HTTPResponse response = null;
        String normalizedPath = request.getRequestHead().substring(0,request.getRequestHead().lastIndexOf("/"))+"/:id";

        if (!routes.containsKey(request.getRequestHead())){
            response = routes.get(normalizedPath).run(request);

        } else {
            response = routes.get(request.getRequestHead()).run(request);
        }

        return response;
    }
}
