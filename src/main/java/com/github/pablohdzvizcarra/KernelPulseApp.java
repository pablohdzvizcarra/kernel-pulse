package com.github.pablohdzvizcarra;

import java.util.logging.Logger;

/**
 * Hello world!
 */
public class KernelPulseApp {
    private static final Logger log = Logger.getLogger(KernelPulseApp.class.getName());

    public static void main(String[] args) {
        log.info("Starting Kernel Pulse Application...");
        
        DatabaseManager dbManager = new DatabaseManager();
        Collector collector = new Collector(dbManager);
        
        java.util.concurrent.ScheduledExecutorService scheduler = java.util.concurrent.Executors.newScheduledThreadPool(1);
        // Run every 1 minute
        scheduler.scheduleAtFixedRate(collector, 0, 1, java.util.concurrent.TimeUnit.MINUTES);
        
        log.info("Collector scheduled to run every 1 minute");
    }
}
