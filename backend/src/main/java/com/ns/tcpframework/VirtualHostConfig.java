package com.ns.tcpframework;

import com.ns.tcpframework.reqeustHandlers.sse.SSEHandler;

public class VirtualHostConfig {
    private final String host;
    private final String documentRoot;
    private final RouterConfig router;

    public VirtualHostConfig(String host, String documentRoot) {
        this.host = host;
        this.documentRoot = documentRoot;
        this.router = new RouterConfig(documentRoot);
    }

    public String getHost() {
        return host;
    }

    public String getDocumentRoot() {
        return documentRoot;
    }

    public RouterConfig getRouter() {
        return router;
    }

    public SSEHandler getSSEHandler() {
        return router.getSSEHandler();
    }
}
