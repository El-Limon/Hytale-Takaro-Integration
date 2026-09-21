# Changelog

All notable features added to the Hytale-Takaro Integration Mod.

---

## 1.14.6-elimon.3

### Moderation
- **F16** - `listBans` sent `player.name: null` for a ban whose target is not in the
  `known-players.json` ledger (any ban issued by UUID, and every ban that predates the ledger).
  Takaro validates `BanDTO.player.name` with `isString` and rejected the **entire** response:
  `gameserverListBans` answered HTTP 400 "The gameserver responded with bad data, please verify
  that the mod is up to date" while the ban was correctly stored in Hytale's `bans.json`.
  `player.name` now falls back to the gameId and is never null.

---

## 1.14.6-elimon.2

Found during the live re-proof of `1.14.6-elimon.1` on a real Hytale 0.6.8 server.

### Moderation
- **F14** - every `takaro <sub>` console helper that takes a player (`banplayer`, `unbanplayer`,
  `kickplayer`, `getplayerlocation`, `getplayerinventory`, `give`, `tp`, `tpp`, `setcolor`, `beds`)
  resolved its argument by **online username only**, so `takaro banplayer <uuid>` answered
  `Player not found` and offline moderation from the console was impossible - exactly the case the
  new offline `banPlayer` implementation was written for. The argument is now resolved as
  UUID -> online username -> the `known-players.json` ledger.

---

## 1.14.6-elimon.1

Fix pass following a hard test of `mad-001/Hytale-Takaro-Integration` @ `ba872f1` against a real
Hytale **0.6.8** dedicated server. **None of these fixes has been re-proven live yet** - see the
status table in the README.

### Compatibility
- Compiles against Hytale 0.6.8 again. Upstream's last functional commit (2026-01-31) failed with
  72 javac errors across 24 call sites: `org.joml` vectors replacing `com.hypixel.hytale.math.vector`,
  `Universe.getPlayers()` returning a `Collection`, `PacketHandler.getChannel()` returning a
  `ChannelConnection`, `disconnect(Message)`, `Teleport`'s `Rotation3fc` rotation,
  `CommandSender.getUsername()`, and the removal of `Inventory.getCombinedEverything()`.
- `getPlayerInventory` covers all six inventory sections again (armor, hotbar, utility, storage,
  backpack, tools) by building a `CombinedItemContainer` explicitly.
- Manifest `ServerVersion` is now `>=0.6.8` and matches the pom version.

### Moderation
- `banPlayer` / `unbanPlayer` were stubs that always returned `success:true`; `listBans` returned `[]`.
  All three are now real, on Hytale's `AccessControlModule`: offline bans by UUID, `expiresAt` honoured,
  the target kicked if online, and the state read back before success is reported.
- `shutdown` is handled (it previously fell through to "Unknown action").

### Correctness
- `executeConsoleCommand` always returns a string `rawResult`. Six shortcut names used to return the
  raw action payload, which Takaro rejected with a 400.
- Takaro's console helpers moved to a `takaro <sub>` namespace so they can no longer shadow real
  Hytale commands.
- Console output is captured per invocation via a capturing `CommandSender` instead of subscribing to
  the global server logger, so another user's output can no longer be returned as yours.
- An unknown console command now returns `success:false` with the game's own message.
- Every optional argument goes through one null-safe helper: an explicit JSON `null` from a module no
  longer throws. `giveItem` honours `amount` and reports an unsettable `quality` instead of dropping it.
- `getPlayerLocation` no longer answers `0,0,0` on failure, and `getPlayerInventory` no longer answers
  `[]` on failure.
- `getPlayer` answers for offline players from a persisted known-players ledger.
- `getServerInfo` reports the real MOTD, version and player counts.

### Reliability
- Game events produced while Takaro is unreachable are queued (`EVENT_QUEUE_SIZE`, default 1000) and
  flushed after re-identify, instead of being dropped. Oldest-first drop with a logged counter.
- A rejected identify is retried with backoff and the real reason is logged, instead of silently ending
  all reconnect attempts. The config file is re-read before every attempt.
- Connection logging reflects reality (socket-open vs identified).
- Requests run on their own bounded pool, not on the WebSocket reader thread.
- The disconnect-dedupe map and the log-forward buffer are bounded; the log buffer no longer drains
  quadratically.

