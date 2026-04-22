package com.github.pablohdzvizcarra;

import java.util.List;

public class Result {
    private List<Sample> samples;
    private double average;
    private double max;
    private double min;
    private int count;

    public Result(List<Sample> samples, double average, double max, double min, int count) {
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

    public double getMax() {
        return max;
    }

    public double getMin() {
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
