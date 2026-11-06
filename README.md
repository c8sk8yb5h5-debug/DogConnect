# DogConnect

A lightweight Android companion for launching SSRDOG Lite on HarmonyOS NEXT devices.

## Current milestone

- Native Android APK; no AppGallery submission required.
- One-tap launch of the installed SSRDOG Lite app.
- Detect whether SSRDOG Lite is installed.
- Investigate a safe, explicit connection trigger before attempting automation.

## Important limitation

DogConnect does not bundle SSRDOG's proprietary code, reuse its private APIs, or handle subscription credentials. Direct connection automation will only be added if a supported public integration point or a reliable, user-authorized accessibility flow can be verified on the target device.

## Build

The project is built by GitHub Actions. See the Actions tab for APK artifacts.
