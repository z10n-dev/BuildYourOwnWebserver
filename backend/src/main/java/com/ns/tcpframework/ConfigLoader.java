package com.ns.tcpframework;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.ns.tcpframework.logger.LogDestination;
import com.ns.tcpframework.logger.Loglevel;
import com.ns.tcpframework.logger.ServerLogger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class ConfigLoader {

    private static final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    public static ServerConfig load(String environment) throws IOException {

        String configPath = "config/config." + environment + ".yaml";
        java.io.File externalConfig = new File(configPath);
        InputStream in;

        if (externalConfig.exists()) {
            ServerLogger.getInstance().log(Loglevel.INFO, "Loading configuration from external file: " + configPath, LogDestination.EVERYWHERE);
            in = new java.io.FileInputStream(externalConfig);
        } else {
            ServerLogger.getInstance().log(Loglevel.INFO, "External config not found, loading from classpath: " + configPath, LogDestination.EVERYWHERE);
            in = ConfigLoader.class.getClassLoader().getResourceAsStream(configPath);
            if (in == null) {
                throw new IllegalArgumentException("Config file not found! " + configPath);
            }
        }

        try (in) {
            ConfigData data = mapper.readValue(in, ConfigData.class);

            Map<String, VirtualHostConfig> hosts = buildHosts(data.hosts);

            return new ServerConfig(
                    data.environment,
                    data.port,
                    Loglevel.valueOf(data.logLevel.toUpperCase()),
                    hosts.getOrDefault(data.defaultHost, hosts.values().stream().findFirst().orElse(null)),
                    hosts
            );
        }
    }

    private static Map<String, VirtualHostConfig> buildHosts(Map<String, HostData> hosts) {
        return hosts.entrySet().stream().collect(
                java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            HostData hostData = entry.getValue();
                            VirtualHostConfig vhost = new VirtualHostConfig(entry.getKey(), hostData.documentRoot);
                            hostData.routes.forEach((path, handlerClassName) -> {
                                try {
                                    vhost.getRouter().register(path, HandlerFactory.getRequestHandlerByName(handlerClassName));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    throw new RuntimeException(e);
                                }
                            });
                            return vhost;
                        }
                )
        );
    }

    private static class ConfigData{
        public String environment;
        public int port;
        public String logLevel;
        public String defaultHost;
        public Map<String, HostData> hosts;
    }

    private static class HostData{
        public String documentRoot;
        public Map<String, String> routes;
    }
}


