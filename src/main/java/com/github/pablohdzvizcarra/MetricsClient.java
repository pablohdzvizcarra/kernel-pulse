package com.github.pablohdzvizcarra;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import com.github.pablohdzvizcarra.metric.DeltaCalculator;
import com.github.pablohdzvizcarra.metric.DeltaResult;
import com.github.pablohdzvizcarra.metric.MetricType;
import com.github.pablohdzvizcarra.metric.Sample;

public class MetricsClient {
    private final DatabaseManager dbManager;

    public MetricsClient(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public Result getFreeMemory(Calendar start, Calendar end) {
        long startTime = start.getTimeInMillis();
        long endTime = end.getTimeInMillis();
        
        List<Sample> samples = dbManager.getSamples(startTime, endTime);
        
        if (samples.isEmpty()) {
            return new Result(samples, 0.0, 0, 0, 0);
        }

        double sum = 0;
        long max = Long.MIN_VALUE;
        long min = Long.MAX_VALUE;

        for (Sample sample : samples) {
            long val = sample.getValue();
            sum += val;
            if (val > max) max = val;
            if (val < min) min = val;
        }

        double average = sum / samples.size();

        return new Result(samples, average, max, min, samples.size());
    }

    /**
     * Computes deltas for a specific metric over a time range.
     * The metric type (COUNTER or GAUGE) is automatically determined from the metric name.
     *
     * <p>For network metrics, tags should include {"interface": "<name>"} to compute
     * deltas per-interface rather than across all interfaces.</p>
     *
     * @param metricName the metric to compute deltas for (e.g., "network_bytes_read", "memory_free")
     * @param tags       optional tags to filter by (null or empty to skip tag filtering)
     * @param start      start of the time range
     * @param end        end of the time range
     * @return list of DeltaResult objects, one per consecutive sample pair
     */
    public List<DeltaResult> getDeltas(String metricName, Map<String, String> tags,
                                        Calendar start, Calendar end) {
        long startTime = start.getTimeInMillis();
        long endTime = end.getTimeInMillis();

        List<Sample> samples = dbManager.getSamplesByMetric(metricName, tags, startTime, endTime);
        MetricType type = MetricType.forMetric(metricName);

        return DeltaCalculator.computeDeltas(samples, type);
    }
}

