package com.github.pablohdzvizcarra.metric;

import java.util.HashMap;
import java.util.Map;

public class Sample {
    private long timestamp;
    private String metricName;
    private long value;
    private Map<String, String> tags;

    public Sample(long timestamp, String metricName, long value) {
        this.timestamp = timestamp;
        this.metricName = metricName;
        this.value = value;
        this.tags = new HashMap<>();
    }

    public Sample(long timestamp, String metricName, long value, Map<String, String> tags) {
        this.timestamp = timestamp;
        this.metricName = metricName;
        this.value = value;
        this.tags = tags != null ? tags : new HashMap<>();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getMetricName() {
        return metricName;
    }

    public long getValue() {
        return value;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    @Override
    public String toString() {
        return "Sample{" +
                "timestamp=" + timestamp +
                ", metricName='" + metricName + '\'' +
                ", value=" + value +
                ", tags=" + tags +
                '}';
    }
}
