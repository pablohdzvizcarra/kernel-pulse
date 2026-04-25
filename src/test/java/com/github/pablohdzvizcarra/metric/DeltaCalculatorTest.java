package com.github.pablohdzvizcarra.metric;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for DeltaCalculator.
 * Pure unit tests — no external dependencies (no /proc, no SQLite).
 */
class DeltaCalculatorTest {

    // ── Counter Delta Tests ──────────────────────────────────────────────

    @Nested
    @DisplayName("Counter delta computation")
    class CounterDeltaTests {

        @Test
        @DisplayName("Normal delta: 1000 → 5000 = 4000")
        void testCounterNormalDelta() {
            long delta = DeltaCalculator.computeDelta(1000, 5000, MetricType.COUNTER);
            assertEquals(4000, delta, "Counter delta should be current - previous");
        }

        @Test
        @DisplayName("Wraparound: MAX-10 → 5 = 16")
        void testCounterWraparound() {
            long previous = Long.MAX_VALUE - 10;
            long current = 5;
            long delta = DeltaCalculator.computeDelta(previous, current, MetricType.COUNTER);
            // (MAX - previous) + current + 1 = (MAX - (MAX-10)) + 5 + 1 = 10 + 5 + 1 = 16
            assertEquals(16, delta, "Wraparound delta should apply the formula correctly");
        }

        @Test
        @DisplayName("Same value: 500 → 500 = 0")
        void testCounterSameValue() {
            long delta = DeltaCalculator.computeDelta(500, 500, MetricType.COUNTER);
            assertEquals(0, delta, "Delta of identical counter values should be zero");
        }

        @Test
        @DisplayName("Counter delta is always >= 0")
        void testCounterDeltaAlwaysNonNegative() {
            // Even with wraparound, result should be non-negative
            long delta = DeltaCalculator.computeDelta(Long.MAX_VALUE, 0, MetricType.COUNTER);
            assertTrue(delta >= 0, "Counter delta should never be negative");
            assertEquals(1, delta, "MAX → 0 wraparound should give delta of 1");
        }
    }

    // ── Gauge Delta Tests ────────────────────────────────────────────────

    @Nested
    @DisplayName("Gauge delta computation")
    class GaugeDeltaTests {

        @Test
        @DisplayName("Positive delta: 2000 → 3000 = 1000 (memory freed)")
        void testGaugePositiveDelta() {
            long delta = DeltaCalculator.computeDelta(2000, 3000, MetricType.GAUGE);
            assertEquals(1000, delta, "Positive gauge delta means resource increased");
        }

        @Test
        @DisplayName("Negative delta: 3000 → 2000 = -1000 (memory consumed)")
        void testGaugeNegativeDelta() {
            long delta = DeltaCalculator.computeDelta(3000, 2000, MetricType.GAUGE);
            assertEquals(-1000, delta, "Negative gauge delta means resource decreased");
        }

        @Test
        @DisplayName("Zero delta: 2000 → 2000 = 0 (no change)")
        void testGaugeZeroDelta() {
            long delta = DeltaCalculator.computeDelta(2000, 2000, MetricType.GAUGE);
            assertEquals(0, delta, "Zero gauge delta means no change in state");
        }
    }

    // ── List Computation Tests ───────────────────────────────────────────

    @Nested
    @DisplayName("computeDeltas() list computation")
    class ListDeltaTests {

        @Test
        @DisplayName("Computes deltas from a list of counter samples")
        void testComputeDeltasList() {
            List<Sample> samples = Arrays.asList(
                    new Sample(1000L, "network_bytes_read", 100),
                    new Sample(2000L, "network_bytes_read", 350),
                    new Sample(3000L, "network_bytes_read", 800)
            );

            List<DeltaResult> deltas = DeltaCalculator.computeDeltas(samples, MetricType.COUNTER);

            assertEquals(2, deltas.size(), "N samples should produce N-1 deltas");

            // First delta: 100 → 350 = 250, elapsed 1000ms
            assertEquals(250, deltas.get(0).getDelta());
            assertEquals(1000, deltas.get(0).getElapsedMs());
            assertEquals(1000, deltas.get(0).getStartTimestamp());
            assertEquals(2000, deltas.get(0).getEndTimestamp());

            // Second delta: 350 → 800 = 450, elapsed 1000ms
            assertEquals(450, deltas.get(1).getDelta());
            assertEquals(1000, deltas.get(1).getElapsedMs());
        }

        @Test
        @DisplayName("Returns empty list for fewer than 2 samples")
        void testComputeDeltasInsufficientSamples() {
            List<DeltaResult> empty = DeltaCalculator.computeDeltas(
                    Collections.emptyList(), MetricType.COUNTER);
            assertTrue(empty.isEmpty(), "Empty input should return empty deltas");

            List<DeltaResult> single = DeltaCalculator.computeDeltas(
                    Collections.singletonList(new Sample(1000, "test", 100)),
                    MetricType.COUNTER);
            assertTrue(single.isEmpty(), "Single sample should return empty deltas");
        }

        @Test
        @DisplayName("Returns empty list for null input")
        void testComputeDeltasNullInput() {
            List<DeltaResult> result = DeltaCalculator.computeDeltas(null, MetricType.COUNTER);
            assertTrue(result.isEmpty(), "Null input should return empty deltas");
        }
    }

    // ── Rate Computation Tests ───────────────────────────────────────────

    @Nested
    @DisplayName("DeltaResult rate computation")
    class RateTests {

        @Test
        @DisplayName("Rate per second: 60000 bytes in 60000ms = 1000 bytes/sec")
        void testRatePerSecond() {
            DeltaResult result = new DeltaResult(0, 60000, 60000, 60000);
            assertEquals(1000.0, result.getRatePerSecond(), 0.01,
                    "60000 bytes in 60 seconds = 1000 bytes/sec");
        }

        @Test
        @DisplayName("Rate with zero elapsed time returns 0.0")
        void testRateZeroElapsed() {
            DeltaResult result = new DeltaResult(1000, 1000, 500, 0);
            assertEquals(0.0, result.getRatePerSecond(),
                    "Zero elapsed time should return 0.0 rate");
        }
    }

    // ── MetricType Registry Tests ────────────────────────────────────────

    @Nested
    @DisplayName("MetricType.forMetric() registry")
    class MetricTypeRegistryTests {

        @Test
        @DisplayName("Network metrics are classified as COUNTER")
        void testNetworkMetricsAreCounters() {
            assertEquals(MetricType.COUNTER, MetricType.forMetric("network_bytes_read"));
            assertEquals(MetricType.COUNTER, MetricType.forMetric("network_bytes_written"));
        }

        @Test
        @DisplayName("Memory metric is classified as GAUGE")
        void testMemoryMetricIsGauge() {
            assertEquals(MetricType.GAUGE, MetricType.forMetric("memory_free"));
        }

        @Test
        @DisplayName("Unknown metrics default to GAUGE")
        void testUnknownMetricDefaultsToGauge() {
            assertEquals(MetricType.GAUGE, MetricType.forMetric("something_unknown"));
        }
    }
}
