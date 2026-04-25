package com.github.pablohdzvizcarra;

import java.util.List;

import com.github.pablohdzvizcarra.metric.Sample;

public class Result {
    private List<Sample> samples;
    private double average;
    private long max;
    private long min;
    private int count;

    public Result(List<Sample> samples, double average, long max, long min, int count) {
        this.samples = samples;
        this.average = average;
        this.max = max;
        this.min = min;
        this.count = count;
    }

    public List<Sample> getSamples() {
        return samples;
    }

    public double getAverage() {
        return average;
    }

    public long getMax() {
        return max;
    }

    public long getMin() {
        return min;
    }

    public int getCount() {
        return count;
    }

    @Override
    public String toString() {
        return "Result{" +
                "count=" + count +
                ", average=" + average +
                ", max=" + max +
                ", min=" + min +
                ", samplesSize=" + (samples != null ? samples.size() : 0) +
                '}';
    }
}
