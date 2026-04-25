package com.github.pablohdzvizcarra;

import java.util.Calendar;

import com.github.pablohdzvizcarra.metric.Sample;

public class DemoClient {
    public static void main(String[] args) {
        DatabaseManager dbManager = new DatabaseManager();
        MetricsClient client = new MetricsClient(dbManager);

        Calendar end = Calendar.getInstance();
        Calendar start = Calendar.getInstance();
        // Go back 1 hour
        start.add(Calendar.HOUR_OF_DAY, -1);

        System.out.println("Fetching memory metrics from " + start.getTime() + " to " + end.getTime());
        Result result = client.getFreeMemory(start, end);
        
        System.out.println("Result: " + result);
        for (Sample s : result.getSamples()) {
            System.out.println("  " + s);
        }
    }
}
