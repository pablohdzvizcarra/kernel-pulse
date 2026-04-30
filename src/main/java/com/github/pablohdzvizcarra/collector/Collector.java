package com.github.pablohdzvizcarra.collector;

import java.util.List;

import com.github.pablohdzvizcarra.metric.Sample;

public interface Collector extends Runnable, MetricPaths {

    /**
     * Generates samples for the specific metric.
     * 
     * @return List of Samples with metric data
     */
    List<Sample> generateSamples();
}
