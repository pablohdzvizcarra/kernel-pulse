package com.github.pablohdzvizcarra;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Collector implements Runnable {
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
                    if (parts.length >= 2) {
                        double freeMemKb = Double.parseDouble(parts[1]);
                        Sample sample = new Sample(System.currentTimeMillis(), "memory_free", freeMemKb);
                        dbManager.insertSample(sample);
                        System.out.println("Collector: Inserted " + sample);
                    }
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Collector failed to read /proc/meminfo: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Collector encountered an error: " + e.getMessage());
        }
    }
}
