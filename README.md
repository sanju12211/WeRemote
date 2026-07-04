# WeRemote — Universal IR Remote (Android)

A native Kotlin Android app that turns a phone's built-in **IR blaster**
(`ConsumerIrManager`) into a universal remote — modeled on the Smart Life style
flow: **Home → Add Device → Select Device Type → Select Brand → match/test →
remote panel**. No extra hardware and no cloud account required.

## Requirements
- A phone with an **IR emitter** (e.g. many TECNO / Xiaomi / realme models).
  The app installs everywhere but can only transmit on phones with IR hardware.
- Android 7.0+ (minSdk 24).

## Features
- **TV** — verified codesets for **LG, Samsung, Singer, Sony** (power, volume,
  channel, D-pad, menu/home/back/exit, digits, ...).
- **Air Conditioner** — a full stateful remote (power / temp / mode / fan /
  swing) with a **test-and-match wizard**: cycle through 9 candidate codesets
  (Gree, Coolix, Midea, TCL, Haier, Electra/AUX, Kelvinator, LG, Hisense),
  tap Power on each, and keep whichever one your AC responds to.
- Saved remotes persist locally and reopen with their chosen codeset.
- Other device categories (Set Top Box, DVD, Projector, Amplifier, Fan, TV Box)
  are listed for browsing; add codesets to enable them.

## How it works
- `ir/IrProtocols.kt` — encoders for **NEC, Samsung, Sony SIRC, RC5** (carrier
  frequency + microsecond on/off pattern).
- `ir/IrBlaster.kt` — wraps `ConsumerIrManager.transmit()` + haptics.
- `ir/GreeAc.kt`, `ir/AcEngine.kt` — stateful air-conditioner protocols and a
  dispatcher. All AC protocols are implemented from public protocol specs.
- `data/IrDatabase.kt` — device types → brands → codes / AC candidates.
- `data/RemoteStore.kt` — saves the user's remotes (SharedPreferences + JSON).
- `ui/*` — Splash, Main (Home / Me), DeviceType, Brand, Match, AC match wizard,
  and the remote panels.

## Build
Open in Android Studio and press Run, **or** from a shell:

```bash
./gradlew assembleDebug      # debug APK
./gradlew assembleRelease    # release APK (configure signing first)
```
Output: `app/build/outputs/apk/debug/app-debug.apk`

Install on another phone:
```bash
adb install -r app-debug.apk
# or copy the .apk to the phone and open it (enable "Install unknown apps")
```

## Adding brands / codesets
- **TV / simple remotes:** add a `Brand` with its per-button `IrCode`s in
  `data/IrDatabase.kt`.
- **AC protocols:** add an `AcProto` value and its encoder in `ir/AcEngine.kt`;
  it automatically appears as another codeset in the match wizard.

## Notes
- AC protocols are best-effort implementations of public specs; test each on the
  real unit and keep the one that works. Gree is the most common in South Asia.
- No third-party commercial IR code databases are bundled — all codes/protocols
  here are hand-written from public specifications.

## Rename / re-brand
- App name: `res/values/strings.xml` → `app_name`
- Package: `applicationId` / `namespace` in `app/build.gradle.kts`
- Icon: `res/mipmap-*` and `res/drawable/ic_launcher_foreground.xml`
