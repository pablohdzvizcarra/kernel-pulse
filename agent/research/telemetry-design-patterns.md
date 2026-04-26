# Telemetry Storage & Calculation Patterns

## Overview
This document explores alternative patterns for storing and calculating telemetry data, specifically comparing the "Narrow Schema" used in Kernel Pulse with the "Table-per-Metric" and "Stored Delta" patterns found in many enterprise systems.

---

## 1. Table-per-Metric (Vertical Partitioning)
In this pattern, every unique metric (e.g., `cpu_usage`, `memory_free`) is stored in its own dedicated database table.

### Why use it?
*   **Performance Isolation:** High-volume metrics (e.g., 1s resolution) don't impact the query performance of low-volume metrics.
*   **Retention Policies:** Easier to delete old data. You can `DROP` or `TRUNCATE` a specific metric table without affecting others.
*   **Schema Specialization:** Allows adding metric-specific columns (e.g., `interface_name` for network, `process_id` for CPU).

### Trade-offs
*   **Schema Bloat:** Managing hundreds of tables can be difficult for migrations.
*   **Cross-Metric Queries:** Joining data from different metrics requires complex SQL joins across multiple tables.

---

## 2. Stored Deltas (Pre-calculation)
Instead of storing only the raw value, the ingestion layer calculates the difference (delta) from the previous sample and stores it in a dedicated column.

### Why use it?
*   **Fast Aggregation:** Calculating the total change over a period becomes a simple `SUM(delta)` rather than fetching all records and subtracting.
*   **Handling Counter Resets:** The server handles counter "wraparound" (e.g., network bytes rolling over to zero) or system reboots once at ingestion time.
*   **Simplified Clients:** The database provides the "clean truth," so the UI and API don't need to implement complex mathematical logic.

### Trade-offs
*   **Storage Overhead:** Storing an extra column per sample.
*   **Ingestion Complexity:** The "Producer" must be stateful (it needs to remember the last value to calculate the delta).

---

## 3. Server-Side Delegation
A core design decision in many systems is delegating the "heavy lifting" to the **Server** (the collector and database) rather than the **Client**.

### Benefits
*   **Reduced Payload:** The server can pre-aggregate data (e.g., hourly averages) so the client only downloads 24 points instead of 1,440.
*   **Consistency:** Every client (CLI, Dashboard, Mobile) sees the same calculated rate because the calculation happens in one place.

---

## 4. Polymorphic Samples (Interface Pattern)
In this pattern, a `Sample` is defined as an interface, with specific implementations for different metric types.

*   **`CounterSample`**: Implements logic for monotonic increases and wraparound.
*   **`GaugeSample`**: Implemented for point-in-time snapshots.

This allows the ingestion layer to treat all samples generically while executing metric-specific calculation logic.

---

## Summary Comparison

| Feature         | Narrow Schema (Kernel Pulse) | Table-per-Metric (Enterprise) |
| :-------------- | :--------------------------- | :---------------------------- |
| **Flexibility** | High (Any tag combination)   | Low (Fixed per table)         |
| **Query Speed** | Moderate (Indexed)           | Very High (Isolated)          |
| **Maintenance** | Easy (Single table)          | Complex (Many tables)         |
| **Calculation** | On-the-fly (API)             | Pre-stored (Ingestion)        |
