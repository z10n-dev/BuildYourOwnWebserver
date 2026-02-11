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

/**
 * Utility class for loading server configuration from YAML files.
 * <p>
 * This class provides functionality to load environment-specific server configurations
 * from YAML files. It supports loading from both external file system paths and
 * classpath resources, with priority given to external configurations.
 * <p>
 * Configuration file location:
 * <ul>
 *   <li>External: {@code config/config.{environment}.yaml} (file system)</li>
 *   <li>Classpath: {@code config/config.{environment}.yaml} (resources)</li>
 * </ul>
 * <p>
 * The loader uses Jackson's YAML parser to deserialize configuration files into
 * structured {@link ServerConfig} objects, including virtual host configurations
 * and route mappings.
 * <p>
 * Example usage:
 * <pre>
 * ServerConfig config = ConfigLoader.load("production");
 * // Loads from config/config.production.yaml
 * </pre>
 *
 * @see ServerConfig
 * @see VirtualHostConfig
 */
public class ConfigLoader {

    /**
     * Jackson ObjectMapper configured for YAML parsing.
     * <p>
     * This mapper is used to deserialize YAML configuration files into Java objects.
     * It is initialized with {@link YAMLFactory} to enable YAML format support.
     */
    private static final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    /**
     * Loads server configuration for the specified environment.
     * <p>
     * This method attempts to load configuration in the following order:
     * <ol>
     *   <li>External file system path: {@code config/config.{environment}.yaml}</li>
     *   <li>Classpath resource: {@code config/config.{environment}.yaml}</li>
     * </ol>
     * <p>
     * The configuration file should contain:
     * <ul>
     *   <li>environment: The environment name (e.g., "development", "production")</li>
     *   <li>port: The server port number</li>
     *   <li>logLevel: The logging level (DEBUG, INFO, WARN, ERROR, FATAL)</li>
     *   <li>defaultHost: The name of the default virtual host</li>
     *   <li>hosts: A map of virtual host configurations with routes</li>
     * </ul>
     *
     * @param environment The environment name used to locate the configuration file
     *                   (e.g., "development", "staging", "production").
     * @return A {@link ServerConfig} object containing all parsed configuration data.
     * @throws IOException If an error occurs while reading or parsing the configuration file.
     * @throws IllegalArgumentException If the configuration file cannot be found in either
     *                                 the file system or classpath.
     * @throws RuntimeException If an error occurs during handler initialization or
     *                         route registration.
     */
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

    /**
     * Builds a map of virtual host configurations from the raw host data.
     * <p>
     * This method processes the parsed YAML host data and creates fully initialized
     * {@link VirtualHostConfig} objects with registered routes. For each host:
     * <ol>
     *   <li>Creates a VirtualHostConfig with the specified document root</li>
     *   <li>Registers each route by instantiating the corresponding handler class</li>
     *   <li>Maps the host name to the completed configuration</li>
     * </ol>
     * <p>
     * Route handlers are instantiated using {@link HandlerFactory#getRequestHandlerByName(String)},
     * which dynamically creates handler instances based on class names specified in the configuration.
     *
     * @param hosts A map of host names to their raw configuration data from the YAML file.
     * @return A map of host names to fully initialized {@link VirtualHostConfig} objects
     *         with all routes registered.
     * @throws RuntimeException If an error occurs during handler instantiation or route
     *                         registration (wraps the underlying exception).
     */
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

    /**
     * Internal data structure for deserializing the root-level YAML configuration.
     * <p>
     * This class maps directly to the structure of the YAML configuration file
     * and is used by Jackson for deserialization. It contains all top-level
     * configuration properties.
     */
    private static class ConfigData{
        /** The environment name (e.g., "development", "production"). */
        public String environment;

        /** The server port number. */
        public int port;

        /** The logging level as a string (e.g., "DEBUG", "INFO", "WARN"). */
        public String logLevel;

        /** The name of the default virtual host. */
        public String defaultHost;

        /** Map of virtual host names to their configuration data. */
        public Map<String, HostData> hosts;
    }

    /**
     * Internal data structure for deserializing virtual host configuration from YAML.
     * <p>
     * This class represents the configuration for a single virtual host,
     * including its document root and route mappings. It is used by Jackson
     * for deserialization as part of the {@link ConfigData} structure.
     */
    private static class HostData{
        /** The document root path for serving static files. */
        public String documentRoot;

        /** Map of URL paths to handler class names for route registration. */
        public Map<String, String> routes;
    }
}


