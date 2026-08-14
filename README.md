# Battle Towers — Minecraft 1.21.1 NeoForge Port

Modern NeoForge 1.21.1 port workspace for AtomicStryker's classic Battle Towers gameplay.

## Current state

The port is now a playable alpha rather than a scaffold. It includes tower generation, staged loot and spawners, the Battle Tower Golem boss, classic sounds/textures, the deflectable ranged attack, collapse behavior, persistent tower tracking and administration commands.

Target:
- Minecraft 1.21.1
- NeoForge 21.1.x
- Java 21
- mod id `battletowers`

## Gameplay

Natural Battle Towers generate deterministically in newly generated Overworld terrain with spacing and terrain checks. Underground variants are supported. Each tower contains staged chest loot and hostile mob spawners. The guardian starts dormant and wakes when a player approaches, attacks it, or interacts with one of its tracked tower chests.

The Golem preserves the classic gameplay loop: melee combat, rage/slam behavior, charge-and-fireball ranged attack, deflectable projectile, classic Battle Towers audio and the staged tower collapse after its death.

## Commands

Operator commands:
- `/battletowers spawn [type] [floors] [underground]`
- `/battletowers types`
- `/battletowers list`
- `/battletowers delete`
- `/battletowers regenerate`
- `/battletowers deleteall`
- `/battletowers regenerateall`

Delete/regenerate commands operate on the persistent per-world tower registry instead of scanning arbitrary blocks.

## Configuration

A modern SERVER config is registered as `battletowers-server.toml`. It controls world generation spacing/chance, floor count, queued worldgen work, Golem combat/scaling, fireball timing and power, and collapse behavior.

Loot progression is intentionally data-driven under `data/battletowers/loot_table/chests/` rather than encoded into config strings as in the old Forge version, so modpacks can override it with datapacks.

## Build

Import the project with JDK 21 and Gradle. CI runs a full `gradle build` for the port branch.

If a Gradle wrapper is not present locally, run:

```text
gradle wrapper
```

Then build with:

```text
./gradlew build
```

Windows:

```text
gradlew.bat build
```

## Legacy reference

The uploaded AtomicStryker archive is retained only as porting reference. The active 1.21.1 implementation lives under `src/main`; legacy Forge/FML code must not be added to the active source set unchanged.

See `PORTING.md` and `PORT_STATUS.md` for architecture and port notes.
