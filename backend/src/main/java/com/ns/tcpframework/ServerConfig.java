package com.ns.tcpframework;

import com.ns.tcpframework.logger.Loglevel;

import java.util.Map;

public class ServerConfig {
    private String environment;
    private int port;
    private Loglevel loglevel;
    private VirtualHostConfig defaultHost;
    private Map<String, VirtualHostConfig> hosts;

    public ServerConfig(String environment, int port, Loglevel loglevel, VirtualHostConfig defaultHost, Map<String, VirtualHostConfig> hosts) {
        this.environment = environment;
        this.port = port;
        this.loglevel = loglevel;
        this.defaultHost = defaultHost;
        this.hosts = hosts;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Loglevel getLoglevel() {
        return loglevel;
    }

    public void setLoglevel(Loglevel loglevel) {
        this.loglevel = loglevel;
    }

    public Map<String, VirtualHostConfig> getHosts() {
        return hosts;
    }

    public void setHosts(Map<String, VirtualHostConfig> hosts) {
        this.hosts = hosts;
    }

    public VirtualHostConfig getDefaultHost() {
        return defaultHost;
    }
}