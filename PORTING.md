# Battle Towers 1.21.1 NeoForge port plan

This repository is a clean porting workspace. The active implementation belongs in `src/main`; the original Forge-era Battle Towers code is only a reference and must not be added to the active source set unchanged.

## Target

- Minecraft: 1.21.1
- Loader: NeoForge 21.1.x
- Java: 21
- Mod id: `battletowers`
- Package root: `atomicstryker.battletowers`

## What the legacy code actually is

The Battle Towers module contained in the uploaded `atomicstrykers-minecraft-mods-1.21.1.zip` still uses the old Forge lifecycle and Minecraft APIs. Its build file uses ForgeGradle 2.3 and the Java source references classes/events such as `EntityPlayerMP`, `FMLPreInitializationEvent`, `SidedProxy`, `GameRegistry` and the old `@Mod` lifecycle.

Treat it as a reference implementation, not as source that should compile in the 1.21.1 project.

## Port order

### 1. Bootstrap and registries

Start from `AS_BattleTowersCore`.

Replace the old lifecycle with NeoForge registration:

- entities through `DeferredRegister<EntityType<?>>`
- sounds through `DeferredRegister<SoundEvent>`
- config through NeoForge config specs
- event listeners through the mod/game event buses
- remove `CommonProxy`, `ClientProxy`, `@SidedProxy` and `@ObjectHolder`

Do this before porting gameplay code so every later subsystem has stable registry objects to target.

### 2. Tower representation and generation

Main legacy references:

- `AS_WorldGenTower`
- `WorldGenHandler`
- `TowerStageItemManager`

Do not mechanically port the old `IWorldGenerator` approach. Rebuild tower placement around the 1.21.1 worldgen/structure APIs and keep tower construction logic separated from placement policy.

Preserve behavior first:

- tower variants/material sets
- underground tower chance
- minimum distance from spawn
- minimum distance between towers
- floor count/layout
- spawners and chest placement
- per-floor loot progression

Only optimize or data-drive it after parity exists.

### 3. Battletower Golem

Main legacy reference: `AS_EntityGolem`.

Port in this order:

1. entity type and attributes
2. dormant/awake state
3. target selection and combat goals
4. boss health/damage scaling
5. save/load state
6. sounds
7. renderer/model/texture hookup

Keep server-side AI independent from the client renderer.

### 4. Golem fireball and networking

References:

- `AS_EntityGolemFireball`
- `common/network/ChestAttackedPacket`
- `common/network/LoginPacket`
- `common/network/NetworkHelper`

Use the 1.21.1 NeoForge payload/networking API. Do not recreate the old packet helper verbatim.

### 5. Tower reaction and destruction

References:

- `AS_TowerDestroyer`
- `ServerTickHandler`
- `ClientTickHandler`

Preserve the original gameplay sequence first, especially boss defeat/tower destruction behavior and the configuration options controlling explosions and spawner destruction. Move ticking work to the appropriate modern events or scheduled server work.

### 6. Client rendering

References:

- `AS_RenderGolem`
- `AS_RenderFireball`
- `ClientProxy`

The old fixed-function/OpenGL render code cannot be carried over directly. Rebuild entity renderers using the current Minecraft renderer APIs and register them only on the client side.

### 7. Commands

References:

- `CommandBattleTowers`
- `CommandSpawnBattleTower`
- `CommandDeleteBattleTower`
- `CommandDeleteAllBattleTowers`
- `CommandRegenerateBattleTower`
- `CommandRegenerateAllBattleTowers`

Port these after tower placement has a stable modern API. Commands should call the same tower service used by worldgen rather than duplicate generation logic.

### 8. Assets and data

Reusable legacy assets include the golem textures and original sounds. Old `.lang`, `mcmod.info` and nested legacy `pack.mcmeta` files must be converted/replaced for 1.21.1.

Recommended modern paths:

- `src/main/resources/assets/battletowers/lang/en_us.json`
- `src/main/resources/assets/battletowers/sounds.json`
- `src/main/resources/assets/battletowers/sounds/`
- `src/main/resources/assets/battletowers/textures/`
- data-driven loot/worldgen under `src/main/resources/data/battletowers/`

## First concrete coding milestone

The first useful milestone is not "make all old files compile". It is:

1. the NeoForge mod loads;
2. sound/entity registries exist;
3. a Battletower Golem can be spawned with a command;
4. one deterministic test tower can be placed by a command;
5. only then connect the tower to natural world generation.

That gives a debuggable vertical slice instead of a wall of legacy compiler errors.
