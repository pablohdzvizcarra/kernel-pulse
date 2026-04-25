package com.github.pablohdzvizcarra;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.pablohdzvizcarra.metric.Sample;

public class DatabaseManager {
    private static final String DB_NAME = "metrics.db";

    public DatabaseManager() {
        initDatabase();
    }

    private void initDatabase() {
        String query = "CREATE TABLE IF NOT EXISTS metrics (timestamp INTEGER, metric_name TEXT, value REAL, tags TEXT); " +
                       "CREATE INDEX IF NOT EXISTS idx_metrics_name_time ON metrics(metric_name, timestamp);";
        executeSql(query);
    }

    public void insertSample(Sample sample) {
        String tagsJson = serializeTags(sample.getTags());
        String query = String.format("INSERT INTO metrics (timestamp, metric_name, value, tags) VALUES (%d, '%s', %d, '%s');",
                sample.getTimestamp(), sample.getMetricName(), sample.getValue(), tagsJson);
        executeSql(query);
    }

    public List<Sample> getSamples(long startTime, long endTime) {
        List<Sample> samples = new ArrayList<>();
        String query = String.format("SELECT timestamp, metric_name, value, tags FROM metrics WHERE timestamp BETWEEN %d AND %d ORDER BY timestamp ASC;",
                startTime, endTime);
        
        try {
            ProcessBuilder pb = new ProcessBuilder("sqlite3", DB_NAME, query);
            Process process = pb.start();
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 3) {
                    long timestamp = Long.parseLong(parts[0]);
                    String metricName = parts[1];
                    long value = (long) Double.parseDouble(parts[2]);
                    Map<String, String> tags = new HashMap<>();
                    if (parts.length == 4) {
                        tags = deserializeTags(parts[3]);
                    }
                    samples.add(new Sample(timestamp, metricName, value, tags));
                }
            }
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return samples;
    }

    /**
     * Get samples for a specific metric within a time range, optionally filtered by tags.
     * Uses the composite index (metric_name, timestamp) for efficient querying.
     * Tag matching is performed in Java since SQLite doesn't support native JSON queries.
     *
     * @param metricName the metric name to filter by (e.g., "network_bytes_read")
     * @param tags       optional tags to match (e.g., {"interface": "enp0s6"}). Pass null or empty map to skip tag filtering.
     * @param startTime  start of the time range (epoch milliseconds, inclusive)
     * @param endTime    end of the time range (epoch milliseconds, inclusive)
     * @return ordered list of matching samples (ascending by timestamp)
     */
    public List<Sample> getSamplesByMetric(String metricName, Map<String, String> tags,
                                            long startTime, long endTime) {
        List<Sample> samples = new ArrayList<>();
        String query = String.format(
                "SELECT timestamp, metric_name, value, tags FROM metrics WHERE metric_name = '%s' AND timestamp BETWEEN %d AND %d ORDER BY timestamp ASC;",
                metricName, startTime, endTime);

        try {
            ProcessBuilder pb = new ProcessBuilder("sqlite3", DB_NAME, query);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 3) {
                    long timestamp = Long.parseLong(parts[0]);
                    String name = parts[1];
                    long value = (long) Double.parseDouble(parts[2]);
                    Map<String, String> sampleTags = new HashMap<>();
                    if (parts.length == 4) {
                        sampleTags = deserializeTags(parts[3]);
                    }

                    // Filter by tags in Java if tags were specified
                    if (tags != null && !tags.isEmpty()) {
                        if (!matchesTags(sampleTags, tags)) {
                            continue;
                        }
                    }

                    samples.add(new Sample(timestamp, name, value, sampleTags));
                }
            }
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return samples;
    }

    /**
     * Checks if a sample's tags contain all the required tag key-value pairs.
     */
    private boolean matchesTags(Map<String, String> sampleTags, Map<String, String> requiredTags) {
        for (Map.Entry<String, String> entry : requiredTags.entrySet()) {
            String sampleValue = sampleTags.get(entry.getKey());
            if (sampleValue == null || !sampleValue.equals(entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    private String serializeTags(Map<String, String> tags) {
        if (tags == null || tags.isEmpty()) return "{}";
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, String> entry : tags.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\"");
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    private Map<String, String> deserializeTags(String json) {
        Map<String, String> tags = new HashMap<>();
        if (json == null || json.length() <= 2) return tags;
        String content = json.substring(1, json.length() - 1);
        if (content.isEmpty()) return tags;
        String[] pairs = content.split(",");
        for (String pair : pairs) {
            String[] kv = pair.split(":");
            if (kv.length == 2) {
                String key = kv[0].replace("\"", "").trim();
                String value = kv[1].replace("\"", "").trim();
                tags.put(key, value);
            }
        }
        return tags;
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
