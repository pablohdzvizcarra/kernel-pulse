# Design Decisions — Delta Calculation

This document records the key design decisions made during the delta calculation feature development.

---

## Decision 1: Support Gauge Deltas (Not Just Counter Deltas)

**Date:** 2026-04-25  
**Status:** ✅ Accepted

**Context:** The user questioned whether delta makes sense for `memory_free` (a gauge). Strictly speaking, deltas are most natural for counters. However, gauge deltas provide useful trend information.

**Decision:** Implement delta for both metric types, but use a `MetricType` enum to ensure the calculator applies the correct logic:
- **COUNTER:** Always returns a non-negative delta; applies wraparound handling.
- **GAUGE:** Returns the raw difference (can be negative); no wraparound.

**Consequence:** The `DeltaResult` must use `long` (not an unsigned type) to accommodate negative gauge deltas.

---

## Decision 2: MetricType as an Enum, Not Stored in the Database

**Date:** 2026-04-25  
**Status:** ✅ Accepted

**Context:** We could store the metric type in the `metrics` table or in a separate `metric_definitions` table. However, this adds schema complexity for only two current metrics.

**Decision:** Define `MetricType` as a Java enum with a static registry mapping `metricName → MetricType`. This keeps the database schema unchanged and centralizes metric metadata in code.

**Trade-off:** If metric types need to be configurable at runtime, this would need to be revisited. For now, metrics are hardcoded in collectors, so a code-level registry is consistent.

---

## Decision 3: DeltaCalculator as a Stateless Utility

**Date:** 2026-04-25  
**Status:** ✅ Accepted

**Context:** Delta calculation could live in the `Collector` (compute and store deltas at collection time) or in a separate utility (compute deltas on-demand from stored raw samples).

**Decision:** Implement `DeltaCalculator` as a stateless utility class that computes deltas from existing stored samples. The collectors continue to store raw values only.

**Rationale:**
- Raw values are always preserved — you can re-calculate deltas over any time window.
- No state management in collectors — they remain simple data producers.
- Deltas can be computed for any arbitrary pair of samples, not just consecutive ones.

---

## Decision 4: Query Deltas by Metric Name and Tags

**Date:** 2026-04-25  
**Status:** ✅ Accepted

**Context:** Network metrics have tags (`{"interface": "enp0s6"}`). When computing deltas for `network_bytes_read`, we need to compute deltas *per interface*, not across interfaces.

**Decision:** The `DatabaseManager.getSamplesByMetricAndTags()` method will filter by both `metric_name` and `tags` to ensure deltas are computed within the correct series.

**Consequence:** Requires a new query method in `DatabaseManager` beyond the existing `getSamples()`.
