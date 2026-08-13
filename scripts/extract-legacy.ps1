$ErrorActionPreference = 'Stop'

$repoRoot = Split-Path -Parent $PSScriptRoot
$zip = Join-Path $repoRoot 'atomicstrykers-minecraft-mods-1.21.1.zip'
$temp = Join-Path $repoRoot '.extract-battletowers'
$destination = Join-Path $repoRoot 'legacy/1.12.2'

if (-not (Test-Path $zip)) {
    throw "Missing source archive: $zip"
}

Remove-Item $temp -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item $destination -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Path $temp | Out-Null
New-Item -ItemType Directory -Path $destination | Out-Null

Expand-Archive -Path $zip -DestinationPath $temp -Force
$battleTowers = Get-ChildItem -Path $temp -Directory -Recurse | Where-Object { $_.Name -eq 'BattleTowers' } | Select-Object -First 1

if ($null -eq $battleTowers -or -not (Test-Path (Join-Path $battleTowers.FullName 'src'))) {
    throw 'BattleTowers directory was not found in the archive.'
}

Copy-Item -Path (Join-Path $battleTowers.FullName '*') -Destination $destination -Recurse -Force
Remove-Item $temp -Recurse -Force

Write-Host "Extracted only BattleTowers to $destination"
