# Project Context Module

> **Status:** ✅ Active

## Purpose
This module defines the core concepts, terminology, and high-level architecture of the **kernel-pulse** telemetry system. It is designed to get any agent or developer up to speed on how we handle time-series data in SQLite.

## Documents
- [**Architecture & Concepts**](./context.md) — Understanding samples, sampling rates, and performance database patterns.

## Quick Reference
| Concept | Definition |
| :--- | :--- |
| **Sample** | A single data point (Timestamp + Value). |
| **TSDB** | Time-Series Database (The category this workflow falls into). |
| **Narrow Schema** | Our database format: `(timestamp, metric_name, value, tags)`. |
