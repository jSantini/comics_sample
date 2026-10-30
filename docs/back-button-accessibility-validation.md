# Detail back button — minimum accessible target

Date: 2026-10-29. Branch: `feature/offline-comics-details`.
Base: `ea4e055703844be07ce511469b3dd9425170964c`.
No commit, push or merge. This is a targeted accessibility correction.

## Report and investigation

The owner reported 39/41 instrumented tests passing and two failures:
`ComicNavigationTest.invalidIdHasRecoveryAndBackCallback` and
`ComicVisualLayoutTest.detailToolbarHasPortugueseBackButtonAtTheStart`.
Both measured the back node at 40dp versus an asserted minimum of 48dp.

The back action was a Material 3 IconButton with only `testTag("details_back")` as its
supplied modifier. Inspection of the resolved Material 3 1.1.1 bytecode shows the default
IconButton applies minimumInteractiveComponentSize, then a size from IconButtonTokens
(StateLayerSize = 40dp), and then its clickable modifier. The theme does not override
minimum-interaction enforcement. Implicit touch expansion/outer layout reservation is
not the same as an explicitly sized clickable/semantic node; the reported 40dp result
is consistent with relying on those defaults. It does not establish that every possible
implicit hit expansion was only 40dp.

## Correction

`ComicDetailsScreen.kt` now constrains the IconButton itself with:

```kotlin
Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp).testTag("details_back")
```

These minimum constraints propagate into the component's inner size/clickable layout;
it is not padding or a separate tagged container outside the action. The visual icon is
explicitly 24dp and remains centered. The existing TopAppBar height, Portuguese title,
“Voltar” content description, Material button semantics, onBack callback and navigation
stack behavior remain unchanged. No global touch-target policy or semantics override was
introduced, and no app-bar height increase was needed.

Both failing tests retain their >=48dp height assertions and now also assert >=48dp width.
The existing 2x-font dark-detail test checks both dimensions. One added instrumented test
uses 2x font, dark theme and a 320dp host, asserts both dimensions and performs an actual
pointer tap at (3dp, 24dp) within the button, outside the centered 24dp icon. It expects
exactly one back callback and keeps the Portuguese accessibility description assertion.
No minimum, assertion, tolerance, click or screen-reader check was weakened or removed.

## Actual local validation

JDK 17.0.21 / existing Gradle 8.2, AGP 8.2.2, Kotlin 1.9.0, Compose/Navigation/Hilt versions.

```bash
./gradlew --no-daemon --max-workers=2 clean :app:assembleDebug \
  testDebugUnitTest :domain:test :app:lintDebug :data:lintDebug :component:lintDebug \
  :app:assembleDebugAndroidTest -PCOMICS_BASE_URL=

# Repeat instrumented compilation after adding the final pointer/large-font assertion.
./gradlew --no-daemon --max-workers=2 :app:assembleDebugAndroidTest -PCOMICS_BASE_URL=
```

- Clean build: **BUILD SUCCESSFUL in 1m16s**, 171 tasks (163 executed, 8 up-to-date).
- Current-run XML: **40 JVM tests, zero failures/errors/skips**. Domain/component test
  tasks are NO-SOURCE, not extra passing tests.
- Lint: **zero errors/fatal findings**; unchanged warning totals: app 23, data 2, component 5.
- Final instrumented compilation: **BUILD SUCCESSFUL in 19s**, 88 tasks (6 executed,
  82 up-to-date). **42 app instrumented methods compiled, none executed locally.**
- Original test methods and 48dp height requirements preserved; one pointer test added.
- `git diff --check` and directed file/secret-pattern checks passed.
- Domain, repositories, ViewModel, Coil, NavHost/routes, UI text, assets, theme, CI and
  toolchain files have no diff. Only the back button, related tests and this record changed.

No emulator/system image or /dev/kvm is configured here. ADB enumeration also failed because
it tried to create its default .android directory under a read-only home. This preflight
failure is an environment limitation; it is not a project build failure or a connected-test
result. No permission/configuration change was requested for an unneeded emulator setup.
The owner's AVD label “flutter_emulator(AVD) - 16” was not independently inspected. The
project minSdk remains 21, so this report does not certify execution on Android API 16;
the effective SDK level must be checked on the actual device if relevant.

## Files and remaining validation

- `app/src/main/java/com/example/js_comics/ui/view/ComicDetailsScreen.kt`
- `app/src/androidTest/java/com/example/js_comics/ui/view/ComicNavigationTest.kt`
- `app/src/androidTest/java/com/example/js_comics/ui/view/ComicVisualLayoutTest.kt`
- `docs/back-button-accessibility-validation.md`

Rerun the 42 instrumented methods on the real emulator, including the two original failures
and the new edge-of-icon pointer case. Verify TalkBack focus/activation and large-font bar
appearance. Successful compilation/JVM tests are not a claim that the device regressions
have passed or that visual/human acceptance is complete.
