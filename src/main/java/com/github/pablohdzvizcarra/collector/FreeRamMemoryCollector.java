package com.github.pablohdzvizcarra.collector;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Logger;

import com.github.pablohdzvizcarra.DatabaseManager;
import com.github.pablohdzvizcarra.metric.Sample;


public class FreeRamMemoryCollector implements Collector {
    private static final Logger log = Logger.getLogger(FreeRamMemoryCollector.class.getName());
    private final DatabaseManager dbManager;

    public FreeRamMemoryCollector(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public void run() {
        Sample sample = generateSample();

        if (sample != null) {
            dbManager.insertSample(sample);
            log.info(() -> "Collector: Inserted " + sample);
        } else {
            log.warning(() -> "Collector: Failed to generate sample");
        }
    }
    
    @Override
    public Sample generateSample() {
        Sample sample = null;
        
        try (BufferedReader reader = new BufferedReader(new FileReader("/proc/meminfo"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("MemFree:")) {
                    String[] parts = line.trim().split("\\s+");
                    System.out.println("DEBUG: " + Arrays.toString(parts));
                    if (parts.length >= 2) {
                        long freeMemKb = Long.parseLong(parts[1]);
                        sample = new Sample(System.currentTimeMillis(), "memory_free", freeMemKb);
                        break;
                    }
                }
            }
        } catch (IOException e) {
            log.severe(() -> "Collector failed to read /proc/meminfo: " + e.getMessage());
        } catch (Exception e) {
            log.severe(() -> "Collector encountered an error: " + e.getMessage());
        }

        return sample;
    }
}
