# DogConnect

A lightweight Android companion for launching and inspecting SSRDOG Lite on HarmonyOS NEXT devices.

## Current milestone

- Native Android APK; no AppGallery submission required.
- One-tap launch of the installed SSRDOG Lite app from the home-screen widget.
- Optional, user-enabled AccessibilityService limited to the SSRDOG package.
- Diagnostic-only accessibility tree capture to determine whether SSRDOG's custom connection switch is exposed as an accessible control.
- No automatic clicks, gestures, VPN state changes, accounts, analytics, ads, or network requests.

## Why diagnostic first

SSRDOG Lite connects successfully in the HarmonyOS NEXT compatibility environment, but its Android Quick Settings tile is not exposed in Control Center. The connection switch is custom-drawn, so screenshots alone cannot establish whether Android accessibility can identify it. DogConnect first captures the accessibility node tree. Automated connection control will only be implemented after that tree and real-device behavior are verified.

## Build

The project is built by GitHub Actions. See the Actions tab for APK artifacts.
