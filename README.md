<p align="center">
  <img src="https://raw.githubusercontent.com/cartographgg/.github/main/cartograph-logo%403x.png" alt="Cartograph" width="600" />
</p>

# Cartograph Plugin

Open-source Minecraft server analytics plugin. Collects server metrics and player activity and sends them
to [Cartograph](https://cartograph.gg).

## Supported Platforms

| Platform   | Versions   | JAR Name                              |
|------------|------------|---------------------------------------|
| Spigot     | 1.21, 26.1 | `cartograph-spigot-{version}.jar`     |
| Paper      | 1.21, 26.1 | `cartograph-paper-{version}.jar`      |
| Folia      | 1.21       | `cartograph-folia-{version}.jar`      |
| BungeeCord | 1.21, 26.1 | `cartograph-bungeecord-{version}.jar` |
| Velocity   | 1.21, 26.1 | `cartograph-velocity-{version}.jar`   |
| NeoForge   | 1.21       | `cartograph-neoforge-{version}.jar`   |

Spigot, Paper, and Folia are server platforms. BungeeCord and Velocity are proxy platforms. NeoForge is a mod loader.

Folia and NeoForge are not yet supported on Minecraft 26.1: PaperMC has not released
a Folia experimental for the 26.x line, and NeoForge 26.x ships only beta builds
upstream. Both lines will be added when stable upstream releases ship.

## Installation

1. Download the correct JAR for your platform and Minecraft version.
2. Place it in your server's `plugins/` directory (or `mods/` for NeoForge).
3. Start the server. The plugin generates a default config file. Add your API key
   from [cartograph.gg](https://cartograph.gg) and restart.

Without an API key the plugin loads but does not send any data.

**Config file locations:**

- Bukkit/BungeeCord/Velocity: `plugins/Cartograph/config.yml`
- NeoForge: `config/cartograph-common.toml`

## Configuration

Default `config.yml`:

```yaml
api-key: ""
api-endpoint: "https://api.cartograph.gg"

flags:
  report-plugins: true
  proxy-backend: false

buffer:
  size-threshold: 50
  time-threshold: 60
  failure-mode: disk
  disk:
    max-size-mb: 8
    max-age-hours: 24
  backoff:
    max-seconds: 900
    retry-after-cap-seconds: 3600

telemetry:
  heartbeat:
    enabled: true
    interval: 60
```

### Reference

**`api-key`** (string, default: `""`)
Your Cartograph API key. Authenticates your server with the Cartograph platform. When empty, the plugin loads but no
telemetry is sent.

**`api-endpoint`** (string, default: `"https://api.cartograph.gg"`)
The URL telemetry is sent to. Only change this if you are running a self-hosted Cartograph instance.

**`flags.report-plugins`** (boolean, default: `true`)
When `true`, the list of installed plugins (or mods on NeoForge) is included in the boot telemetry event (names and
versions only, never published per-server). Enabled by default; set to `false` to opt out.

**`flags.proxy-backend`** (boolean, default: `false`)
Set to `true` on backend servers that sit behind a BungeeCord or Velocity proxy. When enabled, the plugin skips player
join/leave tracking on the backend (the proxy handles it instead) and reports the server's node type as `BACKEND`.

**`buffer.size-threshold`** (integer, default: `50`)
The number of events to accumulate before triggering a flush to the API.

**`buffer.time-threshold`** (integer, default: `60`)
Maximum seconds to wait before flushing buffered events, regardless of count.

**`buffer.failure-mode`** (string, default: `disk`)
What happens to a batch when a send fails. `disk` persists undelivered batches to disk and retries with backoff, so they
survive an outage or a restart; `memory` retries in memory only (bounded, lost on restart); `none` drops the batch
(strictly best-effort, no local state). Backoff always applies regardless of this setting.

**`buffer.disk.max-size-mb`** (integer, default: `8`)
On-disk budget for undelivered batches (used by `failure-mode: disk`, and as the in-memory budget for `memory`). The
oldest batches are dropped once this is exceeded.

**`buffer.disk.max-age-hours`** (integer, default: `24`)
Undelivered batches older than this are dropped.

**`buffer.backoff.max-seconds`** (integer, default: `900`)
Cap on the exponential backoff applied between retries after a send failure.

**`buffer.backoff.retry-after-cap-seconds`** (integer, default: `3600`)
Upper bound on how long a server-supplied HTTP `Retry-After` will be honored.

**`telemetry.heartbeat.enabled`** (boolean, default: `true`)
Whether to collect periodic server health metrics.

**`telemetry.heartbeat.interval`** (integer, default: `60`)
How often (in seconds) to collect a heartbeat snapshot. Clamped to the range [30, 300]; the effective interval in use is
included in each heartbeat event.

## Data Collection

This section describes exactly what telemetry the plugin sends.

### Boot event

Sent once when the server starts.

- Server software and version (e.g. Paper 1.21)
- Java version
- Operating system name and architecture
- Cartograph plugin version
- Maximum player slots, view distance, simulation distance
- Online mode and whitelist status
- List of worlds with environment types
- Installed plugins/mods (only if `report-plugins` is enabled)
- Backend server list (proxy platforms only)
- Supported Minecraft client protocol versions (if ViaVersion is installed)
- Bedrock Edition support (if Geyser or Floodgate is installed)

### Heartbeat

Sent periodically (default every 60 seconds).

- TPS (ticks per second) averages
- Tick duration (MSPT): peak, plus p50 / p95 / p99 percentiles
- Online player count
- Memory usage (used and max)
- CPU load (process and system)
- Loaded chunks and entities (server platforms only)
- Per-world breakdown for notable worlds
- Effective heartbeat interval in use

### Player join

- Player UUID and username
- Whether the player is new (first join)
- Client protocol version
- Player locale
- Connection hostname (the address the player used to connect)
- Current world
- Whether the player connected via Floodgate (Bedrock Edition bridge)

### Player leave

- Player UUID
- Session duration in milliseconds
- Leave reason (quit, kick, ban, or timeout)
- Current world

### Shutdown event

Sent on clean server stop.

- Server uptime in milliseconds
- Shutdown reason

## Building from Source

**Prerequisites:** Java 21 or later, Git.

```
git clone https://github.com/cartographgg/cartograph-plugin.git
cd cartograph-plugin
./gradlew build
```

The Gradle wrapper is included - no need to install Gradle separately.

**Output JARs** are in each platform's version-specific build directory:

```
bukkit/spigot/v1_21/build/libs/cartograph-spigot-1.21-*.jar
bukkit/paper/v1_21/build/libs/cartograph-paper-1.21-*.jar
bukkit/folia/v1_21/build/libs/cartograph-folia-1.21-*.jar
bungeecord/v1_21/build/libs/cartograph-bungeecord-1.21-*.jar
velocity/v1_21/build/libs/cartograph-velocity-1.21-*.jar
neoforge/v1_21/build/libs/cartograph-neoforge-1.21-*.jar
```

**Running tests:**

```
./gradlew test
```

## Project Structure

```
cartograph-plugin/
  common/       Shared telemetry, config, event buffering, HTTP client
  bukkit/       Bukkit base (shared by Spigot, Paper, Folia)
    spigot/     Spigot entry point and version-specific builds
    paper/      Paper entry point (uses Paper plugin loader)
    folia/      Folia entry point (async-safe tick sampling)
  bungeecord/   BungeeCord proxy entry point
  velocity/     Velocity proxy entry point
  neoforge/     NeoForge mod entry point
```

All platform modules depend on `common`, which contains the `Cartograph` facade, event buffer, telemetry client, and
config model. Platform modules provide the entry point, config loader, and event listeners specific to their platform.

## License

Released under the [MIT License](LICENSE). © Cartograph contributors.
