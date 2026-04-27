# Disk I/O Collector Feature Notes

## Overview
This feature introduces a new `DiskIoCollector` that collects metrics for disk block devices (e.g., `sda`) by reading `/proc/diskstats`. Following the "Stored Deltas" and "Table-per-Metric" architectural patterns, it natively calculates the rate of change (deltas) for disk counters directly in the memory of the collection layer before persisting them into a specialized, dedicated database table (`disk_io_metrics`).

## Architecture & Code Changes

### 1. Dedicated Table Schema (`DatabaseManager.java`)
We broke away from the "narrow schema" (`metrics` table) to use a "Table-per-Metric" approach tailored to Disk I/O. 
- **Modifications**: Added a new table creation execution to the `initDatabase()` method.
- **Table Name**: `disk_io_metrics`
- **Columns**:
  - `timestamp` (INTEGER)
  - `device_name` (TEXT)
  - **Raw Counters**: `reads_completed`, `sectors_read`, `writes_completed`, `sectors_written` (INTEGER)
  - **Deltas**: `reads_delta`, `sectors_read_delta`, `writes_delta`, `sectors_written_delta` (INTEGER)
- **Insertion**: Created a dedicated `insertDiskIoSample(DiskIoSample sample)` method handling mapping to the new schema layout.

### 2. Domain Model Customization (`DiskIoSample.java`)
Since we introduced a specialized table format, we created `DiskIoSample` as a subclass of the primary `Sample` class.
- Uses `super(timestamp, "disk_io", 0, tags)` to conform to the inheritance hierarchy.
- Encapsulates 4 raw hardware metric variables and 4 delta calculation variables to prevent tight coupling across method signatures.

### 3. Stateful Metric Collection (`DiskIoCollector.java`)
The core producer logic was implemented using a stateful pattern.
- **Parsing**: Inspects `/proc/diskstats`, breaking by whitespace `\\s+`, specifically grabbing devices starting with `"sda"`. Extracts columns 3 (reads), 5 (sectors read), 7 (writes), and 9 (sectors written).
- **Statefulness**: Maintains a `Map<String, DiskIoSample> previousSamples` which records the last measured state per device.
- **Delta Calculation**: Uses the existing `DeltaCalculator.computeDelta(previous, current, MetricType.COUNTER)` to seamlessly calculate differences while automatically handling Linux `unsigned 64-bit` counter wraparound behaviors.
- **Handling First Run**: During the first tick, `previous == null`, so deltas initialize at `0`, but the system still ensures that the foundational base states are properly recorded into SQLite.

### 4. Scheduler Execution (`KernelPulseApp.java`)
Wired the new `DiskIoCollector` into the application lifecycle:
- Bootstrapped via the main `ScheduledExecutorService`.
- Executed on a fixed rate of `1 MINUTE`, in parallel alongside network and RAM collectors.

## Validation Notes
- Confirmed correct startup without any exceptions using `mvn clean compile exec:java`.
- Ran `sqlite3 metrics.db "SELECT * FROM disk_io_metrics LIMIT 5;"` confirming data properly injected. 

## Next Steps / Future Enhancements
- If needed, we can further extend this to read time intervals (e.g., *time spent reading/writing*) from `/proc/diskstats` columns 7 and 11.
- Expand device discovery if we want to monitor NVMe drives (`nvme*`) or other virtual block devices.
