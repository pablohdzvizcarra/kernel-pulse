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
import com.github.pablohdzvizcarra.metric.Sample;

public class NetworkBytesCollector implements Collector {
    private static final Logger log = Logger.getLogger(NetworkBytesCollector.class.getName());
    private final DatabaseManager dbManager;

    public NetworkBytesCollector(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public void run() {
        List<Sample> samples = generateSamples();

        if (samples != null && !samples.isEmpty()) {
            for (Sample sample : samples) {
                dbManager.insertSample(sample);
                log.info(() -> "Collector: Inserted " + sample);
            }
        } else {
            log.warning(() -> "Collector: Failed to generate samples");
        }
    }

    @Override
    public List<Sample> generateSamples() {
        List<Sample> samples = new ArrayList<>();
        long timestamp = System.currentTimeMillis();

        try (BufferedReader reader = new BufferedReader(new FileReader(PROC_NET_DEV))) {
            String line;
            int lineCount = 0;
            while ((line = reader.readLine()) != null) {
                lineCount++;
                if (lineCount <= 2) {
                    continue; // Skip the two header lines
                }

                String[] interfaceSplit = line.split(":", 2);
                if (interfaceSplit.length != 2) {
                    continue;
                }

                String interfaceName = interfaceSplit[0].trim();
                String metricsPart = interfaceSplit[1].trim();
                String[] columns = metricsPart.split("\\s+");

                if (columns.length >= 9) { // We need at least index 8
                    try {
                        long readBytes = Long.parseLong(columns[0]);
                        long writtenBytes = Long.parseLong(columns[8]);

                        Map<String, String> tags = new HashMap<>();
                        tags.put("interface", interfaceName);

                        samples.add(new Sample(timestamp, "network_bytes_read", readBytes, tags));
                        samples.add(new Sample(timestamp, "network_bytes_written", writtenBytes, tags));
                    } catch (NumberFormatException e) {
                        log.warning("Failed to parse metric values for interface: " + interfaceName);
                    }
                }
            }
        } catch (IOException e) {
            log.severe(() -> "Collector failed to read /proc/net/dev: " + e.getMessage());
        } catch (Exception e) {
            log.severe(() -> "Collector encountered an error: " + e.getMessage());
        }

        return samples;
    }
}
