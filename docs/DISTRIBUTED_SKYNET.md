# SKYNET Distributed Device Architecture

## Implemented foundation
- A consistent, configurable assistant personality.
- Local conversation memory with bounded history and user-controlled clearing.
- Explicit JSON export/import for recovery.
- Wi-Fi discovery for devices that deliberately advertise `_skynet._tcp`.
- Listing of Bluetooth devices already paired by the Android user.

## Required integration work
1. Add a visible Devices screen with Start/Stop controls and a listener for discovery results.
2. Request `BLUETOOTH_CONNECT` at runtime on Android 12+ before listing paired devices.
3. Add `BLUETOOTH_SCAN` only if a future feature performs BLE scanning; explain why and request it at runtime. Do not silently scan.
4. Add a companion app or Wear OS module for watches. A phone app cannot silently install itself or its memory into a watch.
5. Implement authenticated device-to-device messaging (TLS, pairing code, revocable device keys) before exchanging memory. Discovery alone is not a secure connection.
6. Add Android Sharesheet / Storage Access Framework for user-selected backup destinations.
7. Add a user-visible microphone permission and foreground microphone service only when voice listening is explicitly enabled. Show the Android microphone indicator and a persistent notification.
8. Add tests for permission denial, missing Bluetooth, network loss, import validation, and backup recovery.

## Platform and safety limits
- Android, Wear OS, TVs and PCs require compatible companion software and user-approved pairing.
- Wi-Fi/Bluetooth discovery does not grant remote access or permit app installation.
- Android may stop background work; use WorkManager for deferrable sync and a foreground service only for user-noticeable ongoing tasks.
- Apps cannot guarantee survival after uninstall, factory reset, or user revocation. Recovery uses backups controlled by the user.
- The assistant can have a consistent personality and proactive features, but software should not be represented as literally conscious.
