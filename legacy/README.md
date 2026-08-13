# Legacy Battle Towers reference

`legacy/1.12.2/` is reserved for the original AtomicStryker Battle Towers implementation used as a behavioral reference during the NeoForge 1.21.1 port.

The canonical input archive for this workspace is:

`../atomicstrykers-minecraft-mods-1.21.1.zip`

To populate this directory from that archive without copying any of AtomicStryker's other mods, run one of:

- Windows: `./scripts/extract-legacy.ps1`
- Linux/macOS/Git Bash: `bash ./scripts/extract-legacy.sh`

The extractor deletes/recreates `legacy/1.12.2/` and copies **only** the directory named `BattleTowers` from the archive.

Do not add `legacy/1.12.2/src/main/java` to the active Gradle source set. It is old Forge/FML code and is intentionally isolated from the NeoForge implementation under `src/main/java`.
