# Telemetry Metrics Research: Generic Sample vs. Subclasses

## Question
Is it better to create a `Sample` subclass to represent the Free RAM Memory metric, or should we have a generic `Sample` and just change the metric name or add some Tags?

## Context
The goal is to expand the system to collect not only Free RAM but also the number of bytes read/written by the server's network card.

## Analysis
Based on the `telemetry-mentor.md` and `pending-learning-items.md` resources:

1. **Dimensionality / Tagging over Hardcoding Contexts:**
   According to the theory (Topic 4 in `pending-learning-items.md`), modern telemetry avoids hardcoding contexts into metric names or classes. Instead of creating specific structures like `SampleMemoryFree` or naming metrics `network_bytes_enp0s6`, a metric like `network_bytes_read` should be associated with key-value tags or dimensions (e.g., `interface="enp0s6"`). This allows powerful aggregations.

2. **Narrow (Tall) Schema Extensibility:**
   The `telemetry-mentor.md` points out that the database already uses a narrow schema (`timestamp, metric_name, value`). The power of this format is that adding a new metric requires **zero** schema changes. If we start subclassing `Sample`, we break the generic nature of our collector pipeline and would likely need different tables or complex Object-Relational Mapping (ORM) to handle the subclasses in SQLite.

3. **Tags Column Implementation:**
   The mentor documentation specifically proposes adding a `tags` column (as a `TEXT` field storing JSON) to the database schema. This perfectly handles scenarios where different metrics have different contextual data without changing the Java class structure.

## Conclusion
**Do not create a `Sample` subclass.** It is far better to keep a **generic `Sample` class** and introduce a `tags` property (e.g., `Map<String, String>` in Java). 

For the RAM metric, the `metricName` will be `"memory_free"` and tags can be empty.
For the network metric, the `metricName` will be `"network_bytes_read"` with a tag of `{"interface": "enp0s6"}`. 

This approach keeps the Collector and Database pipelines unified and flexible for any future metrics.
