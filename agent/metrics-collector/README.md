# Metrics Collector Module

> **Status:** ✅ Complete

## Purpose
This module contains the implementation history and design research for the core metrics collection engine. It documents the transition from simple RAM monitoring to a generic, tagged telemetry pipeline.

## Implementation History
- [**Phase 1: Metrics Refactoring**](./implementation-plan-1.md) — Moving to a generic `Sample` class with tags and a narrow database schema.
- [**Phase 2: Network Collection**](./implementation-plan-2.md) — Implementing `/proc/net/dev` parsing for read/write bytes per interface.

## Research
- [**Sample Design Research**](./sample-refactor-research.md) — Comparison between using Subclasses vs. Tags for metrics.

## Current Capabilities
- **RAM Monitoring**: Reads `MemFree` from `/proc/meminfo`.
- **Network Monitoring**: Reads `Receive/Transmit` bytes per interface from `/proc/net/dev`.
- **Tagged Storage**: All metrics are stored with metadata (e.g., `interface: "enp0s6"`).
