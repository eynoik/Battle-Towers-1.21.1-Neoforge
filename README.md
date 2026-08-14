# Battle Towers — Minecraft 1.21.1 NeoForge

An unofficial port of **AtomicStryker's Battle Towers** to **Minecraft 1.21.1 NeoForge**.

Battle Towers adds large multi-floor towers to the world, filled with hostile mob spawners, loot chests and a Battle Tower Golem waiting at the top. Defeat the guardian, grab what you can and get out before the tower starts collapsing.

> **Status:** Alpha / pre-release. The mod is playable, but bugs and balance changes are still expected.

## Requirements

- **Minecraft:** 1.21.1
- **Mod loader:** NeoForge 21.1.x
- **Java:** 21

## Installation

1. Install NeoForge for Minecraft 1.21.1.
2. Download the latest Battle Towers `.jar` from **GitHub Releases**.
3. Put the JAR into your Minecraft `mods` folder.
4. Start the game.

For servers, install the mod on the server as well.

## Gameplay

Battle Towers generate naturally in newly generated **Overworld** chunks.

Depending on the terrain, different tower styles can appear, including cobblestone, mossy cobblestone, sandstone, ice and smooth stone variants. Underground towers are also supported.

**Netherrack towers do not generate naturally in the Overworld.**

Each tower contains:

- multiple floors of hostile mob spawners;
- loot chests with progression based on tower floor;
- a Battle Tower Golem guardian;
- classic Battle Towers sounds and textures;
- the Golem's melee, slam and fireball attacks;
- a deflectable Golem fireball;
- the classic delayed tower collapse after the Golem dies.

The Golem starts dormant and wakes when a player gets close or interferes with its tower.

## Configuration

Battle Towers has a per-world server config:

`world/serverconfig/battletowers-server.toml`

You can configure things such as:

- enable/disable natural tower generation;
- minimum distance from world spawn;
- minimum distance between towers;
- overall tower spawn chance;
- underground tower chance;
- number of floors;
- terrain height tolerance;
- which Overworld tower types may generate naturally;
- Golem health, damage and wake distance;
- Golem slam and fireball behavior;
- tower collapse delay, speed and explosion power.

Natural Overworld tower types can be toggled individually under:

```toml
[worldgen.towerTypes]
cobblestone = true
mossyCobblestone = true
sandstone = true
ice = true
smoothStone = true
jungle = false
```

Netherrack towers are intentionally excluded from natural Overworld generation.

## Commands

The following commands require operator permissions:

- `/battletowers spawn [type] [floors] [underground]` — manually generates a tower;
- `/battletowers types` — lists available tower types;
- `/battletowers list` — lists tracked towers;
- `/battletowers locate` — finds the nearest tracked Battle Tower;
- `/battletowers delete` — deletes the nearest tracked tower;
- `/battletowers regenerate` — regenerates the nearest tracked tower;
- `/battletowers deleteall` — deletes all tracked towers;
- `/battletowers regenerateall` — regenerates all tracked towers.

`/battletowers locate` searches already generated and tracked towers. It does not predict towers in unexplored chunks.

## Credits

### Original Battle Towers

**AtomicStryker** — creator and original author of the Battle Towers mod.

This port is based on the gameplay, assets and behavior of AtomicStryker's classic Battle Towers for older Minecraft versions.

Original AtomicStryker mods repository:
https://github.com/AtomicStryker/atomicstrykers-minecraft-mods

### Minecraft 1.21.1 port

**meynoik** — author of the **Minecraft 1.12.2 → 1.21.1 NeoForge port**.

Also known as the **slop port**.

## Disclaimer

This is an unofficial community port and is not the original Battle Towers release by AtomicStryker.
