# Delta Calculation Feature Module

> **Status:** 🟡 Planning  
> **Created:** 2026-04-25  
> **Related Skill:** [telemetry-mentor-skill.md](../skills/telemetry-mentor-skill.md) — Topics 1 & 2  
> **Related Learning:** [pending-learning-items.md](../learning/pending-learning-items.md) — Sections 1 & 2

---

## Purpose

This module adds **delta calculation** capabilities to kernel-pulse. A delta is the difference between two consecutive samples of a metric, and its meaning changes depending on the metric type:

| Metric Type | Delta Meaning | Example |
|:---|:---|:---|
| **Counter** (monotonically increasing) | Work performed in the interval | Network bytes transferred in the last minute |
| **Gauge** (goes up and down) | Change in state over the interval | Memory freed or consumed in the last minute |

---

## Documents in This Module

| File | Purpose |
|:---|:---|
| [theory.md](./theory.md) | Conceptual background: why delta works differently for counters vs gauges |
| [implementation-plan.md](./implementation-plan.md) | Step-by-step technical plan for the code changes |
| [decisions.md](./decisions.md) | Design decisions and trade-offs recorded during development |

---

## Metrics Covered

1. **`network_bytes_read` / `network_bytes_written`** — Cumulative counter from `/proc/net/dev`. Delta = bytes transferred in the interval. Wraparound handling required (theoretically).
2. **`memory_free`** — Gauge from `/proc/meminfo`. Delta = change in free memory. Can be negative (memory consumed) or positive (memory freed).

---

## Quick Navigation

- **Want to understand the theory?** → Start with [theory.md](./theory.md)
- **Want to see the code plan?** → Jump to [implementation-plan.md](./implementation-plan.md)
- **Want to see design rationale?** → Check [decisions.md](./decisions.md)
