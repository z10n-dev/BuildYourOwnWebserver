package com.ns.webserver.handlers;

import com.ns.tcpframework.HTTPRequest;
import com.ns.tcpframework.HTTPResponse;
import com.ns.tcpframework.reqeustHandlers.MethodeBasedHandler;

import java.nio.charset.StandardCharsets;

public class HelloWorldHandler extends MethodeBasedHandler {

    @Override
    protected HTTPResponse handleGetRequest(HTTPRequest request) throws Exception {
        HTTPResponse response = new HTTPResponse(200, "OK");
        response.setBody("<h1>Hello, World!</h1>".getBytes(StandardCharsets.UTF_8), "text/html; charset=UTF-8");
        return response;
    }

}
