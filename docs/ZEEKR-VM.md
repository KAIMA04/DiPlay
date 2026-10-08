# Zeekr 极客极拓 VM compatibility

This variant targets the isolated Android environment called 极客极拓 (also written 极拓), not
the main ZEEKR OS. It makes no attempt to cross the VM boundary, grant itself system permissions,
or access vehicle services that Android does not expose to the app.

## Build identity and Android floor

The source baseline on `main` identifies itself as DiPlay 0.2.15 / version code 34. This variant is
0.2.15-jizhi / version code 35. The normal debug suffix makes the installed debug version
`0.2.15-jizhi-hud-test` in package `com.shihab.diplay.hudtest`.

The minimum remains Android 7.1 (API 25). This is a real packaging floor in the current project:
all application/library modules declare API 25 and the native shared code is built with
`APP_PLATFORM=android-25`. Main also has explicit Android 7.1 USB compatibility paths and API-25
tests. Lowering only the manifest value would permit an unverified install while leaving native and
framework compatibility unresolved. On API 25 and later there is no separate version refusal; the
app checks the services and permissions it actually needs and records the detected API in its
diagnostic report.

## Runtime capability behavior

- USB host, Bluetooth, Wi-Fi, Wi-Fi Direct, microphone, and audio output are optional manifest
  features, so their absence does not make the APK ineligible for installation.
- DiPlay records both Android feature declarations and the services/adapters actually visible to
  this process. Vendor VMs sometimes expose one without the other, so feature declarations alone
  never approve a transport.
- BYD navigation, vehicle-data, and cluster workers start only when a BYD package, receiver, build
  identity, or previously completed BYD vehicle-service probe is present. Exact BYD firmware checks
  and behavior remain unchanged on that hardware. A fresh Zeekr or generic VM leaves those
  integrations inert.
- The main CarPlay canvas uses the activity's measured window size and decoder capabilities.
  BYD's fixed 1920x720 cluster profile remains behind its exact firmware/display checks.
- Apple USB devices continue to be discovered by Apple's vendor ID. The deployment CH341
  VID/PID remains an exact allow-list and is used only when the user explicitly selects the
  USB/CH341 authentication target. Missing USB services fail with a message instead of being
  treated as required hardware.
- Wireless startup requires a visible Bluetooth adapter. A missing adapter or denied Nearby
  devices permission stops retrying and offers Connection setup, where USB remains available.
  If the selected Wi-Fi Direct or car-hotspot backend fails, DiPlay tries only alternatives whose
  credentials the driver already saved; it does not invent or extract network credentials.
- Audio first tries the configured Android route, then a usage-based route and a standard Android
  stream route as applicable. Rejected routes and the route that initialized successfully are
  written to the session diagnostic log.

## Limits the VM or vehicle must resolve

An APK cannot make hardware hidden by the VM appear. Wireless CarPlay still needs Bluetooth plus a
usable Wi-Fi/LAN path, and wired CarPlay needs USB host access and per-device permission. Android
must also provide an audio output route for sound.

Public source builds contain no accessory private key, certificate, MFi identity, release signing
key, or vehicle credential. This repository neither extracts nor forges them. The identity-free
debug APK can install and open its UI, but it cannot complete a real CarPlay authentication
handshake unless the tester independently supplies an authorized supported authentication target.

Build the identity-free APK with:

```sh
./gradlew :mobile:assembleDebug
```
