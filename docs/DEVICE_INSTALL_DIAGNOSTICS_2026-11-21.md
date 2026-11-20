# SKYNET Android install diagnostics

Date: 2026-11-21
Repository: Fevi12/Skynet.mobile
Branch: main

## Device
Samsung SM-A057G
Android 15 / API 35

## Package
Application ID: no.skynet.mobile
Version: 1.2
Version code: 3
Minimum SDK: 23
Target SDK: 35

## Local APK candidates
SKYNET.apk: 35 KB
SHA256: db364e94ba004a4884b5a7b00bd5579fe471fa1f49ada5ade57357017533cb41

SKYNET-debug.apk: 19 KB
SHA256: 0bf608c0bdd412ad3c84c2245b3bdfbb1c205a6c12a2c6c69d386b20fd30ccec

app-debug.apk: 27 KB
SHA256: 38ea6e24a8fec31864c66ee79af4ed95ca45b65a973a1e009c3d4c5409d8de49

SKYNET-1.1.apk: 14 KB
SHA256: cc9b838b005868f2902bc274c791bbf6361b9a987ffdef781259e3a0bdacfe58

## Installation problem
No existing SKYNET package found.

pm install -r SKYNET.apk returned:
Failure calling service package: Failed transaction (2147483646)

Root cause not yet established.

## Next actions
- Inspect Package Manager and Android system logs.
- Verify APK integrity and signing.
- Download and test the exact GitHub Actions artifact.
- Preserve existing project files and APK candidates.

APK files remain in local Termux storage and are not included in this source-code commit.
