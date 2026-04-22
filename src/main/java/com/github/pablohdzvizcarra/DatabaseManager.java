package com.github.pablohdzvizcarra;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String DB_NAME = "metrics.db";

    public DatabaseManager() {
        initDatabase();
    }

    private void initDatabase() {
        String query = "CREATE TABLE IF NOT EXISTS memory_samples (timestamp INTEGER PRIMARY KEY, metric_name TEXT, value REAL); " +
                       "CREATE INDEX IF NOT EXISTS idx_timestamp ON memory_samples(timestamp);";
        executeSql(query);
    }

    public void insertSample(Sample sample) {
        String query = String.format("INSERT INTO memory_samples (timestamp, metric_name, value) VALUES (%d, '%s', %f);",
                sample.getTimestamp(), sample.getMetricName(), sample.getValue());
        executeSql(query);
    }

    public List<Sample> getSamples(long startTime, long endTime) {
        List<Sample> samples = new ArrayList<>();
        String query = String.format("SELECT timestamp, metric_name, value FROM memory_samples WHERE timestamp BETWEEN %d AND %d ORDER BY timestamp ASC;",
                startTime, endTime);
        
        try {
            ProcessBuilder pb = new ProcessBuilder("sqlite3", DB_NAME, query);
            Process process = pb.start();
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 3) {
                    long timestamp = Long.parseLong(parts[0]);
                    String metricName = parts[1];
                    double value = Double.parseDouble(parts[2]);
                    samples.add(new Sample(timestamp, metricName, value));
                }
            }
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return samples;
    }

    private void executeSql(String query) {
        try {
            ProcessBuilder pb = new ProcessBuilder("sqlite3", DB_NAME, query);
            Process process = pb.start();
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
