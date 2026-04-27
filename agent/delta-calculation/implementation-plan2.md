# Disk I/O Collector Implementation Plan

This plan introduces a new metric collection feature that parses disk I/O metrics for `sda` devices, computes their deltas in the ingestion layer, and stores both raw values and deltas in a dedicated table. This aligns with the "Stored Deltas" and "Table-per-Metric" design patterns.

## User Review Required

> [!WARNING]
> This plan proposes introducing a new entity `DiskIoMetrics` instead of using the generic `Sample` interface since we are moving away from the generic `metrics` table for this specific collector. Is this acceptable, or would you prefer the new collector to still adhere to `List<Sample> generateSamples()` from the `Collector` interface, perhaps storing JSON in the `value` column?

## Open Questions

> [!IMPORTANT]  
> 1. In the initial plan, I will track reads completed, sectors read, writes completed, and sectors written. Should we track other columns from `/proc/diskstats` like time spent reading/writing?  
> 2. Should we handle wraparound using `DeltaCalculator` (using `MetricType.COUNTER`) for these disk counters?

## Proposed Changes

### Database Layer

#### [MODIFY] [DatabaseManager.java](file:///home/ubuntu/GitHub/kernel-pulse/src/main/java/com/github/pablohdzvizcarra/DatabaseManager.java)
- Update `initDatabase()` to also execute a query that creates a new `disk_io_metrics` table.
- **Table Schema**: `timestamp INTEGER`, `device_name TEXT`, `reads_completed INTEGER`, `sectors_read INTEGER`, `writes_completed INTEGER`, `sectors_written INTEGER`, `reads_delta INTEGER`, `sectors_read_delta INTEGER`, `writes_delta INTEGER`, `sectors_written_delta INTEGER`.
- Add a new method `insertDiskIoMetrics(DiskIoMetrics metrics)` to write records directly to this new table using `sqlite3`.

### Domain Models

#### [NEW] [DiskIoMetrics.java](file:///home/ubuntu/GitHub/kernel-pulse/src/main/java/com/github/pablohdzvizcarra/metric/DiskIoMetrics.java)
- A new Java class to represent the collected data and calculated deltas for a specific device.

### Collector Layer

#### [NEW] [DiskIoCollector.java](file:///home/ubuntu/GitHub/kernel-pulse/src/main/java/com/github/pablohdzvizcarra/collector/DiskIoCollector.java)
- Implement `Runnable`.
- Read and parse `/proc/diskstats`, filtering for `sda` devices (using `line.trim().split("\\s+")` and checking column 3).
- Implement a stateful Map (`Map<String, DiskIoMetrics> previousSamples`) to persist the last read values for each device.
- Calculate deltas on the fly using `DeltaCalculator.computeDelta` (as `COUNTER` type).
- Use `dbManager.insertDiskIoMetrics(...)` to store the calculated object.

### Application Wiring

#### [MODIFY] [KernelPulseApp.java](file:///home/ubuntu/GitHub/kernel-pulse/src/main/java/com/github/pablohdzvizcarra/KernelPulseApp.java)
- Instantiate `DiskIoCollector` and add it to the `ScheduledExecutorService`, running every 1 minute alongside the other collectors.

## Verification Plan

### Automated/Local Tests
- Run `mvn clean compile exec:java` to execute the `KernelPulseApp`.
- Ensure there are no runtime exceptions and that `DiskIoCollector` is scheduled.

### Manual Verification
- View `app.log` to confirm the collector executes properly.
- Run `sqlite3 metrics.db "SELECT * FROM disk_io_metrics;"` to verify that both raw metrics and deltas are successfully persisted.
