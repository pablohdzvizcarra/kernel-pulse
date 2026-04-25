# Pending Learning Items

Here is the theory behind the concepts you want to learn:

## 1. Cumulative counters vs sampled metrics

*   **Sampled metrics (Gauges):** These represent a single value at a specific point in time. They can go up or down. Examples include current free RAM, CPU temperature, or the number of active database connections. When you sample a gauge, you get the absolute current value. The downside is that if there is a rapid spike and drop between two samples, you will completely miss it.
*   **Cumulative counters:** These are values that continually increase over time. They represent a running total of an event since a specific starting point (like system boot or process start). Examples include total network bytes sent, total CPU ticks spent in user mode, or total disk I/O operations. Because they never decrease (except on restart or wraparound), you can precisely measure what happened between *any* two points in time by calculating the difference, ensuring no spikes are missed.

## 2. Counter delta calculation and wraparound handling

To understand the activity between two timestamps $T_1$ and $T_2$, you calculate the **delta** (difference) of the counter:
`Delta = Value(T2) - Value(T1)`

**Wraparound Handling:**
Since counters are stored in fixed-size integer variables (e.g., 32-bit or 64-bit), they eventually reach their maximum possible value (like $2^{32} - 1$) and roll over back to zero. This is called a wraparound.
When a wraparound occurs, `Value(T2)` will be strictly less than `Value(T1)`. 
To calculate the true delta when `Value(T2) < Value(T1)` for an N-bit counter, the formula is:
`Delta = (Maximum_Possible_Value - Value(T1)) + Value(T2) + 1`

*Note on implementation:* In systems programming (like C or C++), if you use standard unsigned integer arithmetic (e.g., `uint32_t` or `uint64_t`), taking the difference `Value(T2) - Value(T1)` will automatically yield the correct positive delta due to how two's complement binary modular arithmetic works, even if a wraparound occurred!

## 3. Rate, ratio, and average-latency formulas

*   **Rate:** Describes how fast a counter is growing over a specific time window. 
    `Rate = (Value(T2) - Value(T1)) / (T2 - T1)`
    *Example:* If a counter measuring total bytes sent goes from 1000 to 5000 over 10 seconds, the rate is `(5000 - 1000) / 10 = 400 bytes/second`.
*   **Ratio:** Compares two related metrics, usually to find a percentage or utilization.
    `Ratio = Metric A / Metric B`
    *Example:* `CPU Utilization = (Time spent doing work) / (Total elapsed time)`
*   **Average Latency:** The average time taken per operation. This is best calculated using two distinct cumulative counters: one tracking total time spent, and another tracking the total number of operations completed.
    `Average Latency = (Total Time(T2) - Total Time(T1)) / (Total Operations(T2) - Total Operations(T1))`

## 4. Time-series data modeling for performance monitoring

Time-series data is simply a sequence of data points indexed in time order. When modeling this for telemetry, there are a few standard approaches:

*   **Wide Format vs Narrow Format:**
    *   *Wide:* Each row has a timestamp and many columns for different metrics (e.g., `time | cpu_usage | ram_usage | disk_io`). This is efficient if you always collect exactly the same set of metrics simultaneously.
    *   *Narrow (Tall):* Each row is a single data point (e.g., `time | metric_id | value`). This is highly extensible because adding a new metric doesn't require altering the database schema, though queries can be slightly more complex.
*   **Dimensionality / Tagging:** Modern telemetry avoids hardcoding contexts into metric names (like `cpu_server1`). Instead, a metric like `cpu_usage` is associated with key-value tags or dimensions (e.g., `host="server1"`, `datacenter="us-east"`). This allows powerful aggregations (e.g., "average CPU across all servers in us-east").

## 5. SQLite schema design and indexing for telemetry

SQLite is excellent for local, embedded telemetry storage on an agent or server.

**Schema Design:**
A typical "narrow" schema design in SQLite looks like this:
```sql
CREATE TABLE metrics (
    timestamp INTEGER NOT NULL,
    metric_name TEXT NOT NULL,
    value REAL NOT NULL,
    tags TEXT -- JSON string for flexible dimensions
);
```

**Indexing:**
Telemetry workloads are "write-heavy" (inserting new data continuously) but read patterns usually involve selecting a specific metric over a time range. To optimize this:
```sql
CREATE INDEX idx_metrics_name_time ON metrics(metric_name, timestamp);
```
This composite index allows SQLite to instantly find the `metric_name` and then scan sequentially through the relevant `timestamp` range.

**Performance Optimizations:**
*   **WAL Mode:** Run `PRAGMA journal_mode=WAL;` to enable Write-Ahead Logging. This allows concurrent reads and writes, drastically improving throughput.
*   **Batching:** Do not insert metrics one by one. Wrap multiple `INSERT` statements inside a `BEGIN TRANSACTION;` and `COMMIT;` block to reduce disk I/O overhead.
