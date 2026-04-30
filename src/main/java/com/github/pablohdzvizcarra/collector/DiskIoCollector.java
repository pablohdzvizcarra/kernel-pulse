package com.github.pablohdzvizcarra.collector;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.github.pablohdzvizcarra.DatabaseManager;
import com.github.pablohdzvizcarra.metric.DeltaCalculator;
import com.github.pablohdzvizcarra.metric.DiskIoSample;
import com.github.pablohdzvizcarra.metric.MetricType;
import com.github.pablohdzvizcarra.metric.RateCalculator;
import com.github.pablohdzvizcarra.metric.Sample;

public class DiskIoCollector implements Collector {
    private static final Logger log = Logger.getLogger(DiskIoCollector.class.getName());
    private final DatabaseManager dbManager;
    
    // Stateful map to keep previous samples for delta calculation
    // Key: device_name (e.g. "sda")
    private final Map<String, DiskIoSample> previousSamples = new HashMap<>();

    public DiskIoCollector(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public void run() {
        List<Sample> samples = generateSamples();
        if (samples != null && !samples.isEmpty()) {
            for (Sample sample : samples) {
                if (sample instanceof DiskIoSample diskIoSample) {
                    dbManager.insertDiskIoSample(diskIoSample);
                    log.info(() -> "Collector: Inserted " + diskIoSample);
                }
            }
        } else {
            log.warning(() -> "DiskIoCollector: Failed to generate samples or no new deltas could be calculated");
        }
    }

    @Override
    public List<Sample> generateSamples() {
        List<Sample> samples = new ArrayList<>();
        long currentTimestamp = System.currentTimeMillis();

        try (BufferedReader reader = new BufferedReader(new FileReader(PROC_DISKSTATS))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().split("\\s+");
                // parts[2] is the device name
                if (parts.length >= 14 && parts[2].startsWith("sda")) {
                    String deviceName = parts[2];
                    
                    // Column indices based on /proc/diskstats
                    // parts[3]: reads completed
                    // parts[5]: sectors read
                    // parts[7]: writes completed
                    // parts[9]: sectors written
                    long readsCompleted = Long.parseLong(parts[3]);
                    long sectorsRead = Long.parseLong(parts[5]);
                    long writesCompleted = Long.parseLong(parts[7]);
                    long sectorsWritten = Long.parseLong(parts[9]);

                    DiskIoSample previous = previousSamples.get(deviceName);
                    
                    long readsCompletedDelta = 0;
                    long sectorsReadDelta = 0;
                    long writesCompletedDelta = 0;
                    long sectorsWrittenDelta = 0;

                    double readsRate = 0.0;
                    double sectorsReadRate = 0.0;
                    double writesRate = 0.0;
                    double sectorsWrittenRate = 0.0;
                    
                    if (previous != null) {
                        readsCompletedDelta = DeltaCalculator.computeDelta(previous.getReadsCompleted(), readsCompleted, MetricType.COUNTER);
                        sectorsReadDelta = DeltaCalculator.computeDelta(previous.getSectorsRead(), sectorsRead, MetricType.COUNTER);
                        writesCompletedDelta = DeltaCalculator.computeDelta(previous.getWritesCompleted(), writesCompleted, MetricType.COUNTER);
                        sectorsWrittenDelta = DeltaCalculator.computeDelta(previous.getSectorsWritten(), sectorsWritten, MetricType.COUNTER);
                        
                        long elapsedMs = currentTimestamp - previous.getTimestamp();
                        readsRate = RateCalculator.computeRatePerSecond(readsCompletedDelta, elapsedMs);
                        sectorsReadRate = RateCalculator.computeRatePerSecond(sectorsReadDelta, elapsedMs);
                        writesRate = RateCalculator.computeRatePerSecond(writesCompletedDelta, elapsedMs);
                        sectorsWrittenRate = RateCalculator.computeRatePerSecond(sectorsWrittenDelta, elapsedMs);
                    }

                    DiskIoSample currentSample = new DiskIoSample(
                            currentTimestamp, deviceName,
                            readsCompleted, sectorsRead, writesCompleted, sectorsWritten,
                            readsCompletedDelta, sectorsReadDelta, writesCompletedDelta, sectorsWrittenDelta,
                            readsRate, sectorsReadRate, writesRate, sectorsWrittenRate,
                            new HashMap<>() // tags
                    );
                    
                    previousSamples.put(deviceName, currentSample);
                    
                    // Always add to the return list to store raw metrics, even if deltas are 0 initially
                    samples.add(currentSample);
                }
            }
        } catch (IOException e) {
            log.severe(() -> "DiskIoCollector failed to read /proc/diskstats: " + e.getMessage());
        } catch (Exception e) {
            log.severe(() -> "DiskIoCollector encountered an error: " + e.getMessage());
        }

        return samples;
    }
}
