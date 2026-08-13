# Battle Towers — Minecraft 1.21.1 NeoForge Port

Unofficial porting workspace for **AtomicStryker's Battle Towers**, targeting **Minecraft 1.21.1 + NeoForge**.

> **Status:** port scaffold only. The modern project structure is ready, but the legacy gameplay code has not been ported yet.

## Target

- Minecraft `1.21.1`
- NeoForge `21.1.x`
- Java `21`
- Mod ID: `battletowers`
- Package: `atomicstryker.battletowers`

## Repository layout

```text
src/main/                  active NeoForge 1.21.1 implementation
src/main/templates/        generated NeoForge mod metadata
legacy/1.12.2/             legacy Battle Towers reference workspace
scripts/                    source extraction helpers
PORTING.md                  subsystem-by-subsystem port plan
atomicstrykers-minecraft-mods-1.21.1.zip
                           original source archive uploaded for this port
```

The uploaded archive contains the wider AtomicStryker mod repository. **Only the `BattleTowers` directory is relevant to this project.** The extraction scripts deliberately locate and copy only that directory.

## Extract the legacy Battle Towers source

Windows PowerShell:

```powershell
./scripts/extract-legacy.ps1
```

Linux/macOS/Git Bash:

```bash
bash ./scripts/extract-legacy.sh
```

The scripts replace `legacy/1.12.2/` with the complete `BattleTowers` directory from the archive and do not copy any of the other AtomicStryker mods.

## Why the old code is isolated

Despite the archive/branch name, the Battle Towers module itself is still old Forge-era code. Its build script uses **ForgeGradle 2.3** and its Java sources use the old FML lifecycle (`FMLPreInitializationEvent`, `@SidedProxy`, old `GameRegistry`, `EntityPlayerMP`, etc.). Throwing those files directly into the active 1.21.1 source set would only create a wall of compiler errors.

The port therefore starts with a clean NeoForge project and uses the old source strictly as a behavioral reference.

## Build setup

Import the repository as a Gradle project with **JDK 21**.

If a Gradle wrapper is not present yet, generate it once with your installed Gradle:

```bash
gradle wrapper
```

Then build with:

```bash
./gradlew build
```

On Windows:

```powershell
./gradlew.bat build
```

## Porting strategy

The detailed plan is in [`PORTING.md`](PORTING.md). The intended order is:

1. NeoForge bootstrap, registries and config
2. tower representation and deterministic command placement
3. Battletower Golem entity/AI
4. projectile + networking
5. tower destruction sequence
6. client renderers and sounds
7. commands
8. natural world generation and final data-driven loot/config cleanup

The first useful milestone is **one test tower placeable by command + one spawnable Golem**, not "make every legacy class compile".

## Original project and licensing

Battle Towers was created by **AtomicStryker**. Original code/assets remain subject to the original author's licensing terms. This repository is a porting workspace and should keep attribution and the original license information with any reused source/assets.
