package com.github.pablohdzvizcarra.metric;

import java.util.ArrayList;
import java.util.List;

/**
 * Stateless utility class for computing deltas between consecutive metric samples.
 *
 * <p>Deltas are computed differently depending on the metric type:</p>
 * <ul>
 *   <li><b>COUNTER:</b> Result is always &ge; 0. Wraparound is handled when
 *       current value is less than the previous value.</li>
 *   <li><b>GAUGE:</b> Result can be negative (e.g., memory consumed).
 *       No wraparound logic is applied.</li>
 * </ul>
 *
 * <p>This class does not store state. Raw samples are preserved in the database,
 * and deltas are computed on-the-fly from any pair of samples.</p>
 */
public class DeltaCalculator {

    /**
     * Maximum value for the wraparound formula.
     * Linux /proc/net/dev uses unsigned 64-bit counters, but Java's long is
     * signed 64-bit. We use Long.MAX_VALUE as the practical ceiling.
     * At 10 Gbps, it would take ~29 years to wrap around.
     */
    private static final long COUNTER_MAX_VALUE = Long.MAX_VALUE;

    // Prevent instantiation
    private DeltaCalculator() {
    }

    /**
     * Computes the delta between two sample values based on metric type.
     *
     * <p>For COUNTER metrics: if current &ge; previous, returns the simple
     * difference. If current &lt; previous (wraparound), applies the formula:
     * (MAX_VALUE - previous) + current + 1.</p>
     *
     * <p>For GAUGE metrics: returns the simple difference (current - previous),
     * which can be negative.</p>
     *
     * @param previous the previous sample value
     * @param current  the current sample value
     * @param type     the metric type (COUNTER or GAUGE)
     * @return the computed delta
     */
    public static long computeDelta(long previous, long current, MetricType type) {
        if (type == MetricType.COUNTER) {
            if (current >= previous) {
                return current - previous;
            }
            // Wraparound detected: counter rolled past its maximum
            return (COUNTER_MAX_VALUE - previous) + current + 1;
        }

        // GAUGE: simple difference, can be negative
        return current - previous;
    }

    /**
     * Computes a list of DeltaResult objects from consecutive samples.
     * Each DeltaResult represents the delta between sample[i-1] and sample[i].
     *
     * <p>The input list must be ordered by timestamp (ascending) and must
     * contain samples for the same metric and tag set.</p>
     *
     * @param samples ordered list of samples (same metric + tags)
     * @param type    the metric type (COUNTER or GAUGE)
     * @return list of DeltaResult objects (size = samples.size() - 1),
     *         or empty list if fewer than 2 samples
     */
    public static List<DeltaResult> computeDeltas(List<Sample> samples, MetricType type) {
        if (samples == null || samples.size() < 2) {
            return new ArrayList<>();
        }

        List<DeltaResult> deltas = new ArrayList<>(samples.size() - 1);

        for (int i = 1; i < samples.size(); i++) {
            Sample prev = samples.get(i - 1);
            Sample curr = samples.get(i);

            long delta = computeDelta(prev.getValue(), curr.getValue(), type);
            long elapsedMs = curr.getTimestamp() - prev.getTimestamp();

            deltas.add(new DeltaResult(
                    prev.getTimestamp(),
                    curr.getTimestamp(),
                    delta,
                    elapsedMs
            ));
        }

        return deltas;
    }
}
