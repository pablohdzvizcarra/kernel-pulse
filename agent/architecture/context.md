Welcome to the world of time-series data! While SQLite is often thought of as a general-purpose relational database, it is surprisingly capable of handling performance metrics if the schema is designed correctly.

Here is a breakdown of the core concepts you’ll encounter while working with this architecture.

---

## 1. What exactly is a "Sample"?

In the context of performance monitoring, a **Sample** is a single discrete measurement of a system's state at a specific point in time. Think of it like a single frame in a movie. One frame doesn't tell you the whole story, but when you string them together, you see the movement (the trend).

A standard Sample in an SQLite table usually consists of three primary components:

* **Timestamp:** When the measurement was taken (usually in Epoch milliseconds or ISO-8601 strings).
* **Metric Identifier:** The name or ID of what is being measured (e.g., `cpu_usage`, `memory_free`, or `disk_io`).
* **Value:** The actual numeric measurement (e.g., `45.5` or `1024`).

**Why call it a "Sample"?** Because your background thread isn't watching the metric every millisecond; it "samples" the state once every minute. If a CPU spike happens at second 30 and disappears by second 59, your one-minute sample might miss it entirely. This is known as the **Sampling Rate**.

---

## 2. Performance Tables vs. Standard Tables

In a standard database, you might update a user's address or delete an old order. In a **Performance Database**, the behavior is different:

* **Immutable Data:** Once a sample is written, it is almost never changed. You don't "update" the CPU usage of 10 minutes ago.
* **Append-Heavy:** Your background thread is constantly performing `INSERT` operations.
* **Range-Based Reads:** Clients rarely ask for "Sample #502." They almost always ask for "All samples between 2:00 PM and 3:00 PM."



---

## 3. Understanding the Architecture

### The Ingestion Thread (The Collector)
Your thread running every minute is the **Data Producer**. In SQLite, it’s important that this thread handles transactions efficiently. Since SQLite locks the database during writes, keeping these "minute-by-minute" inserts fast is key to ensuring the API (the reader) doesn't get blocked.

### The JAR API (The Consumer)
The reason your API returns a **Result** object instead of just a raw list of samples is likely for **encapsulation and metadata**. A `Result` object allows the API to provide:
1.  **The Collection:** The actual list of `Sample` objects.
2.  **Summary Statistics:** The API might pre-calculate the average, min, or max value of that range so the client doesn't have to.
3.  **Status Codes:** Information on whether the range was valid or if the data was truncated.

---

## 4. Key Concepts for SQLite Metrics

If you are looking at the underlying SQLite tables, keep an eye out for these two performance "must-haves":

### Indexing on Timestamps
For a time-range query to be fast, the `timestamp` column **must** have an index. Without it, SQLite has to do a "Full Table Scan" (reading every single row in the database) just to find the metrics for the last five minutes. 

### Data Retention (Pruning)
Performance databases grow indefinitely. Usually, there is a "Retention Policy." Since you are saving samples every minute, that is 1,440 rows per metric per day. Most systems will eventually run a "cleanup" task to delete samples older than, say, 30 days to keep the SQLite file size manageable.

---

## Summary Table

| Term | Definition |
| :--- | :--- |
| **Sample** | A single data point (Timestamp + Value). |
| **Metric** | The "Subject" being measured (e.g., Temp, RAM). |
| **Sampling Rate** | The frequency of data collection (in your case, 1 minute). |
| **Range Query** | A request for data between two specific timestamps. |
| **TSDB** | Time-Series Database (The category this workflow falls into). |

Does the specific API you're working with allow for any "downsampling"—for example, asking for the hourly average instead of every single minute-by-minute sample?