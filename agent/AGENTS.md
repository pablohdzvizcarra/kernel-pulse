# kernel-pulse — Modular Agent System

> This file is the root index for all agent documentation modules. Each module lives in its own folder under `.agent/` and is self-contained with its own README, plans, and decision logs.

---

## Module Index

| Module | Path | Status | Description |
|:---|:---|:---|:---|
| **Context** | [`.agent/context/`](./context/) | ✅ Active | Core project concepts and architecture overview |
| **Skills** | [`.agent/skills/`](./skills/) | ✅ Active | Reusable agent skill definitions (TelemetryMentor, etc.) |
| **Learning** | [`.agent/learning/`](./learning/) | ✅ Active | Pending and completed learning items |
| **Metrics Collector** | [`.agent/metrics-collector/`](./metrics-collector/) | ✅ Complete | Implementation plans for the core metrics collection features |
| **Delta Calculation** | [`.agent/delta-calculation/`](./delta-calculation/) | 🟡 Planning | Delta computation for counters (network bytes) and gauges (free RAM) |
| **Agents** | [`.agent/agents/`](./agents/) | 📋 Reserved | Agent role definitions (future use) |
| **Workflow** | [`.agent/workflow/`](./workflow/) | 📋 Reserved | CI/CD and development workflow documentation |

---

## How This System Works

Each feature or concern gets its own folder under `.agent/`. Every module folder follows a consistent structure:

```
.agent/<module-name>/
├── README.md                  # Module overview, purpose, and document index
├── implementation-plan.md     # Technical plan for code changes
├── theory.md                  # Conceptual background (if applicable)
├── decisions.md               # Design decisions and trade-offs log
└── ...                        # Additional module-specific documents
```

### Conventions

1. **README.md is the entry point** — Always start reading a module from its README.
2. **Status badges** — Each module tracks its status: 📋 Reserved → 🟡 Planning → 🔵 In Progress → ✅ Complete
3. **Cross-references** — Documents link to related modules, skills, and learning items.
4. **Decisions are logged** — Non-obvious choices are recorded in `decisions.md` with date, context, and rationale.
5. **Implementation plans match code** — Plans reference actual file paths in `src/` and include verifiable test cases.

---

## Feature Development Flow

```
1. Create module folder:  .agent/<feature-name>/
2. Write theory.md:       Understand the concept first
3. Write decisions.md:    Record key design choices
4. Write plan.md:         Detail every file change
5. Get user approval:     Review the plan
6. Implement:             Write the code
7. Verify:                Run tests, manual checks
8. Update status:         Mark module as ✅ Complete
```
