package com.ns.tcpframework.logger;

import org.json.JSONObject;
import oshi.hardware.GlobalMemory;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.concurrent.atomic.AtomicLong;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

/**
 * A singleton statistics collector that tracks server performance metrics.
 * <p>
 * This class collects and provides access to various server statistics including:
 * <ul>
 *   <li>Server uptime</li>
 *   <li>Total number of requests processed</li>
 *   <li>Number of active connections</li>
 *   <li>CPU usage percentage</li>
 *   <li>Memory usage in GB</li>
 * </ul>
 * <p>
 * The Stats class runs on a separate platform thread and uses the OSHI library
 * to collect system-level metrics such as CPU and memory usage.
 */
public class Stats implements Runnable{
    /** The singleton instance of the Stats collector. */
    private static Stats instance;

    /** The timestamp (in milliseconds) when the server started. */
    private AtomicLong startTime = new AtomicLong(System.currentTimeMillis());

    /** The total number of requests processed since server start. Thread-safe counter. */
    private AtomicLong totalRequests = new AtomicLong();

    /** The current number of active client connections. Thread-safe counter. */
    private AtomicLong activeConnections = new AtomicLong();

    /** MXBean for accessing operating system information. */
    private OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();

    /** Flag indicating whether the stats collection thread should continue running. */
    private volatile boolean running = true;

    /**
     * Initializes the singleton Stats instance and starts its monitoring thread.
     * <p>
     * This method creates a new Stats instance if one doesn't exist and starts
     * a platform thread to run the stats collector. The thread is named "Stats-Trhead"
     * (note: typo in original code).
     * <p>
     * This method should be called once during application startup.
     */
    public static void init() {
        if (instance == null) {
            instance = new Stats();
        }
        Thread.ofPlatform().name("Stats-Trhead").start(instance);
    }

    /**
     * Returns the singleton instance of the Stats collector.
     * <p>
     * If the instance has not been initialized, this method will call {@link #init()}
     * to create and start it automatically.
     *
     * @return The Stats singleton instance.
     */
    public static Stats getInstance() {
        if (instance == null) {
            init();
        }
        return instance;
    }

    /**
     * Retrieves all current server statistics as a JSON object.
     * <p>
     * The returned JSON object contains the following fields:
     * <ul>
     *   <li><b>uptime</b>: Server uptime in milliseconds</li>
     *   <li><b>totalRequests</b>: Total number of requests processed</li>
     *   <li><b>activeConnections</b>: Current number of active connections</li>
     *   <li><b>cpuUsage</b>: Current CPU usage as a percentage (0.0 to 1.0)</li>
     *   <li><b>memoryUsage</b>: Current memory usage in gigabytes</li>
     * </ul>
     *
     * @return A JSONObject containing all server statistics.
     * @throws InterruptedException if the CPU usage measurement is interrupted.
     */
    public JSONObject getStatsAsJson() throws InterruptedException {
        final JSONObject obj = new JSONObject();
        obj.put("uptime", System.currentTimeMillis() - startTime.get());
        obj.put("totalRequests", totalRequests);
        obj.put("activeConnections", activeConnections);
        obj.put("cpuUsage", getCPUUsage());
        obj.put("memoryUsage", getMemUsage());
        return obj;
    }

    /**
     * Increments the total request counter by one.
     * <p>
     * This method is thread-safe and should be called whenever a new request is processed.
     */
    public void incrementRequests() {
        totalRequests.incrementAndGet();
    }

    /**
     * Sets the active connections counter to a new value.
     * <p>
     * This method replaces the internal active connections counter with the provided value.
     * It's typically used to synchronize the stats with an external connection tracker.
     *
     * @param activeConnections The new AtomicLong counter for active connections.
     */
    public void setActiveConnections(AtomicLong activeConnections) {
        this.activeConnections = activeConnections;
    }

    /**
     * Retrieves the current CPU usage as a decimal percentage.
     * <p>
     * This method uses the OSHI library to measure system CPU load over a 1-second interval.
     * The returned value ranges from 0.0 (0% usage) to 1.0 (100% usage).
     *
     * @return The current CPU usage as a decimal percentage (0.0 to 1.0).
     * @throws InterruptedException if the measurement is interrupted during the 1-second sampling period.
     */
    private double getCPUUsage() throws InterruptedException {
        SystemInfo systemInfo = new SystemInfo();
        CentralProcessor processor = systemInfo.getHardware().getProcessor();

        return processor.getSystemCpuLoad(1000);
    }

    /**
     * Retrieves the current memory usage in gigabytes.
     * <p>
     * This method uses the OSHI library to calculate memory usage by subtracting
     * available memory from total memory. The result is converted from bytes to gigabytes.
     *
     * @return The current memory usage in gigabytes.
     */
    private double getMemUsage() {
        SystemInfo systemInfo = new SystemInfo();
        GlobalMemory memory = systemInfo.getHardware().getMemory();

        return (double) (memory.getTotal() - memory.getAvailable()) / (1000000000.0);
    }

    /**
     * Updates the statistics counters.
     * <p>
     * Currently, this method resets the active connections counter to a new AtomicLong.
     * This appears to be unused or placeholder code that may be intended for future
     * periodic statistics updates.
     *
     * @deprecated This method appears to be unused and its purpose is unclear.
     */
    private void updateStats() {
        this.activeConnections = new AtomicLong();
    }

    /**
     * Main execution loop for the stats collector thread.
     * <p>
     * Currently, this method simply sleeps indefinitely and does not perform any
     * periodic statistics collection. The thread will only wake up if interrupted
     * or if the running flag is set to false.
     * <p>
     * Note: This implementation suggests that statistics are collected on-demand
     * via {@link #getStatsAsJson()} rather than on a periodic schedule.
     */
    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
