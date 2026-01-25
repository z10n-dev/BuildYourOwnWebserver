package com.ns.webserver.handlers;

import org.json.JSONObject;
import tcpframework.HTTPRequest;
import tcpframework.HTTPResponse;
import tcpframework.RequestHandler;

import java.net.Socket;

public class ClientCountHandler extends RequestHandler {
    ClientCounterHandler clientCounterHandler;
    public ClientCountHandler(ClientCounterHandler clientCounterHandler) {
        this.clientCounterHandler = clientCounterHandler;
    }

    @Override
    protected void handleGetRequest(HTTPRequest request, Socket socket) throws Exception {
        int count = clientCounterHandler.clientCount();
        HTTPResponse response = new HTTPResponse(200, "OK");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("count", count);
        response.setBody(jsonObject.toString().getBytes(), "application/json; charset=UTF-8");
        response.send(socket);
    }
}