### Observability
- `TAKARO_DEBUG=true` logs every frame with its `requestId` at INFO, through a logger that is excluded
  from log forwarding - which is what makes it safe (raising the old FINE logging would have created a
  log -> gameEvent -> log amplification loop).
- `LOG_FORWARD_LEVEL` and `LOG_FORWARD_MAX_PER_MIN` bound log forwarding.

### Content
- `entity-killed` is emitted when a player kills a mob, with the killer attributed; player deaths caused
  by another player now carry an `attacker`. Hytale 0.6.8 records no weapon, so `weapon` is empty.
- `listEntities` returns spawnable NPC role templates with display names.
- `listLocations` returns the server's named warps.
- `listItems` filters `Debug_*`/`Test_*`/`Dev_*` (`CATALOG_INCLUDE_DEBUG=true` keeps them) and includes
  descriptions where a translation exists.

### Privacy
- HytaleCharts is strictly opt-in: no secret placeholder, promo-on-login off, no boot-time nag, and one
  explicit line stating what is sent when it IS enabled.

### Docs
- README rewritten as a what-works / what-does-not table plus exact install steps and every config key.
- The config path documented here now matches the code.

---

## Configuration file location

The config file is created and read at:

```
<mods folder>/HytaleTakaroMod/TakaroConfig.properties
```

- Dedicated server: `<server directory>/mods/HytaleTakaroMod/TakaroConfig.properties`
- Client-hosted world: `AppData/Roaming/Hytale/UserData/Saves/<WorldName>/mods/HytaleTakaroMod/TakaroConfig.properties`

An earlier entry in this file announced a move to `mods/TakaroConfig.properties`. That move
never happened in the code (`TakaroPlugin.setup()` has always resolved the `HytaleTakaroMod`
subdirectory), so anyone who followed it edited a file the mod never reads. The documented
path now matches the code, and the code is unchanged so existing installs keep working.

### ⚠️ CRITICAL WARNING - DEV Configuration
**DO NOT ENABLE DEV CONFIGURATION UNLESS YOU ARE A DEVELOPER!**

```properties
# NEVER enable these on production servers:
# DEV_ENABLED=true
# DEV_IDENTITY_TOKEN=...
# DEV_REGISTRATION_TOKEN=...
```

**Enabling dev mode on production servers will cause crashes and instability!** Only use dev configuration if you're actively developing Takaro integrations with a dev Takaro instance.

---

## [1.12.4] - 2026-01-26

### Fixed
- **Automatic Reconnection on Internal Errors**: Detects "Internal error handling game event" from Takaro and automatically reconnects with fresh websocket connection
  - Closes existing connection when internal error detected
  - Reconnects with fresh identity and registration tokens
  - Reinitializes connection cleanly in Takaro
  - Prevents connection state corruption

## [1.12.3] - 2026-01-23

### Added
- **Configurable Command Prefix**: Set custom prefix for Takaro commands (default: `!`)
  - Configure via `COMMAND_PREFIX` in TakaroConfig.properties
  - Supports any custom prefix (avoid `/` - reserved by Hytale)
- **Configurable Command Response**: Private confirmation message when player uses command
  - Configure via `COMMAND_RESPONSE` in TakaroConfig.properties
  - Default: `[cyan]Command[-] [green]{prefix}{command}[-]`
  - Supports `{prefix}` and `{command}` placeholders
  - Full color code support
