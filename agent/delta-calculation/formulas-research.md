# Rate, Ratio, and Average Latency Analysis

This document analyzes how the formulas for Rate, Ratio, and Average Latency apply to the currently supported telemetry metrics in the `kernel-pulse` project.

## 1. Rate

**Definition:** Describes how fast a counter is growing over a specific time window.
`Rate = (Value(T2) - Value(T1)) / (T2 - T1)`

**Applicability to Current Metrics:**
*   **Network Bytes (`NetworkBytesCollector`):** 
    *   **Applicable.** We collect `network_bytes_read` and `network_bytes_written` (cumulative counters).
    *   *Usage:* Calculating Network Bandwidth/Throughput (e.g., Bytes read per second, Bytes written per second).
*   **Disk I/O (`DiskIoCollector`):** 
    *   **Applicable.** We collect `readsCompleted`, `writesCompleted`, `sectorsRead`, and `sectorsWritten` (cumulative counters).
    *   *Usage:* 
        *   **IOPS (I/O Operations Per Second):** Rate of `readsCompleted` and `writesCompleted`.
        *   **Disk Throughput:** Rate of `sectorsRead` and `sectorsWritten` (which can be converted to bytes/sec).
*   **Free RAM Memory (`FreeRamMemoryCollector`):**
    *   **Not Applicable.** Free RAM is a Gauge metric representing a snapshot of state, not a cumulative growing counter. Calculating a "rate" of a gauge yields the instantaneous slope, which isn't typically used this way for RAM.

## 2. Ratio

**Definition:** Compares two related metrics, usually to find a percentage or utilization.
`Ratio = Metric A / Metric B`

**Applicability to Current Metrics:**
*   **Disk I/O (`DiskIoCollector`):** 
    *   **Applicable.** 
    *   *Usage:* 
        *   **Average I/O Size:** `sectorsRead / readsCompleted` (tells us how many sectors are read per operation on average).
        *   **Read vs. Write Ratio:** `readsCompleted / writesCompleted`.
*   **Network Bytes (`NetworkBytesCollector`):**
    *   **Applicable.** 
    *   *Usage:* **Network Traffic Ratio:** `network_bytes_read / network_bytes_written` to understand the balance of inbound vs. outbound traffic.
*   **Free RAM Memory (`FreeRamMemoryCollector`):**
    *   **Partially Applicable.** Currently, we only collect `free_ram_memory`. If we also collected `total_ram_memory`, we could calculate **RAM Utilization Ratio** (`(total_ram_memory - free_ram_memory) / total_ram_memory`).

## 3. Average Latency

**Definition:** The average time taken per operation. Best calculated using two distinct cumulative counters: total time spent, and total operations completed.
`Average Latency = (Total Time(T2) - Total Time(T1)) / (Total Operations(T2) - Total Operations(T1))`

**Applicability to Current Metrics:**
*   **Disk I/O (`DiskIoCollector`):** 
    *   **Requires Enhancement.** Currently, `DiskIoCollector` collects operations (`readsCompleted`, `writesCompleted`) but **does not** collect the total time spent doing these operations.
    *   *Actionable Next Step:* `/proc/diskstats` provides "time spent reading (ms)" (Field 4) and "time spent writing (ms)" (Field 8). By adding these fields to `DiskIoCollector.java`, we can perfectly calculate **Disk Read Latency** and **Disk Write Latency**.
*   **Network Bytes & Free RAM:**
    *   **Not Applicable.** Neither of these expose cumulative time spent doing operations natively in our collectors.

## Summary & Next Steps

1.  **Rate** and **Ratio** formulas can immediately be applied to our existing Network and Disk I/O metrics.
2.  To implement **Average Latency**, we should update `DiskIoCollector.java` to read Fields 4 and 8 from `/proc/diskstats`.
