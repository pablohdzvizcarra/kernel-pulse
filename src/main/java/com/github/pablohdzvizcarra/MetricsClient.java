package com.github.pablohdzvizcarra;

import java.util.Calendar;
import java.util.List;

public class MetricsClient {
    private final DatabaseManager dbManager;

    public MetricsClient(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public Result getFreeMemory(Calendar start, Calendar end) {
        long startTime = start.getTimeInMillis();
        long endTime = end.getTimeInMillis();
        
        List<Sample> samples = dbManager.getSamples(startTime, endTime);
        
        if (samples.isEmpty()) {
            return new Result(samples, 0.0, 0, 0, 0);
        }

        double sum = 0;
        long max = Long.MIN_VALUE;
        long min = Long.MAX_VALUE;

        for (Sample sample : samples) {
            long val = sample.getValue();
            sum += val;
            if (val > max) max = val;
            if (val < min) min = val;
        }

        double average = sum / samples.size();

        return new Result(samples, average, max, min, samples.size());
    }
}