- **Command Interception**: Commands no longer appear in public chat (behaves like Hytale's `/` commands)

### Changed
- **Configuration File Location**: documented as `mods/TakaroConfig.properties`. This was never true - the code always used `mods/HytaleTakaroMod/TakaroConfig.properties`. Corrected at the top of this file.

## [1.11.3] - 2026-01-19

### Added
- **Clickable Links in Messages**: URLs in Takaro messages are automatically detected and made clickable
  - Supports `http://`, `https://`, and `www.` URLs
  - Links appear in cyan color and open in browser when clicked

## [1.10.0] - 2026-01-19

### Added
- **Player Death Events**: Track and report player deaths to Takaro
  - Uses Hytale's ECS system (RefChangeSystem) to detect deaths
  - Sends death events with player info, position, and damage source
  - Enables death-based stats and respawn commands

## [1.8.5] - 2026-01-18

### Added
- **Dev Takaro Connection**: Optional secondary connection for development/testing
  - Configure via `DEV_ENABLED=true` in TakaroConfig.properties
  - Separate dev identity and registration tokens
  - Production and dev connections run simultaneously
- **Friendly Item Names**: Item lists now show display names (e.g., "Crude Arrow") instead of codes
  - Uses Hytale's I18n system for proper localization
  - Fallback to item code if translation unavailable

## [1.7.5] - 2026-01-18

### Added
- **Console Commands for All API Actions**: Execute any API action via console
  - `testReachability` - Test connection to Takaro
  - `getPlayers` - List all online players
  - `getServerInfo` - Display server information
  - `listItems` - Show all available items
  - `sendMessage <message>` - Broadcast message to all players
  - `getPlayerLocation <player>` - Get player coordinates
  - `getPlayerInventory <player>` - View player inventory
  - `kickPlayer <player> [reason]` - Kick player from server
  - `banPlayer <player>` - Ban player
  - `unbanPlayer <player>` - Unban player
- **Enhanced Help System**: Categorized commands with examples and argument descriptions

## [1.4.0] - 2026-01-17

### Added
- **Player Name Colors**: Custom chat name colors via Takaro permissions
  - Takaro can set player name colors via `setPlayerNameColor` API action
  - Supports hex colors (e.g., `ff0000`) and named colors (e.g., `gold`, `red`, `blue`)
  - Colors cached in memory for instant application
  - Console command: `setcolor <player> <color>`
- **Color Code System**: Rich text formatting in messages
  - Format: `[color]text[-]` (e.g., `[red]Warning[-]`)
  - Supports hex: `[ff0000]text[-]`
  - Supports named colors: `[red]`, `[green]`, `[blue]`, `[yellow]`, `[orange]`, `[pink]`, `[cyan]`, `[magenta]`, `[purple]`, `[gold]`, `[white]`, `[gray]`, `[black]`

## [1.3.1] - 2026-01-16

### Added
- **Player Bed Locations**: Track and query player bed/spawn positions
  - API action: `getPlayerBed` returns bed coordinates
  - Console command: `beds` lists all player bed locations

## [1.3.0] - 2026-01-16

### Added
- **Enhanced Console Commands**: Execute Takaro actions directly from console
  - Core commands for testing and administration
  - Detailed help menu with examples

## [1.2.5] - 2026-01-15

### Added
- **Hytale-Takaro Integration**: Complete integration between Hytale server and Takaro platform
  - WebSocket connection to Takaro (wss://connect.takaro.io/)
  - Real-time bidirectional communication
  - Automatic reconnection on disconnect
- **Player Event Tracking**: Monitor player activity
  - Player connect/disconnect events
  - Player position tracking
  - Player inventory monitoring
- **Chat Integration**: Full chat relay system
  - Send chat messages from Takaro to game
  - Forward game chat to Takaro
  - Support for private (DM) messages
- **Server Management API**: Remote server control
  - Get server information
  - List online players
  - Get player location and inventory
  - Execute console commands
  - Teleport players
- **Item Management**: Complete item system integration
  - List all available items with categories
  - Give items to players
  - Check item existence
- **Player Moderation**: Administrative actions
  - Kick players
  - Ban/unban players
  - Execute commands as console
- **Hytale API Client**: Optional integration with official Hytale API
  - Server telemetry reporting
  - Authentication support
  - Prepared for future Hytale API features
- **Configuration System**: Simple properties-based config
  - Server identity token
  - Registration token for Takaro
  - Optional Hytale API configuration
- **Logging Integration**: Forward server logs to Takaro
  - Real-time log streaming
  - Filtered log levels
  - Configurable log buffer

---

## Version Numbering

- **Major** (X.0.0): Breaking changes or major feature overhauls
- **Minor** (0.X.0): New features and capabilities
- **Patch** (0.0.X): Bug fixes and minor improvements

## Links

- [GitHub Repository](https://github.com/yourusername/Hytale-Takaro-Integration)
- [Takaro Platform](https://takaro.io/pricing/?via=zach550)
- [Hytale Website](https://hytale.com/)
