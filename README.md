# Hytale-Takaro Integration

A Hytale server plugin that connects a dedicated server to [Takaro](https://takaro.io/pricing/?via=zach550).

Built and tested against **Hytale dedicated server 0.6.8**. Java 21+.

---

## Status

This fork (`1.14.6-elimon.2`) is the result of a hard test of upstream `mad-001/Hytale-Takaro-Integration`
against a real 0.6.8 server, followed by a fix pass. Upstream at `ba872f1` **does not compile** against
0.6.8 at all, so there is no stock build to compare against.

**Nothing in the table below has been re-proven live since the fixes were written.** The code compiles,
the unit tests pass, and each fix is written against the API signatures verified with `javap` on the
0.6.8 server jar — but every row is marked ⚠️ until it has been exercised against a running server with
a real client. Treat the table as "implemented, not yet proven".

| Capability | Status | Notes |
|---|---|---|
| Connect / identify with Takaro | ⚠️ needs live test | WebSocket to `wss://connect.takaro.io/` |
| Reconnect + re-identify after an outage | ⚠️ needs live test | Exponential backoff; config re-read before each attempt |
| `testReachability` | ⚠️ needs live test | Proven honest in the stock hard test (a stopped server reports unreachable) |
| `getPlayers` | ⚠️ needs live test | Name, gameId, platformId, IP |
| `getPlayer` — online | ⚠️ needs live test | |
| `getPlayer` — offline | ⚠️ needs live test | Served from the known-players ledger; a never-seen player returns an error |
| `getServerInfo` | ⚠️ needs live test | Real MOTD, real Hytale version, player counts |
| `getPlayerLocation` | ⚠️ needs live test | Failures return an error, not `0,0,0` |
| `getPlayerInventory` | ⚠️ needs live test | All six inventory sections; failures return an error, not `[]` |
| `getPlayerBedLocation` | ⚠️ needs live test | Respawn points, read reflectively |
| `giveItem` | ⚠️ needs live test | Honours `amount`; `quality` is echoed back as `qualityIgnored` (see limits) |
| `teleportPlayer` / `teleportPlayerToPlayer` | ⚠️ needs live test | Rejects missing/non-numeric coordinates |
| `sendMessage` — global and DM | ⚠️ needs live test | Colour codes and links supported |
| `kickPlayer` | ⚠️ needs live test | Reason defaults when null or empty |
| `banPlayer` / `unbanPlayer` | ⚠️ needs live test | Real Hytale `AccessControlModule`; works offline by UUID; honours `expiresAt`; state is read back before success is reported |
| `listBans` | ⚠️ needs live test | Takaro `IBan` shape; names come from the known-players ledger |
| `listItems` | ⚠️ needs live test | Human-readable names; `Debug_*`/`Test_*`/`Dev_*` filtered |
| `listEntities` | ⚠️ needs live test | Spawnable NPC role templates with display names |
| `listLocations` | ⚠️ needs live test | Named warps; empty list on a server with no warps |
| `executeConsoleCommand` | ⚠️ needs live test | Per-command output capture; unknown command ⇒ `success:false` |
| `shutdown` | ⚠️ needs live test | Graceful stop via the server's own shutdown path |
| Events: connected, disconnected, chat-message, player-death, log | ⚠️ needs live test | |
| Event: `entity-killed` | ⚠️ needs live test | Mob killed by a player; killer attributed |
| Events survive a Takaro outage | ⚠️ needs live test | Queued (default 1000) and flushed after re-identify |

### What this connector does **not** do

| Missing | Why |
|---|---|
| Weapon on a kill or a death | Hytale 0.6.8 records none. Neither `DeathComponent` nor `Damage` nor `Damage.Source` has an item field; the nearest thing is a UI icon id. The event's `weapon` is sent empty rather than guessed. |
| Item quality on `giveItem` | 0.6.8's `ItemStack` has no quality/variant concept the plugin can set. The requested value is echoed back as `qualityIgnored` instead of being silently dropped. |
| Hostile/friendly classification in `listEntities` | Role templates carry no such category field, so every row reports `type: "npc"`. |
| Map tiles | Takaro has no map support for Generic game servers. |
| Regions / points of interest in `listLocations` | 0.6.8 has no POI, region or landmark API. Warps are the only named-point store. |
| Backpack-aware `getPlayerInventory` guarantees beyond 0.6.8 | The whole `Inventory` class is `@Deprecated(forRemoval)` in 0.6.8; expect this to need rework on the next server release. |

---

## Install

1. **Get the jar.** Build it (below) or take `HytaleTakaroMod-1.14.6-elimon.2.jar` from a release.
2. **Drop it in the server's mods folder:**
   ```
   <server directory>/mods/HytaleTakaroMod-1.14.6-elimon.2.jar
   ```
   (For a client-hosted world: `AppData/Roaming/Hytale/UserData/Saves/<WorldName>/mods/`.)
3. **Start the server once.** It creates the config at:
   ```
   <server directory>/mods/HytaleTakaroMod/TakaroConfig.properties
   ```
   That is the only path the plugin reads. Nothing is read from `mods/TakaroConfig.properties`.
4. **In Takaro:** *Settings → Game Servers → Add Server → Generic*. Copy the registration token.
5. **Edit the config** — set `IDENTITY_TOKEN` to a name for this server and `REGISTRATION_TOKEN` to
   the token from step 4.
6. **Restart the server.** The log should show `Sending identify message` then
   `[Takaro] Successfully identified`.

> Takaro rotates the domain-wide registration token whenever a new gameserver is registered. If a
> reconnect logs `Identification REJECTED by Takaro: ... Invalid registrationToken`, paste the current
> token into the config — it is re-read before every attempt, so no server restart is needed.

## Configuration keys

Written to `mods/HytaleTakaroMod/TakaroConfig.properties`.

| Key | Default | What it does |
|---|---|---|
| `IDENTITY_TOKEN` | `MyHytaleServer` | The name this server identifies as |
| `REGISTRATION_TOKEN` | *(empty)* | From Takaro: Settings → Game Servers → Add Server → Generic |
| `COMMAND_PREFIX` | `!` | Prefix for Takaro chat commands (do not use `/`) |
| `COMMAND_RESPONSE` | `[cyan]Command[-] [green]{prefix}{command}[-]` | Private confirmation sent when a command is seen |
| `TAKARO_DEBUG` | `false` | Log every WebSocket frame (direction, type, requestId, action) at INFO |
| `LOG_FORWARD_LEVEL` | `INFO` | Minimum server log level forwarded to Takaro. `OFF` disables forwarding |
| `LOG_FORWARD_MAX_PER_MIN` | `120` | Cap on forwarded log records per minute (`0` = unlimited) |
| `EVENT_QUEUE_SIZE` | `1000` | Game events held while Takaro is unreachable; oldest dropped when full |
| `CATALOG_INCLUDE_DEBUG` | `false` | Include `Debug_*`/`Test_*`/`Dev_*` entries in `listItems` and `listEntities` |
| `HYTALECHARTS_SECRET` | *(empty)* | **Opt-in.** Setting it sends this server's player list (usernames + UUIDs) to hytalecharts.com every 5 minutes |
| `HYTALECHARTS_PROMO_ON_LOGIN` | `false` | Send a hytalecharts.com promo link to players when they join |
| `HYTALECHARTS_PROMO_ENABLED` | `false` | Broadcast that promo link periodically |
| `HYTALECHARTS_DEBUG` | `false` | Verbose HytaleCharts logging |

Everything under `HYTALECHARTS_*` is off by default and sends nothing anywhere unless a secret is set.

`DEV_ENABLED` / `DEV_IDENTITY_TOKEN` / `DEV_REGISTRATION_TOKEN` / `DEV_WS_URL` point the plugin at a
development Takaro instance. **Do not enable these on a production server.**

## Takaro console

Type `takaro help` in the Takaro console. Every Takaro helper lives under the `takaro` namespace so it
cannot shadow a real Hytale command; anything not starting with `takaro ` is passed straight to the
Hytale console.

## Build

Needs the Hytale server jar, which is not redistributable and is never committed:

```bash
mkdir -p libs
cp /path/to/HytaleServer.jar libs/HytaleServer.jar
cd HytaleTakaroMod && mvn clean package
# -> HytaleTakaroMod/target/HytaleTakaroMod-1.14.6-elimon.2.jar
```

`mvn test` runs the unit tests (argument handling, event queue, ban mapping, response shaping,
log-forward filter); none of them need the server jar at runtime.

## Note on the repository layout

Only `HytaleTakaroMod/` is built. The `src/` directory at the repository root is a stale duplicate of
an older copy of the same sources and is not part of the build.

## Links

- [Takaro](https://takaro.io/pricing/?via=zach550) · [Discord](https://discord.gg/pwenDRrtnA)
