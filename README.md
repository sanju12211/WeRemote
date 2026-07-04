# WeRemote — Universal IR Remote (Android)

A native Kotlin Android app that turns a phone's built-in **IR blaster**
(`ConsumerIrManager`) into a universal remote — modeled on the Smart Life /
nebula IR remote flow: **Home → Add Device → Select Device Type → Select Brand
→ match/test → remote panel**.

## Requirements to run
- A phone that has an **IR emitter** (e.g. many TECNO / Xiaomi / realme models).
  The app installs everywhere but can only transmit on phones with IR hardware.
- minSdk 24 (Android 7.0+).

## Build
Open in Android Studio and press Run, **or** from a shell:

```
# uses the bundled Gradle 8.14.3 / AGP 8.6.1 / Kotlin 1.9.25
gradle assembleDebug          # debug APK
gradle assembleRelease        # release APK (configure signing first)
```
Output: `app/build/outputs/apk/debug/app-debug.apk`

Install on another phone:
```
adb install -r app-debug.apk
# or copy the .apk to the phone and open it (enable "Install unknown apps")
```

## How it works
- `ir/IrProtocols.kt` — encoders for **NEC, Samsung, Sony SIRC, RC5**. Each
  returns a carrier frequency + a microsecond on/off pattern.
- `ir/IrBlaster.kt` — wraps `ConsumerIrManager.transmit()` + haptics.
- `data/IrDatabase.kt` — the code database: **device types → brands → per-button
  codes**. Ships verified working codesets for **Samsung, LG, Sony** TVs.
- `data/RemoteStore.kt` — saves the user's remotes locally (SharedPreferences).
- `ui/*` — Splash, Main (Home/Me), DeviceType grid, Brand list + search,
  Match wizard, and the Remote panel.

## Add more brands / device types
Add a `Brand` with its codes in `data/IrDatabase.kt`:

```kotlin
private val myTv = Brand("MyBrand", mapOf(
    Fn.POWER   to IrCode(Proto.NEC, 0x20DF10EF),
    Fn.VOL_UP  to IrCode(Proto.NEC, 0x20DF40BF),
    // ...
))
```
Then reference it in the relevant `DeviceType(...)` `brands` list. Any function
that has no code is automatically greyed out on the remote panel.

For a truly universal database, import an open IR dataset (Flipper-IRDB, IRDB,
or LIRC) and generate `IrCode` entries from it — the encoders already cover the
most common protocols. Panasonic/Sharp/Kaseikyo etc. would need their own
encoder added to `IrProtocols.kt`.

## Rename / re-brand
- App name: `res/values/strings.xml` → `app_name`
- Package: `applicationId` + `namespace` in `app/build.gradle.kts` (and the
  `com.weremote.app` folder / imports)
- Icon: `res/mipmap-*` and `res/drawable/ic_launcher_foreground.xml`
