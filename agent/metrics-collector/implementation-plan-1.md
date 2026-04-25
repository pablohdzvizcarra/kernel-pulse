# Telemetry Metrics Refactoring

Based on the research from `.agent/learning/pending-learning-items.md` and `.agent/skills/telemetry-mentor.md`, the best approach for expanding our metrics collection (e.g., adding network bytes) is to use a **generic `Sample` class with tags** instead of subclasses. This follows the narrow, dimensional time-series data modeling pattern.

## User Review Required

> [!WARNING]
> This refactor will modify the `Sample` class to include a `tags` map. This will require updating the `DatabaseManager` to properly insert and retrieve these tags (likely as a JSON string in a new `tags` TEXT column) to fully comply with the theory. We'll start with updating `Sample` and `Collector`, but `DatabaseManager` will also need modifications to not break the application.
> Are you okay with updating the `DatabaseManager` schema to rename `memory_samples` to a generic `metrics` table and adding a `tags` column?

## Proposed Changes

### `Sample.java`

#### [MODIFY] `src/main/java/com/github/pablohdzvizcarra/metric/Sample.java`
- Introduce a `Map<String, String> tags` field.
- Add an overloaded constructor to allow creating samples without tags (which defaults to an empty map) to maintain backward compatibility, and another that takes the `tags` map.
- Add a `getTags()` getter method.
- Update `toString()` to include tags.

### `Collector.java`

#### [MODIFY] `src/main/java/com/github/pablohdzvizcarra/Collector.java`
- While this file currently only reads `/proc/meminfo`, we can prepare it for tags by using the updated `Sample` constructor (either passing an empty map or no map, depending on the constructors provided).
- *Optional Note for later execution:* We can refactor `Collector` into an interface and have a `MemoryCollector` and `NetworkCollector` that both produce `Sample` objects with appropriate tags. But for now, we will just update it to be compatible with the new `Sample` structure.

### `DatabaseManager.java`

#### [MODIFY] `src/main/java/com/github/pablohdzvizcarra/DatabaseManager.java`
- Update the table creation query to: `CREATE TABLE IF NOT EXISTS metrics (timestamp INTEGER PRIMARY KEY, metric_name TEXT, value REAL, tags TEXT);`
- Update the index query to use a composite index: `CREATE INDEX IF NOT EXISTS idx_metrics_name_time ON metrics(metric_name, timestamp);`
- Update `insertSample()` to serialize the `tags` Map into a JSON string and insert it.
- Update `getSamples()` to parse the `tags` JSON string back into a Map when recreating `Sample` objects.

## Verification Plan

### Automated Tests
- Run `mvn clean compile` to ensure all Java classes compile properly.
- Verify `DatabaseManager` can successfully insert and query samples with tags using `sqlite3` CLI or a small demo script.

### Manual Verification
- Review the `/home/ubuntu/GitHub/kernel-pulse/.agent/metrics-collector/sample-refactor-research.md` document for the theoretical justification.
