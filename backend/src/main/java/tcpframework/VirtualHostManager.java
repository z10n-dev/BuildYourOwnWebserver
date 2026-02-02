package tcpframework;

import java.util.HashMap;

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
}

