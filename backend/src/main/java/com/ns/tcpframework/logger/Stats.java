package com.ns.tcpframework.logger;

import org.json.JSONObject;
import oshi.hardware.GlobalMemory;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.concurrent.atomic.AtomicLong;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

public class Stats implements Runnable{
    private static Stats instance;
    private AtomicLong startTime = new AtomicLong(System.currentTimeMillis());
    private AtomicLong totalRequests = new AtomicLong();
    private AtomicLong activeConnections = new AtomicLong();
    private OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
    private volatile boolean running = true;


    public static void init() {
        if (instance == null) {
            instance = new Stats();
        }
        Thread.ofPlatform().name("Stats-Trhead").start(instance);
    }
    public static Stats getInstance() {
        if (instance == null) {
            init();
        }
        return instance;
    }
    public JSONObject getStatsAsJson() throws InterruptedException {
        final JSONObject obj = new JSONObject();
        obj.put("uptime", System.currentTimeMillis() - startTime.get());
        obj.put("totalRequests", totalRequests);
        obj.put("activeConnections", activeConnections);
        obj.put("cpuUsage", getCPUUsage());
        obj.put("memoryUsage", getMemUsage());
        return obj;
    }

    public void incrementRequests() {
        totalRequests.incrementAndGet();
    }

    public void setActiveConnections(AtomicLong activeConnections) {
        this.activeConnections = activeConnections;
    }

    private double getCPUUsage() throws InterruptedException {
        SystemInfo systemInfo = new SystemInfo();
        CentralProcessor processor = systemInfo.getHardware().getProcessor();

        return processor.getSystemCpuLoad(1000);

    }

    private double getMemUsage() {
        SystemInfo systemInfo = new SystemInfo();
        GlobalMemory memory = systemInfo.getHardware().getMemory();

        return (double) (memory.getTotal() - memory.getAvailable()) / (1000000000.0);
    }

    private void updateStats() {
        this.activeConnections = new AtomicLong();

    }


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
