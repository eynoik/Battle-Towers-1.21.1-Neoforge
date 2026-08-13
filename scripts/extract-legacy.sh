#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ZIP="$ROOT/atomicstrykers-minecraft-mods-1.21.1.zip"
TMP="$ROOT/.extract-battletowers"
DEST="$ROOT/legacy/1.12.2"

[[ -f "$ZIP" ]] || { echo "Missing source archive: $ZIP" >&2; exit 1; }

rm -rf "$TMP" "$DEST"
mkdir -p "$TMP" "$DEST"
unzip -q "$ZIP" -d "$TMP"

SRC="$(find "$TMP" -type d -name BattleTowers -print -quit)"
[[ -n "$SRC" && -d "$SRC/src" ]] || { echo 'BattleTowers directory was not found in the archive.' >&2; exit 1; }

cp -a "$SRC"/. "$DEST"/
rm -rf "$TMP"
printf 'Extracted only BattleTowers to %s\n' "$DEST"
