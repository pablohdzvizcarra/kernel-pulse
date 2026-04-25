# 🛰️ Kernel Pulse — Modular Agent System

> This file serves as the **Control Plane** for all agent documentation. Each module is a self-contained ecosystem within its own directory, following a standardized modular architecture to prevent documentation bloat.

---

## 📂 Module Registry

| Module                | Location                                           | Status     | Core Responsibility                               |
| :-------------------- | :------------------------------------------------- | :--------- | :------------------------------------------------ |
| **Project Context**   | [`agent/context/`](./context/)                     | ✅ Active   | High-level architecture and time-series concepts. |
| **Agent Skills**      | [`agent/skills/`](./skills/)                       | ✅ Active   | Specialized capabilities (e.g., TelemetryMentor). |
| **Learning Path**     | [`agent/learning/`](./learning/)                   | ✅ Active   | Theoretical knowledge base and progress tracking. |
| **Metrics Collector** | [`agent/metrics-collector/`](./metrics-collector/) | ✅ Complete | Ingestion logic for RAM and Network telemetry.    |
| **Delta Calculation** | [`agent/delta-calculation/`](./delta-calculation/) | ✅ Complete | Computation of rates, ratios, and counter deltas. |
| **Agents**            | [`agent/agents/`](./agents/)                       | 📋 Reserved | Future agent role definitions and personas.       |
| **Workflow**          | [`agent/workflow/`](./workflow/)                   | 📋 Reserved | CI/CD and development lifecycle standards.        |

---

## 🛠️ The Modular Documentation Pattern

To avoid creating "Big and Complex" files, this project follows the **Atomic Documentation** principle:

### 1. The Directory is the Module
Every major feature or concern has its own folder. Never add a new top-level `.md` file if it can be grouped into a module.

### 2. The `README.md` is the Entry Point
Each folder **MUST** contain a `README.md`. This file acts as the "receptionist" for that module, indexing all internal documents (Theory, Plans, Decisions).

### 3. Cross-Module Referencing
Modules should link to each other rather than duplicating information. For example, the *Delta Calculation* plan links to *Agent Skills* for theoretical context.

---

## 🚀 Feature Development Workflow

When starting a new feature, follow this modular flow:

1.  **Initialize**: Create `agent/<feature-name>/`.
2.  **Theory**: Write `theory.md` to understand the *why*.
3.  **Decisions**: Record design trade-offs in `decisions.md`.
4.  **Plan**: Draft the technical changes in `implementation-plan.md`.
5.  **Build & Verify**: Implement the code and run tests.
6.  **Close**: Update the module `README.md` and mark the status as ✅ Complete in this index.
****