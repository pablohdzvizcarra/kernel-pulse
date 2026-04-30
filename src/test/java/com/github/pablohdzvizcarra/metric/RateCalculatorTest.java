package com.github.pablohdzvizcarra.metric;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RateCalculatorTest {

    @Test
    @DisplayName("Calculate simple rate: 1000 items in 1 second")
    void testSimpleRate() {
        Sample previous = new Sample(1000L, "metric_name", 0);
        Sample current = new Sample(2000L, "metric_name", 1000);

        double rate = RateCalculator.computeRatePerSecond(previous, current, MetricType.COUNTER);
        assertEquals(1000.0, rate, 0.01, "Should be exactly 1000/sec");
    }

    @Test
    @DisplayName("Calculate rate over list of samples")
    void testComputeRatesList() {
        List<Sample> samples = Arrays.asList(
                new Sample(1000L, "disk_reads", 100),
                new Sample(2000L, "disk_reads", 350),  // delta = 250, time = 1000ms -> rate = 250
                new Sample(3000L, "disk_reads", 800)   // delta = 450, time = 1000ms -> rate = 450
        );

        List<Double> rates = RateCalculator.computeRates(samples, MetricType.COUNTER);

        assertEquals(2, rates.size(), "N samples should produce N-1 rates");
        assertEquals(250.0, rates.get(0), 0.01);
        assertEquals(450.0, rates.get(1), 0.01);
    }

    @Test
    @DisplayName("Zero elapsed time returns 0.0 rate")
    void testZeroElapsedTime() {
        Sample previous = new Sample(1000L, "metric", 100);
        Sample current = new Sample(1000L, "metric", 200);

        double rate = RateCalculator.computeRatePerSecond(previous, current, MetricType.COUNTER);
        assertEquals(0.0, rate, "Should handle division by zero safely");
    }
}
