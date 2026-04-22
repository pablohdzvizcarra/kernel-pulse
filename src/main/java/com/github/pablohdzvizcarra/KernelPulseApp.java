package com.github.pablohdzvizcarra;

/**
 * Hello world!
 */
public class KernelPulseApp {
    public static void main(String[] args) {
        System.out.println("Starting Kernel Pulse Application...");
        
        DatabaseManager dbManager = new DatabaseManager();
        Collector collector = new Collector(dbManager);
        
        java.util.concurrent.ScheduledExecutorService scheduler = java.util.concurrent.Executors.newScheduledThreadPool(1);
        // Run every 1 minute
        scheduler.scheduleAtFixedRate(collector, 0, 1, java.util.concurrent.TimeUnit.MINUTES);
        
        System.out.println("Collector scheduled to run every 1 minute. Press Ctrl+C to exit.");
    }
}
