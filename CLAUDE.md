# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build

```bash
mvn clean package   # outputs target/PrismaUtils-1.0-SNAPSHOT.jar
```

Java 21, Paper API 1.21. No test suite — verification is manual against a Paper server.

## Architecture

**Entry point**: `PrismaUtils.java` — `onEnable()` runs initialization in order:
1. Load all config files via `ConfigManager`
2. Build `FeatureToggleManager` from `features.yml`
3. Initialize services (`ProtectionService`, `TerritoryService`, `SitService`)
4. Initialize core managers (`PlayerDataManager`, `CooldownManager`, etc.)
5. Conditionally init feature managers (`AfkManager`, `FlightManager`, `ChatHandler`) based on toggles
6. Register PlaceholderAPI expansions
7. Register commands via `CommandManager`
8. Register event listeners via `EventManager`

**Commands** all extend `BaseCommand`, which handles permission checks, console-block, and usage display. The concrete class only implements `onCommandExecute()` and optionally `onTabCompleteExecute()`. Commands are declared in `plugin.yml` and registered in `onEnable()` inside a feature-toggle check.

**Handlers** implement Bukkit's `Listener` and are registered through `EventManager.registerFeatureListeners(Feature, Listener)`.

**Feature toggles** are the central control mechanism. Every new command or handler needs:
- An entry in the `FeatureToggleManager.Feature` enum
- A default entry in `features.yml`
- Registration wrapped in `if (featureToggleManager.isEnabled(Feature.X))`

**Configuration** is split across multiple YAML files (`config.yml`, `messages.yml`, `chat.yml`, `afk.yml`, `warps.yml`, `death_messages.yml`). Each has a dedicated config-manager class under `managers/config/`. All user-facing strings come from `messages.yml` and are deserialized via MiniMessage using `TextUtils.deserializeString()`.

**Player data** is persisted to `plugins/PrismaUtils/playerdata/{UUID}.yml` by `PlayerDataManager`, which keeps an in-memory cache and auto-saves asynchronously every 5 minutes.

**Integration with other plugins** is isolated in `services/` (`ProtectionService` → WorldGuard, `TerritoryService` → Towny, `SitService` → GSit). These are optional soft-dependencies.

## Adding a new command

1. Create class in the appropriate `commands/` subpackage extending `BaseCommand`
2. Add the command definition to `plugin.yml`
3. Add a `Feature` enum value to `FeatureToggleManager` and a default in `features.yml`
4. Register in `PrismaUtils.onEnable()` inside the corresponding feature check

## Adding a new event handler

1. Create class in `handlers/` implementing `Listener`
2. Add a `Feature` enum value and default in `features.yml`
3. Call `eventManager.registerFeatureListeners(Feature.X, new YourHandler(...))` in `onEnable()`

## Key conventions

- All messages to players use MiniMessage format and come from `messages.yml`
- Cooldowns are tracked centrally through `CooldownManager`; bypass permission is `prismautils.cooldown.bypass`
- The singleton pattern (`ConfigManager.getInstance()`, `CooldownManager.getInstance()`, etc.) is used for shared state — don't instantiate these directly
- Optional plugin integrations should be guarded by availability checks (see `ProtectionService` for the pattern)