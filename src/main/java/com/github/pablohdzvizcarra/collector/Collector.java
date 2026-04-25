package com.github.pablohdzvizcarra.collector;

import com.github.pablohdzvizcarra.metric.Sample;

public interface Collector extends Runnable {

    /**
     * Generates a new sample for the specific metric.
     * 
     * @return Sample with metric data
     */
    Sample generateSample();
}
