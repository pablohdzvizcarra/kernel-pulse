# Workflow Module

> **Status:** ✅ Active

## Purpose
This module defines the CI/CD and development lifecycle standards for the project, including the required workflow for all code modifications made by the agent.

## Code Modification Workflow

As mandated by the Control Plane (`agent/AGENTS.md`), all code changes MUST be documented.

### Standard Operating Procedure for Code Changes:
1. **Identify the Target Module:** Determine which `agent/<module>/` directory corresponds to the codebase you are modifying.
2. **Create if Missing:** If no appropriate module exists for the code being changed, create a new one:
   - Create a new directory `agent/<module-name>/`.
   - Add a `README.md` to define its purpose.
3. **Document the Change:** Inside the targeted module, document the change:
   - Append to an existing `changes.md` or `implementation-plan.md` file.
   - If neither exists, create a `changes.md` file.
   - The documentation should explain **what** files were modified, **what** changes were made, and **why** they were necessary.
4. **No Explicit Request Needed:** Perform these steps automatically for every single code change. Do not wait for the user to ask for documentation.
