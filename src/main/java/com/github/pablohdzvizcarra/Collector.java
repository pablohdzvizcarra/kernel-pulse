package com.github.pablohdzvizcarra;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Logger;

public class Collector implements Runnable {
    private static final Logger log = Logger.getLogger(Collector.class.getName());
    private final DatabaseManager dbManager;

    public Collector(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new FileReader("/proc/meminfo"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("MemFree:")) {
                    String[] parts = line.trim().split("\\s+");
                    System.out.println("DEBUG: " + Arrays.toString(parts));
                    if (parts.length >= 2) {
                        long freeMemKb = Long.parseLong(parts[1]);
                        Sample sample = new Sample(System.currentTimeMillis(), "memory_free", freeMemKb);
                        dbManager.insertSample(sample);
                        log.info(() -> "Collector: Inserted " + sample);
                    }
                    break;
                }
            }
        } catch (IOException e) {
            log.severe(() -> "Collector failed to read /proc/meminfo: " + e.getMessage());
        } catch (Exception e) {
            log.severe(() -> "Collector encountered an error: " + e.getMessage());
        }
    }
}
