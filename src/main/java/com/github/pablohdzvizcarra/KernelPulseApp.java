package com.github.pablohdzvizcarra;

import java.util.logging.Logger;

import com.github.pablohdzvizcarra.collector.DiskIoCollector;
import com.github.pablohdzvizcarra.collector.FreeRamMemoryCollector;
import com.github.pablohdzvizcarra.collector.NetworkBytesCollector;

/**
 * Hello world!
 */
public class KernelPulseApp {
    private static final Logger log = Logger.getLogger(KernelPulseApp.class.getName());

    public static void main(String[] args) {
        log.info("Starting Kernel Pulse Application...");
        
        DatabaseManager dbManager = new DatabaseManager();
        FreeRamMemoryCollector ramCollector = new FreeRamMemoryCollector(dbManager);
        NetworkBytesCollector networkCollector = new NetworkBytesCollector(dbManager);
        DiskIoCollector diskIoCollector = new DiskIoCollector(dbManager);
        
        java.util.concurrent.ScheduledExecutorService scheduler = java.util.concurrent.Executors.newScheduledThreadPool(3);
        // Run every 1 minute
        scheduler.scheduleAtFixedRate(ramCollector, 0, 1, java.util.concurrent.TimeUnit.MINUTES);
        scheduler.scheduleAtFixedRate(networkCollector, 0, 1, java.util.concurrent.TimeUnit.MINUTES);
        scheduler.scheduleAtFixedRate(diskIoCollector, 0, 1, java.util.concurrent.TimeUnit.MINUTES);
        
        log.info("Collectors scheduled to run every 1 minute");
    }
}
