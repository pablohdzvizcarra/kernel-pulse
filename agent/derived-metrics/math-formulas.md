# Derived Metrics: Math Formulas

This document centralizes all mathematical formulas used to calculate derived metrics in the `kernel-pulse` project. 
Derived metrics are calculated from raw metrics (like cumulative counters or instantaneous gauges) collected by various collectors.

## 1. Delta

**Definition:** The absolute change in a metric's value between two points in time. It forms the foundation for computing other derived metrics like Rate and Average Latency. The calculation depends on whether the metric is a `COUNTER` (always increasing, subject to wraparound) or a `GAUGE` (can go up or down).

**Formulas:**
*   **GAUGE:** `Delta = Value(T2) - Value(T1)`
    *(Result can be positive, negative, or zero)*
*   **COUNTER:**
    *   If `Value(T2) >= Value(T1)`: `Delta = Value(T2) - Value(T1)`
    *   If `Value(T2) < Value(T1)` (Wraparound): `Delta = (MAX_VALUE - Value(T1)) + Value(T2) + 1`
    *(Result is always >= 0)*

**Applicability to Current Metrics:**
*   **Network Bytes (`NetworkBytesCollector`):** 
    *   **Applicable (COUNTER).** 
    *   *Usage:* Total bytes transferred in a time window.
*   **Disk I/O (`DiskIoCollector`):** 
    *   **Applicable (COUNTER).** 
    *   *Usage:* Total I/O operations or sectors read/written in a time window.
*   **Free RAM Memory (`FreeRamMemoryCollector`):**
    *   **Applicable (GAUGE).** 
    *   *Usage:* Amount of memory freed (positive delta) or consumed (negative delta) during the interval.

## 2. Rate

**Definition:** Describes how fast a counter is growing over a specific time window. Typically represented as operations per second or bytes per second.

`Rate = (Value(T2) - Value(T1)) / (T2 - T1)`
*(where `T2 - T1` is the elapsed time usually converted to seconds)*

**Applicability to Current Metrics:**
*   **Network Bytes (`NetworkBytesCollector`):** 
    *   **Applicable.** We collect `network_bytes_read` and `network_bytes_written` (cumulative counters).
    *   *Usage:* Calculating Network Bandwidth/Throughput (e.g., Bytes read per second, Bytes written per second).
*   **Disk I/O (`DiskIoCollector`):** 
    *   **Applicable.** We collect `readsCompleted`, `writesCompleted`, `sectorsRead`, and `sectorsWritten` (cumulative counters).
    *   *Usage:* 
        *   **IOPS (I/O Operations Per Second):** Rate of `readsCompleted` and `writesCompleted`.
        *   **Disk Throughput:** Rate of `sectorsRead` and `sectorsWritten` (which can be converted to bytes/sec by multiplying sectors by sector size, typically 512 bytes).
*   **Free RAM Memory (`FreeRamMemoryCollector`):**
    *   **Not Applicable.** Free RAM is a Gauge metric representing a snapshot of state, not a cumulative growing counter. 

## 3. Ratio

**Definition:** Compares two related metrics, usually to find a percentage, utilization, or a balance between two values.

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
    *   **Partially Applicable.** If we also collected `total_ram_memory`, we could calculate **RAM Utilization Ratio** (`(total_ram_memory - free_ram_memory) / total_ram_memory`).

## 4. Average Latency

**Definition:** The average time taken per operation. Best calculated using two distinct cumulative counters: total time spent, and total operations completed.

`Average Latency = (Total Time(T2) - Total Time(T1)) / (Total Operations(T2) - Total Operations(T1))`

**Applicability to Current Metrics:**
*   **Disk I/O (`DiskIoCollector`):** 
    *   **Applicable (Requires new fields).** By collecting "time spent reading (ms)" and "time spent writing (ms)" from `/proc/diskstats`, we can compute:
        *   **Disk Read Latency:** `Delta(time spent reading) / Delta(readsCompleted)`
        *   **Disk Write Latency:** `Delta(time spent writing) / Delta(writesCompleted)`
*   **Network Bytes & Free RAM:**
    *   **Not Applicable.** Neither of these expose cumulative time spent doing operations natively in our collectors.
