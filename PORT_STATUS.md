# Port status

Battle Towers has an active Minecraft 1.21.1 / NeoForge gameplay port.

## Implemented

- NeoForge/Java 21 project and registries
- classic 14x14 tower floor geometry and material variants
- staged datapack loot tables, mob spawners and lighting
- natural deterministic Overworld generation plus underground variants
- persistent per-world Battle Tower registry via SavedData
- Battle Tower Golem dormant/awake AI, scaling, melee, slam and ranged fireball attack
- deflectable Golem fireball using modern projectile behavior
- original Golem textures and original Battle Towers sound assets
- configurable worldgen, Golem combat, fireball and collapse behavior
- staged tower collapse after guardian death
- chest interaction wakes and targets the tower's own guardian
- admin commands: spawn, types, list, delete, regenerate, deleteall, regenerateall
- English and Polish entity localization
- GitHub Actions build verification

## Deliberate modernization

Loot balance lives in datapack loot tables instead of the old encoded config strings. Natural generation is deferred from new-chunk events and throttled through a server queue to avoid doing placement work inside the chunk-load event.

## Remaining validation

The port should be tested in a real 1.21.1 NeoForge client/server world for gameplay balance, modpack worldgen interaction and visual edge cases. CI verifies compilation and packaging, not in-game behavior.
