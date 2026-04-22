package com.github.pablohdzvizcarra;

public class Sample {
    private long timestamp;
    private String metricName;
    private long value;

    public Sample(long timestamp, String metricName, long value) {
        this.timestamp = timestamp;
        this.metricName = metricName;
        this.value = value;
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

    @Override
    public String toString() {
        return "Sample{" +
                "timestamp=" + timestamp +
                ", metricName='" + metricName + '\'' +
                ", value=" + value +
                '}';
    }
}
