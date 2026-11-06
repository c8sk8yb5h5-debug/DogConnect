# DogConnect

A lightweight Android companion for launching SSRDOG Lite on HarmonyOS NEXT devices.

## Current milestone

- Native Android APK; no AppGallery submission required.
- One-tap launch of the installed SSRDOG Lite app from the home-screen widget.
- Detect whether SSRDOG Lite is installed.
- APK inspection found SSRDOG's built-in Quick Settings Tile service (`io.nekohasekai.sfa.bg.TileService`). Upstream sing-box Android implementation toggles its background service from that tile. On HarmonyOS NEXT, verify whether the system exposes this tile in Control Center.
- DogConnect does not claim to toggle the VPN directly; direct app-to-app control is not exposed by Android's Quick Settings API.

## Important limitation

DogConnect does not bundle SSRDOG's proprietary code, reuse its private APIs, or handle subscription credentials. Direct connection automation will only be added if a supported public integration point or a reliable, user-authorized accessibility flow can be verified on the target device.

## Build

The project is built by GitHub Actions. See the Actions tab for APK artifacts.
