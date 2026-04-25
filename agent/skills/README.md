# Agent Skills Module

> **Status:** ✅ Active

## Purpose
This module contains reusable "Skills" or System Prompts that define specialized agent personas for this project. These skills ensure the agent behaves consistently when teaching or implementing specific features.

## Available Skills
- [**TelemetryMentor**](./telemetry-mentor-skill.md) — A Socratic tutor skill for teaching Linux performance monitoring, counters, gauges, and SQLite indexing.

## Usage
When an agent starts a session, it should read the relevant skill file to adopt the required persona and knowledge base.
