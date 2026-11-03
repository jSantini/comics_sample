# Real Android screenshot capture

Status: **all four real screenshots supplied** by the maintainer from the Android Studio
emulator on Windows and integrated into the main README. Each PNG is 1080 × 1920 (9:16).
They show the light/dark catalog, Aurora details in dark theme and Sentinela Neon details
in light theme (including Edição 5). The images are readable at their native resolution;
no private content, keyboard, dialog or development overlay was observed in visual inspection.
Ordinary Android status/navigation bars are preserved.

The two supplied detail filenames were reversed. They were swapped to match their actual
content without re-encoding, cropping or changing any bytes within either capture. Catalog
files are unchanged. No screenshot was generated, edited or captured in this cloud workspace.
The maintainer reports real emulator provenance; PNG dimensions alone do not prove provenance.
Device model/API level and the exact installed capture build were not independently verified.

The following instructions remain a reproduction guide. For a matching refresh, capture
Aurora in dark mode; a future light-mode Aurora capture is optional and must be labeled
accordingly. Compose previews and generated cover artwork are not screenshot substitutes.

## Capture the reviewed application on Windows

Use Android Studio's working emulator and this repository's `feature/offline-comics-details`
branch. The reviewed application source is commit
`ced34c2c04efcc45492affe87b31936a54885da9`; this documentation change does not modify it.
Check `git status --short` and `git rev-parse HEAD` and record the actual source commit
if it changes. Do not discard local work or capture a stale installed APK.

From PowerShell at the repository root, with the project's JDK 17 and Android SDK
configured, build and install the offline debug APK:

```powershell
.\gradlew.bat :app:assembleDebug "-PCOMICS_BASE_URL="
# Continue only after BUILD SUCCESSFUL.
$androidSdkPath = "$env:LOCALAPPDATA\Android\Sdk"
# If Android Studio uses a different SDK location, set that path above.
$adbPath = Join-Path $androidSdkPath "platform-tools\adb.exe"
& $adbPath devices -l
# Replace this with the serial printed by adb, not the AVD's display name.
$deviceSerial = "emulator-5554"
& $adbPath -s $deviceSerial shell getprop ro.build.version.sdk
& $adbPath -s $deviceSerial shell wm size
& $adbPath -s $deviceSerial shell wm density
& $adbPath -s $deviceSerial install -r "app\build\outputs\apk\debug\app-debug.apk"
# Continue only after installation reports Success.
& $adbPath -s $deviceSerial shell cmd uimode night no
& $adbPath -s $deviceSerial shell am force-stop com.example.js_comics
& $adbPath -s $deviceSerial shell am start -n com.example.js_comics/.MainActivity
```

The app follows the system light/dark setting. The `cmd uimode` commands are suitable
for the API 34 emulator used by CI; if unavailable on your device, use Android Settings
→ Display → Dark theme instead. Use a portrait emulator, normal font/display settings
for this gallery, and the same device resolution for all four captures. Do not change
application code or crop a screen to make more content appear.

Wait for the covers to finish loading. Confirm **Demonstração offline** is visible on
the catalog. Turn off any developer pointer/layout overlays in Android Settings. Close
the keyboard, notification shade, dialogs and unrelated windows. Check the full screen
for private notifications, account information or other sensitive content before saving.

## Save native PNG bytes

Use the function below after navigating to each requested screen. It captures the actual
Android display and pulls the PNG without PowerShell binary-output redirection, which
can corrupt `adb exec-out` output on older Windows PowerShell versions.

```powershell
New-Item -ItemType Directory -Force "docs\screenshots" | Out-Null
function Save-ComicsScreenshot([string]$Name) {
    & $adbPath -s $deviceSerial shell screencap -p /sdcard/comics-readme.png
    if ($LASTEXITCODE -ne 0) { throw "Android screen capture failed" }
    & $adbPath -s $deviceSerial pull /sdcard/comics-readme.png "docs\screenshots\$Name"
    if ($LASTEXITCODE -ne 0) { throw "PNG transfer failed" }
    & $adbPath -s $deviceSerial shell rm /sdcard/comics-readme.png
}
```

Capture in this order, using touch/mouse interaction with the actual app:

| File | Navigate and inspect before capture | PowerShell call |
| --- | --- | --- |
| `catalog-light.png` | Initial catalog, light theme. Show Aurora, Cidade de Papel and Além da Órbita; keep the top position where possible. | `Save-ComicsScreenshot "catalog-light.png"` |
| `details-aurora.png` | Enable dark mode and relaunch using the commands below, then tap Aurora. Show the back toolbar, full title, metadata, cover and beginning of the synopsis. | `Save-ComicsScreenshot "details-aurora.png"` |
| `details-sentinela.png` | Enable light mode (`night no`) and relaunch, then scroll to Sentinela Neon and open it. Confirm Memórias Roubadas and Edição 5 on the cover. Show the actual details interface. | `Save-ComicsScreenshot "details-sentinela.png"` |
| `catalog-dark.png` | Enable dark mode using the commands below and reopen the initial catalog. Inspect title/body contrast and cover visibility. | `Save-ComicsScreenshot "catalog-dark.png"` |

For Aurora and the last catalog capture, use dark mode; for Sentinela, use the same commands with `night no`:

```powershell
& $adbPath -s $deviceSerial shell cmd uimode night yes
& $adbPath -s $deviceSerial shell am force-stop com.example.js_comics
& $adbPath -s $deviceSerial shell am start -n com.example.js_comics/.MainActivity
# Wait for the initial catalog and its covers, then capture catalog-dark.png.
```

If the emulator's viewport cannot show all requested content simultaneously, report that
limitation and supply the genuine capture. Do not stitch screens, edit UI text, insert
covers, generate mockups or use previews as substitutes. Android Studio's Running Devices
**Take Screenshot** action is also acceptable: save the raw screenshot without a device
frame, effects or cropping, under the same filename.

Restore the device's original theme after capture. For a device originally following its
system schedule, `adb shell cmd uimode night auto` restores automatic mode; otherwise use
its previous setting. Share the PNGs with the source commit, Android API level, device/AVD
name, resolution and density. These details establish reported provenance; file format or
dimensions alone cannot prove that an image is an authentic capture.

## Review and README integration for future captures

- Verify all four are genuine decodable PNGs, with consistent full-screen dimensions and
  the requested screen/theme/content. Confirm no secrets, overlays or unrelated content.
- If optimizing PNG encoding, preserve dimensions, colors and every decoded pixel; keep
  the supplied originals outside the repository until verification succeeds.
- Add a compact gallery to the main README's existing **Screenshots** section only after
  checking the files exist. Use two rows of two images, relative `docs/screenshots/` paths,
  descriptive alt text/captions and consistent displayed widths around 250–300px. Check
  GitHub desktop and mobile rendering; keep images scalable rather than forcing wide rows.
- Remove the pending table only as real captures become available. Never claim all four
  captures exist if any are missing. Check every relative image path and run
  `git diff --check` before review.

No commit, push, PR update or merge is authorized by these capture instructions.
