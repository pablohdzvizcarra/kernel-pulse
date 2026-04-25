package com.github.pablohdzvizcarra.metric;

/**
 * Classifies a metric as either a cumulative counter or a point-in-time gauge.
 *
 * <ul>
 *   <li><b>COUNTER</b> — Monotonically increasing value (e.g., total network bytes).
 *       Delta is always &ge; 0 and represents work performed in an interval.
 *       Wraparound handling is applied when current &lt; previous.</li>
 *   <li><b>GAUGE</b> — Snapshot of current state (e.g., free RAM).
 *       Delta can be negative and represents change in state.
 *       No wraparound logic is applied.</li>
 * </ul>
 */
public enum MetricType {
    COUNTER,
    GAUGE;

    /**
     * Returns the MetricType for a given metric name.
     * Defaults to GAUGE if the metric name is not recognized.
     *
     * @param metricName the name of the metric (e.g., "network_bytes_read")
     * @return the corresponding MetricType
     */
    public static MetricType forMetric(String metricName) {
        return switch (metricName) {
            case "network_bytes_read", "network_bytes_written" -> COUNTER;
            case "memory_free" -> GAUGE;
            default -> GAUGE;
        };
    }
}
