# Kernel Pulse

Java application to create Metrics for a Linux Operating System.


## How to execute the Application in Background

```bash
nohup java -jar target/kernel-pulse-1.0.0.jar > app.log 2>&1 &
```

## How to Read SQLite Metrics with Command Line

```bash
sqlite3 metrics.db "SELECT * FROM metrics;"
```

```bash
sqlite3 metrics.db "SELECT * FROM metrics WHERE metric_name LIKE 'network_bytes_%';"
```

```bash
sqlite3 metrics.db "SELECT * FROM metrics WHERE metric_name = 'network_bytes_read' AND json_extract(tags, '$.interface') = 'enp0s6';"
```