# 🛰️ Kernel Pulse — Modular Agent System

> This file serves as the **Control Plane** for all agent documentation. Each module is a self-contained ecosystem within its own directory, following a standardized modular architecture to prevent documentation bloat.

---

## 📂 Module Registry

| Module                   | Location                                           | Status     | Core Responsibility                               |
| :----------------------- | :------------------------------------------------- | :--------- | :------------------------------------------------ |
| **Project Architecture** | [`agent/architecture/`](./architecture/)           | ✅ Active   | High-level architecture and time-series concepts. |
| **Agent Skills**         | [`agent/skills/`](./skills/)                       | ✅ Active   | Specialized capabilities (e.g., TelemetryMentor). |
| **Learning Path**        | [`agent/learning/`](./learning/)                   | ✅ Active   | Theoretical knowledge base and progress tracking. |
| **Metrics Collector**    | [`agent/metrics-collector/`](./metrics-collector/) | ✅ Complete | Ingestion logic for RAM and Network telemetry.    |
| **Delta Calculation**    | [`agent/delta-calculation/`](./delta-calculation/) | ✅ Complete | Computation of rates, ratios, and counter deltas. |
| **Research**             | [`agent/research/`](./research/)                   | ✅ Active   | Industry patterns and alternative design studies. |
| **Agents**               | [`agent/agents/`](./agents/)                       | 📋 Reserved | Future agent role definitions and personas.       |
| **Workflow**             | [`agent/workflow/`](./workflow/)             ****  | ✅ Active   | CI/CD and development lifecycle standards.        |

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

## 📖 Research Documentation Guidelines

1. **Always Record Research**: Any new research performed MUST be recorded in a markdown file.
2. **Auto-Generate Markdown**: Always generate the research into a markdown file automatically without asking for manual confirmation from the user.
3. **Module Placement**: If you don't find an existing module in the `agent/` folder that is related to the research, create a new module (directory with a `README.md`) for it.

---

## 📝 Code Modification Guidelines

1. **Always Document Code Changes**: EVERY TIME you make a code change in a file for ANY reason (e.g., bug fix, new feature, refactoring), you MUST automatically document the detailed code change into an appropriate `agent/` module.
2. **Default Behavior**: You must perform this documentation step by default for EVERY request, without requiring the user to explicitly ask for it.
3. **Module Placement**: Document the change in the specific module (directory) that is related to the code being modified (e.g., appending to a `changes.md` or `implementation-plan.md` file). If an appropriate module does not exist for the modified code, you MUST create a new module (a new directory with a `README.md` and a `changes.md` file) to track it.

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