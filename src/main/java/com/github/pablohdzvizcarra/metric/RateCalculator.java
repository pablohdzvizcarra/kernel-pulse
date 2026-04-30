package com.github.pablohdzvizcarra.metric;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for computing the Rate (per second) between consecutive metric samples.
 * Uses DeltaCalculator to compute the differences and then calculates the rate
 * over the elapsed time.
 */
public class RateCalculator {

    private RateCalculator() {
    }

    /**
     * Computes the rate per second between two samples.
     * Rate = Delta / (Elapsed Time in seconds)
     *
     * @param previous the previous sample
     * @param current  the current sample
     * @param type     the metric type (COUNTER or GAUGE)
     * @return the rate per second, or 0.0 if elapsed time is zero
     */
    public static double computeRatePerSecond(Sample previous, Sample current, MetricType type) {
        long delta = DeltaCalculator.computeDelta(previous.getValue(), current.getValue(), type);
        long elapsedMs = current.getTimestamp() - previous.getTimestamp();

        if (elapsedMs == 0) {
            return 0.0;
        }
        return (double) delta / (elapsedMs / 1000.0);
    }
    /**
     * Computes the rate per second given a delta and the elapsed time in milliseconds.
     *
     * @param delta     the computed difference between two samples
     * @param elapsedMs the elapsed time in milliseconds
     * @return the rate per second, or 0.0 if elapsed time is zero
     */
    public static double computeRatePerSecond(long delta, long elapsedMs) {
        if (elapsedMs == 0) {
            return 0.0;
        }
        return (double) delta / (elapsedMs / 1000.0);
    }

    /**
     * Computes a list of rates from a list of consecutive samples.
     *
     * @param samples ordered list of samples (same metric + tags)
     * @param type    the metric type (COUNTER or GAUGE)
     * @return list of rates representing the change per second
     */
    public static List<Double> computeRates(List<Sample> samples, MetricType type) {
        List<DeltaResult> deltas = DeltaCalculator.computeDeltas(samples, type);
        List<Double> rates = new ArrayList<>(deltas.size());
        
        for (DeltaResult result : deltas) {
            rates.add(result.getRatePerSecond());
        }
        
        return rates;
    }
}
