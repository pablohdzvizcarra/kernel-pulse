package com.github.pablohdzvizcarra.metric;

import java.util.Map;

public class DiskIoSample extends Sample {
    private final String deviceName;
    private final long readsCompleted;
    private final long sectorsRead;
    private final long writesCompleted;
    private final long sectorsWritten;
    
    private final long readsCompletedDelta;
    private final long sectorsReadDelta;
    private final long writesCompletedDelta;
    private final long sectorsWrittenDelta;
    
    private final double readsRate;
    private final double sectorsReadRate;
    private final double writesRate;
    private final double sectorsWrittenRate;

    public DiskIoSample(long timestamp, String deviceName, 
                        long readsCompleted, long sectorsRead, 
                        long writesCompleted, long sectorsWritten,
                        long readsCompletedDelta, long sectorsReadDelta, 
                        long writesCompletedDelta, long sectorsWrittenDelta,
                        double readsRate, double sectorsReadRate,
                        double writesRate, double sectorsWrittenRate,
                        Map<String, String> tags) {
        // We use "disk_io" as the general metricName and 0 for the generic value
        super(timestamp, "disk_io", 0, tags);
        this.deviceName = deviceName;
        this.readsCompleted = readsCompleted;
        this.sectorsRead = sectorsRead;
        this.writesCompleted = writesCompleted;
        this.sectorsWritten = sectorsWritten;
        this.readsCompletedDelta = readsCompletedDelta;
        this.sectorsReadDelta = sectorsReadDelta;
        this.writesCompletedDelta = writesCompletedDelta;
        this.sectorsWrittenDelta = sectorsWrittenDelta;
        this.readsRate = readsRate;
        this.sectorsReadRate = sectorsReadRate;
        this.writesRate = writesRate;
        this.sectorsWrittenRate = sectorsWrittenRate;
    }

    public String getDeviceName() { return deviceName; }
    public long getReadsCompleted() { return readsCompleted; }
    public long getSectorsRead() { return sectorsRead; }
    public long getWritesCompleted() { return writesCompleted; }
    public long getSectorsWritten() { return sectorsWritten; }
    
    public long getReadsCompletedDelta() { return readsCompletedDelta; }
    public long getSectorsReadDelta() { return sectorsReadDelta; }
    public long getWritesCompletedDelta() { return writesCompletedDelta; }
    public long getSectorsWrittenDelta() { return sectorsWrittenDelta; }

    public double getReadsRate() { return readsRate; }
    public double getSectorsReadRate() { return sectorsReadRate; }
    public double getWritesRate() { return writesRate; }
    public double getSectorsWrittenRate() { return sectorsWrittenRate; }

    @Override
    public String toString() {
        return "DiskIoSample{" +
                "timestamp=" + getTimestamp() +
                ", deviceName='" + deviceName + '\'' +
                ", readsCompleted=" + readsCompleted +
                ", sectorsRead=" + sectorsRead +
                ", writesCompleted=" + writesCompleted +
                ", sectorsWritten=" + sectorsWritten +
                ", readsCompletedDelta=" + readsCompletedDelta +
                ", sectorsReadDelta=" + sectorsReadDelta +
                ", writesCompletedDelta=" + writesCompletedDelta +
                ", sectorsWrittenDelta=" + sectorsWrittenDelta +
                ", readsRate=" + readsRate +
                ", sectorsReadRate=" + sectorsReadRate +
                ", writesRate=" + writesRate +
                ", sectorsWrittenRate=" + sectorsWrittenRate +
                ", tags=" + getTags() +
                '}';
    }
}
