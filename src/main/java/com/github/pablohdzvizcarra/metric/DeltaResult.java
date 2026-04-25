package com.github.pablohdzvizcarra.metric;

/**
 * Holds the result of a delta computation between two consecutive samples.
 * Contains the delta value, the time window, and can compute the rate of change.
 */
public class DeltaResult {
    private final long startTimestamp;
    private final long endTimestamp;
    private final long delta;
    private final long elapsedMs;

    public DeltaResult(long startTimestamp, long endTimestamp, long delta, long elapsedMs) {
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
        this.delta = delta;
        this.elapsedMs = elapsedMs;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public long getEndTimestamp() {
        return endTimestamp;
    }

    public long getDelta() {
        return delta;
    }

    public long getElapsedMs() {
        return elapsedMs;
    }

    /**
     * Computes the rate of change per second.
     * For counters, this gives you e.g., bytes/second.
     * For gauges, this gives you e.g., kB freed per second.
     *
     * @return the rate per second, or 0.0 if elapsed time is zero
     */
    public double getRatePerSecond() {
        if (elapsedMs == 0) {
            return 0.0;
        }
        return (double) delta / (elapsedMs / 1000.0);
    }

    @Override
    public String toString() {
        return "DeltaResult{" +
                "startTimestamp=" + startTimestamp +
                ", endTimestamp=" + endTimestamp +
                ", delta=" + delta +
                ", elapsedMs=" + elapsedMs +
                ", ratePerSecond=" + String.format("%.2f", getRatePerSecond()) +
                '}';
    }
}
