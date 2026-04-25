# Delta Calculation — Conceptual Theory

> This document explains the *why* behind delta calculations. For the *how* (code changes), see [implementation-plan.md](./implementation-plan.md).

---

## What Is a Delta?

A **delta** is the difference between two consecutive readings of the same metric:

```
delta = Value(T₂) - Value(T₁)
```

But what that difference *means* depends entirely on whether the metric is a **counter** or a **gauge**.

---

## Counters: Delta = Work Performed

A **cumulative counter** only goes up. It represents a running total since some epoch (usually system boot). Examples from kernel-pulse:

- `network_bytes_read` — total bytes received on an interface since boot
- `network_bytes_written` — total bytes sent on an interface since boot

The absolute value of a counter is rarely useful on its own ("the system has sent 47 GB since boot" — so what?). The *delta* between two readings tells you how much work happened in that interval:

```
bytes_transferred = network_bytes_read(T₂) - network_bytes_read(T₁)
```

If the collector runs every 60 seconds and the delta is 1,048,576 bytes, you know **1 MB was received in the last minute**.

### Wraparound

Counters are stored in fixed-size integers. In `/proc/net/dev`, the kernel uses unsigned 64-bit integers. When the value reaches `2⁶⁴ - 1` (about 18.4 exabytes), it rolls back to 0. This is called **wraparound**.

**Detection:** `Value(T₂) < Value(T₁)` (assuming a single wraparound, not a system restart).

**Correct delta with wraparound:**
```
delta = (MAX_VALUE - Value(T₁)) + Value(T₂) + 1
```

**In practice with Java `long`:** Java's `long` is 64-bit signed (max ~9.2 × 10¹⁸). At 10 Gbps continuously, it would take ~29 years to wrap around. So wraparound is essentially theoretical for our use case, but we should handle it for correctness.

> **Key insight from the learning items:** In languages with unsigned arithmetic (C/C++), subtraction handles wraparound automatically. In Java, we need explicit logic since `long` is signed.

---

## Gauges: Delta = Change in State

A **gauge** can go up or down. It represents a snapshot of current state. Example from kernel-pulse:

- `memory_free` — how much RAM is currently free (in kB)

### Can You Calculate Delta for a Gauge?

**Yes, but it means something different.**

| | Counter Delta | Gauge Delta |
|:---|:---|:---|
| Always positive? | Yes (except wraparound) | **No** — can be negative |
| What it means | Work done in the interval | Change in state over the interval |
| Example | "1 MB was transferred" | "Free memory decreased by 200 MB" |
| Wraparound? | Possible | **Not applicable** — gauges don't wrap |

A gauge delta tells you:
- **Positive delta:** The resource *increased* (e.g., memory was freed)
- **Negative delta:** The resource *decreased* (e.g., memory was consumed)
- **Zero delta:** No change

### When Is Gauge Delta Useful?

- **Trend analysis:** "Is memory trending down over the last hour?" → compute deltas and check if they're consistently negative.
- **Alerting:** "Did free memory drop by more than 500 MB in a single interval?" → check if any delta is < -500,000 kB.
- **Rate of change:** "How fast is memory being consumed?" → `delta / elapsed_time` gives you kB/second.

### When Is Gauge Delta NOT Useful?

- **Total work measurement:** You can't sum gauge deltas to get "total memory consumed" — that doesn't make physical sense because memory can be freed and re-consumed multiple times.
- **Missed spikes:** If memory drops from 4 GB → 100 MB → 4 GB between two samples, the delta shows 0 (no change), completely missing the spike. This is inherent to gauges.

---

## Summary Decision for kernel-pulse

| Metric | Type | Implement Delta? | Rationale |
|:---|:---|:---|:---|
| `network_bytes_read` | Counter | ✅ **Yes** | Delta = bytes received in the interval. The primary use case. |
| `network_bytes_written` | Counter | ✅ **Yes** | Delta = bytes sent in the interval. Same reasoning. |
| `memory_free` | Gauge | ✅ **Yes, with caveats** | Delta = change in free memory. Useful for trend analysis but must clearly communicate that it can be negative and doesn't represent "total work." |

---

## Connecting to the Code

The `DeltaCalculator` class will need two modes:

1. **Counter mode:** `delta = current - previous`. If `current < previous`, apply wraparound formula. Result is always ≥ 0.
2. **Gauge mode:** `delta = current - previous`. Result can be negative. No wraparound logic.

The `MetricType` enum will distinguish these two modes, and the calculator will choose the correct formula based on the type.
