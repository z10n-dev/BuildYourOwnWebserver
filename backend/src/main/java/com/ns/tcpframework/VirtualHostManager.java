package com.ns.tcpframework;

import com.ns.tcpframework.reqeustHandlers.sse.SSEHandler;

import java.util.HashMap;
import java.util.Map;

public class VirtualHostManager {
    private final HashMap<String, VirtualHostConfig> virtualHosts;
    private final VirtualHostConfig defaultVirtualHost;

    public VirtualHostManager(VirtualHostConfig defaultVirtualHost) {
        this.virtualHosts = new HashMap<>();
        this.defaultVirtualHost = defaultVirtualHost;
    }

    public void registerVirtualHost(VirtualHostConfig vhost) {
        virtualHosts.put(vhost.getHost().toLowerCase(), vhost);
    }

    public VirtualHostConfig getVirtualHost(String host) {
        return virtualHosts.getOrDefault(host.toLowerCase(), defaultVirtualHost);
    }

    public Map<String, VirtualHostConfig> getVirtualHosts() {
        return virtualHosts;
    }

    public SSEHandler getSSEHandler() {
        for (Map.Entry<String, VirtualHostConfig> entry : virtualHosts.entrySet()) {
            if (entry.getValue().getSSEHandler() != null) {
                return entry.getValue().getSSEHandler();
            }
        }

        return null;
    }
}

