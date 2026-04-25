# Delta Calculation — Implementation Plan

> **Goal:** Add delta calculation to kernel-pulse so users can compute the difference between consecutive samples for both counter metrics (network bytes) and gauge metrics (free RAM).

## Background

The kernel-pulse application currently collects raw metric values and stores them in SQLite. There is no mechanism to compute the *delta* (difference) between consecutive samples. For counter metrics like `network_bytes_read`, the raw cumulative value is rarely useful — users want "bytes transferred in the last minute." For gauge metrics like `memory_free`, the delta represents "how much memory changed."

See [theory.md](./theory.md) for the full conceptual background and [decisions.md](./decisions.md) for design rationale.

---

## Open Questions

> [!IMPORTANT]
> **Q1:** The current `DatabaseManager.getSamples()` returns all metrics in a time range without filtering by `metric_name` or `tags`. For delta calculation we need per-metric, per-tag-set queries. The plan adds a new `getSamplesByMetric()` method. **Is this approach acceptable, or would you prefer to modify the existing `getSamples()` method?**

> [!NOTE]
> **Q2:** Should the `DeltaResult` objects be stored back into the database (a materialized view approach), or should they always be computed on-the-fly from raw samples? The plan uses on-the-fly computation. Storing deltas would trade storage for query speed.

---

## Proposed Changes

### Component 1: Metric Type System

#### [NEW] [MetricType.java](file:///home/ubuntu/GitHub/kernel-pulse/src/main/java/com/github/pablohdzvizcarra/metric/MetricType.java)

An enum to classify metrics as `COUNTER` or `GAUGE`, with a static registry:

```java
package com.github.pablohdzvizcarra.metric;

public enum MetricType {
    COUNTER,  // Monotonically increasing (e.g., network bytes)
    GAUGE;    // Point-in-time snapshot (e.g., free RAM)

    /**
     * Returns the MetricType for a given metric name.
     * Defaults to GAUGE if unknown.
     */
    public static MetricType forMetric(String metricName) {
        return switch (metricName) {
            case "network_bytes_read", "network_bytes_written" -> COUNTER;
            case "memory_free" -> GAUGE;
            default -> GAUGE;
        };
    }
}
```

---

### Component 2: Delta Calculator

#### [NEW] [DeltaCalculator.java](file:///home/ubuntu/GitHub/kernel-pulse/src/main/java/com/github/pablohdzvizcarra/metric/DeltaCalculator.java)

A stateless utility class with the core delta computation logic:

```java
package com.github.pablohdzvizcarra.metric;

import java.util.ArrayList;
import java.util.List;

public class DeltaCalculator {

    // Max value for 64-bit unsigned counter (used by /proc/net/dev)
    private static final long UNSIGNED_64_MAX = Long.MAX_VALUE;

    /**
     * Computes delta between two sample values based on metric type.
     *
     * For COUNTER: always returns >= 0, handles wraparound.
     * For GAUGE: returns raw difference (can be negative).
     */
    public static long computeDelta(long previous, long current, MetricType type) {
        if (type == MetricType.COUNTER) {
            if (current >= previous) {
                return current - previous;
            }
            // Wraparound detected
            return (UNSIGNED_64_MAX - previous) + current + 1;
        }
        // GAUGE: simple difference, can be negative
        return current - previous;
    }

    /**
     * Computes a list of DeltaResult objects from consecutive samples.
     * Each DeltaResult contains the delta between sample[i] and sample[i+1].
     *
     * @param samples Ordered list of samples (must be same metric + tags)
     * @param type    The metric type (COUNTER or GAUGE)
     * @return List of DeltaResult (size = samples.size() - 1)
     */
    public static List<DeltaResult> computeDeltas(List<Sample> samples, MetricType type) {
        List<DeltaResult> deltas = new ArrayList<>();
        for (int i = 1; i < samples.size(); i++) {
            Sample prev = samples.get(i - 1);
            Sample curr = samples.get(i);
            long delta = computeDelta(prev.getValue(), curr.getValue(), type);
            long elapsed = curr.getTimestamp() - prev.getTimestamp();
            deltas.add(new DeltaResult(prev.getTimestamp(), curr.getTimestamp(), delta, elapsed));
        }
        return deltas;
    }
}
```

---

### Component 3: Delta Result

#### [NEW] [DeltaResult.java](file:///home/ubuntu/GitHub/kernel-pulse/src/main/java/com/github/pablohdzvizcarra/metric/DeltaResult.java)

A data class to hold the result of a delta computation:

```java
package com.github.pablohdzvizcarra.metric;

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

    /** Rate of change per second */
    public double getRatePerSecond() {
        if (elapsedMs == 0) return 0.0;
        return (double) delta / (elapsedMs / 1000.0);
    }

    // getters + toString ...
}
```

