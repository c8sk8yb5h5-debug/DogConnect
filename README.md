# DogConnect

Android companion for SSRDOG Lite on HarmonyOS NEXT.

## Features
- Home-screen widget launches SSRDOG Lite.
- Optional SSRDOG-only accessibility tree diagnostic; read-only, no automated clicks.
- In-app update check, APK download, package validation, and Android system installer handoff.
- Updates are published as GitHub Releases; the user confirms installation in the system dialog.

## Release signing
Configure GitHub Actions repository secrets: `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, and `KEY_PASSWORD`. Never commit the keystore or passwords. Back up the keystore offline; every release must use the same signing identity.

Push a version tag such as `v0.2.0`. The release workflow builds a signed APK and attaches it to a GitHub Release. DogConnect checks the latest stable release through the public GitHub API.

## Migration note
The earlier 0.1.0 CI APK was debug-signed with an ephemeral GitHub runner key. It cannot be updated in place by the new persistent release key. The first signed release requires uninstalling the old debug build and installing the first signed release, which removes app-private diagnostic data. Future releases can update in place.
