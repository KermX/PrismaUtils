# 🔷 PrismaUtils
**A feature-rich, modular utility plugin for Minecraft servers running Paper 1.21.8**

PrismaUtils is an all-in-one server utility plugin designed for survival, SMP, and semi-vanilla servers. It provides essential quality-of-life features, administration tools, and gameplay tweaks—all configurable through a powerful feature toggle system.

## ✨ Features
### **Player Systems**
- **Homes** - Set, manage, and teleport to multiple home locations
- **Warps** - Server-wide warp points with admin management
- **Teleport Requests** - Player-to-player teleport system (TPA/TPAHere)
- **Mail System** - Send and receive messages to offline players
- **AFK System** - Auto-detection with teleportation and damage protection

### ⚡ **Teleportation & Movement**
- **Back Command** - Return to your last location after death or teleport
- **Spawn System** - Configurable spawn points with first-join support
- **Flight Management** - Toggle flight with temporary flight time support
- **Top/Bottom** - Quick teleportation to surface or ground level
- **Advanced TP Commands** - `/tp`, `/tphere`, `/tppos` with world support

### 🛠️ **Portable Workstations**
Open crafting interfaces anywhere:
- Crafting Table, Anvil, Enchanting Table, Smithing Table
- Stonecutter, Grindstone, Loom, Cartography Table
- Ender Chest access

### 🎮 **Gameplay Tweaks**
- **Silk Touch Spawners** - Mine spawners with silk touch
- **Climbable Chains** - Chains work like ladders
- **Copper Oxidation Control** - Wax/unwax copper with protection plugin support
- **Slime Split Control** - Configure slime/magma cube splitting
- **Horse Zombification** - Convert skeleton/zombie horses
- **Custom Death Messages** - Randomized, configurable death messages
- **Anti-Auto Fishing** - Prevent AFK fishing farms

### 👤 **Player Utilities**
- **God Mode** - Invulnerability toggle
- **Health & Hunger Restoration** - Quick healing commands
- **Item Management** - Repair, rename, condense/uncondense items
- **Personal Time/Weather** - Individual time and weather settings
- **Trash GUI** - Safe item disposal interface
- **Measure Tool** - Calculate distances between points

### 🔧 **Admin Tools**
- **Block/Entity/Item Info** - Detailed debug information
- **Player Management** - Cuff, smite, patrol online players
- **Mob Management** - Spawn mobs, clear entities with filters
- **Light Level Visualization** - Show spawnable blocks
- **Player Heads** - Give custom player skulls
- **Server Uptime** - Display server runtime

### 💬 **Chat Management**
- **Chat Filters** - Word filtering with staff notifications
- **Emoji System** - Custom emoji replacements with permissions
- **MiniMessage Support** - Rich text formatting
- **Command Monitoring** - Filter commands for inappropriate content

### 📊 **PlaceholderAPI Integration**
Custom placeholders for:
- AFK status and time
- Event countdowns and information
- Unix timestamps and time zones
- MiniMessage formatting

---

## 📋 Requirements

- **Server Software**: [Paper](https://papermc.io/) 1.21.8+ (or any Paper fork)
- **Java Version**: 21 or higher
- **Required Dependencies**:
    - [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) *(required)*
- **Optional Dependencies**:
    - [Towny](https://github.com/TownyAdvanced/Towny) - For territory-based flight restrictions
    - [WorldGuard](https://enginehub.org/worldguard) - For region protection integration
    - [GSit](https://www.spigotmc.org/resources/gsit-modern-sit-seat-and-chair-lay-and-crawl-plugin-1-13-x-1-21-x.62325/) - For sitting integration

---

## 🚀 Installation

1. **Download** the latest `PrismaUtils.jar`
2. **Place** the JAR file in your server's `plugins/` folder
3. **Install** PlaceholderAPI (required dependency)
4. **Restart** your server
5. **Configure** the plugin files in `plugins/PrismaUtils/`
6. **Reload** configs with `/prismautilsreload` or restart again

---

## ⚙️ Configuration

PrismaUtils uses a modular configuration system with multiple files:

| File                 | Purpose                                                   |
|----------------------|-----------------------------------------------------------|
| `features.yml`       | **Master feature toggle** - Enable/disable entire systems |
| `config.yml`         | Main plugin settings (spawns, cooldowns, tweaks)          |
| `messages.yml`       | All user-facing messages and text                         |
| `chat.yml`           | Chat filter and emoji configuration                       |
| `afk.yml`            | AFK system settings and location                          |
| `warps.yml`          | Warp point definitions                                    |
| `death_messages.yml` | Custom death message templates                            |
| `events.yml`         | Event placeholder configurations                          |