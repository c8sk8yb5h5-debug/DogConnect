# DogConnect

Android companion for SSRDOG Lite on HarmonyOS NEXT.

## Features
- Home-screen widget launches SSRDOG Lite.
- Optional SSRDOG-only accessibility tree diagnostic; read-only, no automated clicks.
- In-app update check, APK download, package validation, and Android system installer handoff.
- Every qualifying push to `main` automatically builds, signs, and publishes a GitHub Release. DogConnect checks the latest stable release through the public GitHub API.

## Automatic release workflow
Changes to app source, Gradle configuration, or the release workflow trigger a signed release automatically. Version names increment as `0.2.N` and tags as `v0.2.N`, using the GitHub Actions release workflow run number. No manual tag or release creation is required.

Required GitHub Actions repository secrets:
- `KEYSTORE_BASE64`
- `KEYSTORE_PASSWORD`
- `KEY_ALIAS`
- `KEY_PASSWORD`

Keep the signing keystore backed up offline. All releases must use the same signing identity; losing it prevents in-place updates.

## Migration note
The earlier 0.1.0 CI APK was debug-signed with an ephemeral GitHub runner key. It cannot be updated in place by the persistent release key. The first signed release requires uninstalling the old debug build and installing the first signed release, which removes app-private diagnostic data. Future releases can update in place. Android/HarmonyOS still requires user confirmation in the system installer.