---

### Component 4: Database Query Enhancement

#### [MODIFY] [DatabaseManager.java](file:///home/ubuntu/GitHub/kernel-pulse/src/main/java/com/github/pablohdzvizcarra/DatabaseManager.java)

Add a new query method that filters by metric name and tags:

```java
/**
 * Get samples for a specific metric within a time range,
 * optionally filtered by tags.
 */
public List<Sample> getSamplesByMetric(String metricName, Map<String, String> tags,
                                        long startTime, long endTime) {
    // Query: SELECT ... WHERE metric_name = ? AND timestamp BETWEEN ? AND ?
    // Then filter in Java by matching tags (since tags are stored as JSON text)
    // Order by timestamp ASC to ensure correct delta computation
}
```

> [!NOTE]
> Tag filtering is done in Java after the SQL query because SQLite doesn't natively support JSON field queries without extensions. The composite index `idx_metrics_name_time` already covers `(metric_name, timestamp)` efficiently.

---

### Component 5: MetricsClient Integration

#### [MODIFY] [MetricsClient.java](file:///home/ubuntu/GitHub/kernel-pulse/src/main/java/com/github/pablohdzvizcarra/MetricsClient.java)

Add public methods to expose delta calculations:

```java
/**
 * Get deltas for a specific metric over a time range.
 * For network metrics, tags should include {"interface": "<name>"}.
 */
public List<DeltaResult> getDeltas(String metricName, Map<String, String> tags,
                                    Calendar start, Calendar end) {
    List<Sample> samples = dbManager.getSamplesByMetric(
        metricName, tags, start.getTimeInMillis(), end.getTimeInMillis());
    MetricType type = MetricType.forMetric(metricName);
    return DeltaCalculator.computeDeltas(samples, type);
}
```

---

### Component 6: Unit Tests

#### [NEW] [DeltaCalculatorTest.java](file:///home/ubuntu/GitHub/kernel-pulse/src/test/java/com/github/pablohdzvizcarra/metric/DeltaCalculatorTest.java)

Test cases to validate delta computation:

| Test Case | Description |
|:---|:---|
| `testCounterNormalDelta` | Counter: 1000 → 5000 = delta 4000 |
| `testCounterWraparound` | Counter: MAX-10 → 5 = delta 16 (wraparound) |
| `testCounterSameValue` | Counter: 500 → 500 = delta 0 |
| `testGaugePositiveDelta` | Gauge: 2000 → 3000 = delta 1000 (memory freed) |
| `testGaugeNegativeDelta` | Gauge: 3000 → 2000 = delta -1000 (memory consumed) |
| `testGaugeZeroDelta` | Gauge: 2000 → 2000 = delta 0 |
| `testComputeDeltasList` | Multiple samples → correct list of DeltaResults |
| `testRatePerSecond` | Verify rate calculation from delta and elapsed time |

---

## File Summary

| Action | File | Component |
|:---|:---|:---|
| **NEW** | `metric/MetricType.java` | Enum: COUNTER vs GAUGE with registry |
| **NEW** | `metric/DeltaCalculator.java` | Stateless utility for delta computation |
| **NEW** | `metric/DeltaResult.java` | Data class for delta + elapsed time |
| **MODIFY** | `DatabaseManager.java` | Add `getSamplesByMetric()` with tag filtering |
| **MODIFY** | `MetricsClient.java` | Add `getDeltas()` public API method |
| **NEW** | `DeltaCalculatorTest.java` | Unit tests for all delta scenarios |

---

## Verification Plan

### Automated Tests
1. `mvn clean compile` — verify all new files compile
2. `mvn test` — run `DeltaCalculatorTest` to validate delta logic
3. Pure unit tests with no external dependencies (no `/proc`, no SQLite)

### Manual Verification
1. Run the app for a few minutes to collect samples
2. Use `DemoClient` (or a new delta demo) to query deltas:
   ```
   sqlite3 metrics.db "SELECT * FROM metrics WHERE metric_name = 'network_bytes_read' ORDER BY timestamp DESC LIMIT 5;"
   ```
3. Verify that network byte deltas are positive and reasonable
4. Verify that memory deltas show both positive and negative values

### Implementation Order
1. `MetricType.java` (no dependencies)
2. `DeltaResult.java` (no dependencies)
3. `DeltaCalculator.java` (depends on MetricType, DeltaResult, Sample)
4. `DeltaCalculatorTest.java` (validate core logic before integration)
5. `DatabaseManager.java` modifications (new query method)
6. `MetricsClient.java` modifications (wire it all together)
