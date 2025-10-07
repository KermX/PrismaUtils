# PrismaUtils – Development TODO List

_Last Updated: 2025-10-07_

## Quick Links
- [🔴 Critical Issues](#🔴-critical-issues)
- [🟠 High Priority](#🟠-high-priority)
- [🟡 Medium Priority](#🟡-medium-priority)
- [⚪ Low Priority](#⚪-low-priority)
- [🧭 Developer Notes](#🧭-developer-notes)

---

## 🔴 Critical Issues

### Structure & Architecture
- [ ] **[#1]** Refactor **ChatHandler** into separate listener and manager
    - Location: `handlers/player/ChatHandler.java`
    - Priority: CRITICAL
    - Details: Split into `ChatEventHandler` (listener) and `ChatManager` (business logic).
    - Notes: Reduces coupling; aligns with `AfkManager`, `FlightManager`, etc.

- [ ] **[#2]** Fix **ChatHandler** instantiation in `PrismaUtils.onEnable()`
    - Location: `PrismaUtils.java:onEnable()`
    - Priority: CRITICAL
    - Details: Currently instantiated in a handler package but behaves as a manager; unify with manager/service architecture.

### Configuration & Data Management
- [ ] **[#3]** Add robust error handling and defaults for config loading
    - Location: `config/` package
    - Priority: CRITICAL
    - Details: Ensure every configuration key has a safe fallback; log missing values clearly.
    - Related: Future tie-in with automatic config updater.

### Logging, Commands, & Admin Tools
- [ ] **[#4]** Implement command rate limiting
    - Location: `commands/core`
    - Priority: CRITICAL
    - Details: Prevent spam or abuse; configurable limits via `config.yml`.

### Infrastructure & Performance
- [ ] **[#5]** Improve shutdown and cleanup logic in `onDisable()`
    - Location: `PrismaUtils.java:onDisable()`
    - Priority: CRITICAL
    - Details: Properly cancel all async tasks and save states.
    - Affected: `AfkManager`, `FlightManager`, `ChatHandler`, `TeleportRequestManager`.

- [ ] **[#6]** Implement proper UUID → name resolution with caching
    - Location: `util/UUIDUtils.java` or new `services/PlayerLookupService.java`
    - Priority: CRITICAL
    - Details: Cache Mojang API responses; auto-invalidate stale mappings.

---

## 🟠 High Priority

### Structure & Architecture
- [ ] **[#7]** Create `models/` package for data classes
    - Move: `Home`, `MailMessage`, `TeleportRequest`
    - Goal: Separate data models from managers.
    - Estimated Time: 2–3 hours.

- [ ] **[#8]** Extract spawn/location logic into dedicated `LocationService`
    - Current: Within config managers
    - Result: Easier cross-feature use of spawn handling.

### Configuration & Data Management
- [ ] **[#9]** Add configurable options for `AntiAutoFishingHandler`
    - Details: Allow enabling/disabling detection, thresholds, and notifications.
    - Priority: HIGH.

- [ ] **[#10]** Implement internal player data backup system
    - Location: `managers/data/PlayerDataManager.java`
    - Priority: HIGH
    - Details: Auto-backup before each save; retain last 5; add restore command.

### Gameplay & Features
- [ ] **[#11]** Finalize or remove `SpawnerInfoHandler`
    - Location: `handlers/block/SpawnerInfoHandler.java`
    - Priority: HIGH
    - Details: Display mob spawn conditions on interaction; delete if not used.

### Utilities & Support Code
- [ ] **[#12]** Clean up util classes
    - Move time utilities from `TextUtils` → new `TimeUtils`.
    - Ensure all utils follow static-only or singleton pattern.

### Logging, Commands, & Admin Tools
- [ ] **[#13]** Improve overall logging clarity and consistency
    - Replace arbitrary `System.out` or raw `Logger` calls with a unified `Log` utility.
    - Use colorized console output for dev mode.

- [ ] **[#14]** Fix patrol command
    - Location: `commands/admin/PatrolCommand.java`
    - Priority: HIGH
    - Details: Currently fails to locate next player; verify player iteration logic.

---

## 🟡 Medium Priority

### Structure & Architecture
- [ ] **[#15]** Separate handlers by lifecycle
    - e.g., `gameplay/`, `persistence/`, `infra/` instead of by entity type.
    - Goal: Improve logical flow and reduce cross-package references.

### Configuration & Data Management
- [ ] **[#16]** Add command alias configuration
    - Location: `config/commands.yml`
    - Priority: MEDIUM
    - Details: Let admins define multiple aliases per command.

- [ ] **[#17]** Implement per-world feature toggles
    - Example: Disable flight in specific worlds via config.
    - Priority: MEDIUM.

- [ ] **[#18]** Improve automatic config updating system
    - Model after Towny’s `ConfigUpdater`.
    - Priority: MEDIUM.

### Logging, Commands, & Admin Tools
- [ ] **[#19]** Add admin notifications for critical events
    - e.g., player data load failures, config corruption.
    - Send to Discord or console depending on settings.

### Infrastructure & Performance
- [ ] **[#20]** Add permission caching (optional)
    - Priority: MEDIUM
    - Details: Cache player permissions for short durations to reduce lookups.

---

## ⚪ Low Priority

### Gameplay & Features
- [ ] **[#21]** Consolidate `RestoreHealthCommand` and `RestoreHungerCommand`
    - Create unified `RestoreCommand` with subcommands.
    - Priority: LOW.

### Utilities & Support Code
- [ ] **[#22]** General utility cleanup and refactoring
    - Remove duplicate helper methods; add Javadoc where missing.

---

## 🧭 Developer Notes

### Priority Levels

| Tag               | Definition                                                                                                                                                                                                                          | Core Impact                                                        | Typical Examples                                                                         |
|-------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------|------------------------------------------------------------------------------------------|
| 🔴 **[CRITICAL]** | Tasks that directly affect **plugin stability, data integrity, or core architecture**. If unaddressed, these can cause runtime errors, crashes, or data corruption. These are required for safe operation or long-term code health. | Foundational; must be done before stable releases.                 | Core architecture fixes, shutdown logic, config safety, rate limiting, UUID caching      |
| 🟠 **[HIGH]**     | Tasks that **complete major systems, refactor critical structures, or significantly improve maintainability**. Not immediately breaking, but block further progress or degrade player experience if left undone.                    | High; required to unlock new features or ensure maintainable code. | Data backup system, LocationService extraction, logging consistency, unfinished handlers |
| 🟡 **[MEDIUM]**   | Enhancements that improve **developer workflow, maintainability, or configuration flexibility**. Useful for polish and iteration but not required for stability.                                                                    | Moderate; improves usability and flexibility.                      | Command alias config, per-world toggles, admin alerts, handler organization              |
| ⚪ **[LOW]**       | Optional improvements for **cleanup, structure clarity, or long-term code quality**. Can be done during low activity or cleanup phases.                                                                                             | Low; deferred until after all higher priorities.                   | Utility refactors, manager-to-service conversions, minor command consolidations          |

### Contribution Guidelines
1. Add new tasks under the correct **priority** and **category**.
2. Include:
    - File location(s)
    - Priority
    - Short description
    - Any related doc references
3. Mark tasks `[x]` when complete, and move to **Completed** with a date.
4. Keep entries concise but clear enough for new contributors.

---
