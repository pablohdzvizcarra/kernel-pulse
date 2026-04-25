package com.github.pablohdzvizcarra;

import java.util.logging.Logger;

import com.github.pablohdzvizcarra.collector.FreeRamMemoryCollector;

/**
 * Hello world!
 */
public class KernelPulseApp {
    private static final Logger log = Logger.getLogger(KernelPulseApp.class.getName());

    public static void main(String[] args) {
        log.info("Starting Kernel Pulse Application...");
        
        DatabaseManager dbManager = new DatabaseManager();
        FreeRamMemoryCollector collector = new FreeRamMemoryCollector(dbManager);
        
        java.util.concurrent.ScheduledExecutorService scheduler = java.util.concurrent.Executors.newScheduledThreadPool(1);
        // Run every 1 minute
        scheduler.scheduleAtFixedRate(collector, 0, 1, java.util.concurrent.TimeUnit.MINUTES);
        
        log.info("Collector scheduled to run every 1 minute");
    }
}
