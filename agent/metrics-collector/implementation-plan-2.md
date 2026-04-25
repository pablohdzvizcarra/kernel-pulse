# Network Bytes Collector Implementation Plan

To collect network read and write bytes from `/proc/net/dev`, we need to parse the file, extract the relevant columns, and generate multiple metrics for each network interface. 

## User Review Required

> [!WARNING]
> The current `Collector` interface only supports generating a *single* `Sample` via `Sample generateSample()`. However, the network collector needs to return *multiple* samples per run (two metrics: `network_bytes_read` and `network_bytes_written` for *each* interface).
> **Are you okay with changing `Collector.java` to return a `List<Sample>` instead?** This will require a minor update to the existing `FreeRamMemoryCollector` to return a list containing its single sample.

## Proposed Changes

### 1. Update the `Collector` Interface

#### [MODIFY] `src/main/java/com/github/pablohdzvizcarra/collector/Collector.java`
- Change `Sample generateSample();` to `List<Sample> generateSamples();`.

### 2. Update the Existing RAM Collector

#### [MODIFY] `src/main/java/com/github/pablohdzvizcarra/collector/FreeRamMemoryCollector.java`
- Update `generateSample()` to `generateSamples()` and return a `List<Sample>`.
- Update the `run()` method to iterate over the returned list and insert each sample into the database.

### 3. Create the New Network Collector

#### [NEW] `src/main/java/com/github/pablohdzvizcarra/collector/NetworkBytesCollector.java`
- Implement the `Collector` interface.
- Constructor takes `DatabaseManager`.
- `run()` method iterates over generated samples and inserts them.
- `generateSamples()` logic:
  - Read `/proc/net/dev`.
  - Skip the first two header lines.
  - For each data line, split by `:` to get the interface name (e.g., `enp0s6`).
  - Split the rest of the line by whitespace.
  - Parse `Receive bytes` (index 0) and `Transmit bytes` (index 8).
  - Create two `Sample` objects per interface:
    - `metricName`: `"network_bytes_read"`, `tags`: `{"interface": "<interface_name>"}`
    - `metricName`: `"network_bytes_written"`, `tags`: `{"interface": "<interface_name>"}`
  - Add all samples to a list and return.

### 4. Wire the New Collector in the Main App

#### [MODIFY] `src/main/java/com/github/pablohdzvizcarra/KernelPulseApp.java`
- Instantiate `NetworkBytesCollector`.
- Schedule it to run alongside `FreeRamMemoryCollector` in the `ScheduledExecutorService`.

## Verification Plan

### Automated Tests
- Run `mvn clean compile` to verify the code compiles without errors after interface changes.
- Ensure the app runs without crashing.

### Manual Verification
- Query the database using `sqlite3 metrics.db "SELECT * FROM metrics WHERE metric_name LIKE 'network_bytes_%';"` to verify that both read and write metrics are being properly recorded with the correct tags.
