# Visual refinement — compact Portuguese catalog and details

Date: 2026-10-29. Branch: `feature/offline-comics-details`.
Base: `edf5fc59f382d271fb4817a1759006205dde00fa`.
No commit, push or merge. The owner reported 38 JVM and 36 instrumented tests passing
on the functional base; this record concerns the new visual diff, not that earlier run.

## Scope and visual decisions

- Catalog: one row per card, cover on the left and title/genre/edition/short synopsis on
  the right. Cover slot is at most 88dp × 132dp. Summary is at most three lines with ellipsis;
  titles/metadata can wrap, and there is no fixed card height. Stable LazyColumn IDs,
  whole-card click semantics and all loading/empty/error/retry states remain.
- Original local aspect ratios come from the six known asset dimensions, only in Android
  presentation: square Aurora/City/Neon, 565:800 Orbit, 1170:1790 Eclipse and 512:640 Vigilante.
  No header/bitmap decoding is performed in composition to obtain those dimensions.
- Remote covers retain a temporary 2:3 unknown-size fallback and adopt a successful painter's
  finite positive intrinsic ratio. Unspecified/invalid sizes keep the safe fallback.
  ContentScale.Fit is retained. The image itself no longer has rounded clipping, preserving
  corner information/signatures; rounded Material 3 cards remain. No cover bytes changed.
- Details: compact Material 3 TopAppBar with an icon button at the start, accessible “Voltar”
  description and “Detalhes” title. A compact source label, full title and available metadata
  precede the image, whose bounds are at most 280dp × 200dp at its own aspect ratio. Full
  synopsis remains below and scrolls with content. Back remains reachable in the fixed bar.
- Remove the unconditional Activity top bar to avoid double bars on details and save vertical
  space in the catalog. Keep the root Scaffold and consume its padding as window insets;
  the detail Scaffold handles its own toolbar. This is presentation-only inset/toolbar work,
  not a navigation or ViewModel restructuring. Scoped ExperimentalLayoutApi opt-ins are
  required by the retained Compose version; no dependency/global compiler change was made.
- Visible interface resources are Portuguese: Catálogo, Demonstração offline, Edição, Série,
  Gênero, Publicado em, Sinopse, Voltar, progress, recovery and cover accessibility labels.
  App name “Comics” remains a proper name. Provider content is displayed as received rather
  than translated/fabricated. The shared LoadingBar label was translated in :component.
- Previews retain all states and add 320dp, dark 2x-font, portrait-detail and incomplete remote
  examples. Previews are definitions; no rendered screenshot or visual sign-off is claimed.

## Preserved behavior and assets

Domain/data/repositories, ViewModel, NavHost/ID route logic, Hilt, ImageLoaderFactory,
HTTPS/TLS/redirect policy, IO fetch/decode dispatchers, request precision and Coil version
are unchanged. No backend, Room, additional navigation layer, feature or library was added.
All six original JPGs and embedded credits match the base byte-for-byte. Symbolic R mappings
are unchanged. Licensing remains pending in `image-licensing.md`; layout work grants no rights.

No DTO dimensions or new domain fields were introduced for presentation sizing. The
maximum cover bounds are independent of text; larger fonts can increase card/content height
and the list/detail body can scroll. Device/TalkBack testing is still needed for the new UI.

## Tests updated/added

- Existing metadata, volume-versus-issue and placeholder assertions now use strings.xml,
  rather than hardcoded English interface text. Existing test methods were retained.
- Two new JVM cases check all six aspect ratios and rejection of invalid remote dimensions.
- Five new instrumented visual cases cover square cover bounds, three-line summary at
  2x text/320dp/dark mode beside the image, the detail height cap/metadata order, back button
  position/Portuguese accessibility, and a landscape remote image adopting its ratio.
  Remote image geometry uses an injected loader/bitmap; no real server is contacted.
- Existing catalog-navigation/Activity recreation, Repository request counter, cancel/retry,
  HTTP/TLS and image pipeline suites remain enabled.

## Commands and actual results

JDK 17.0.21, Gradle 8.2, AGP 8.2.2, Kotlin 1.9.0, Compose compiler 1.5.1 and the existing BOM
and Navigation 2.7.7 retained. SDK Platform 34 / Build Tools 34.0.0.

```bash
./gradlew --no-daemon --max-workers=2 clean :app:assembleDebug \
  testDebugUnitTest :domain:test :app:lintDebug :data:lintDebug :component:lintDebug \
  :app:assembleDebugAndroidTest -PCOMICS_BASE_URL=
```

Two preliminary attempts exposed compatibility errors introduced by this visual work:

1. Main compilation required scoped ExperimentalLayoutApi opt-ins for consumeWindowInsets
   (failed in 58s). Added local opt-ins; did not upgrade Compose or add a global flag.
2. Main compilation then passed, but instrumented compilation rejected geometry convenience
   APIs unavailable in this toolchain (failed in 1m15s). Tests now calculate dimensions and
   center positions from supported node bounds. Height/position assertions were retained.

These were project-change compatibility issues, not environment failures. No failed tests
were removed, assertions disabled or test baselines introduced.

Final corrected clean command: **BUILD SUCCESSFUL in 1m17s**, 171 tasks (163 executed,
8 up-to-date). Debug APK and instrumented test APK were generated.

- **40 JVM tests executed, zero failures/errors/skips**: presentation 5, ViewModel 7,
  image client 7, HTTP 7, mapping 6, repository 6 and demo 2. Domain/component test tasks
  report NO-SOURCE and are not counted as passed tests.
- **41 app instrumented methods compiled**, including the five new visual methods;
  none were executed in this cloud validation. The 36 pre-existing methods remain.
- Lint: **zero errors/fatal findings**, with the same totals of 23 app warnings, 2 data
  warnings and 5 component warnings. App: 7 UnusedAttribute, 7 UnusedResources,
  1 RedundantLabel, 8 GradleDependency; data/component warnings are GradleDependency.
- `git diff --check`, directed secret-pattern/whitespace checks and protected-path checks
  passed. All six current JPGs match their HEAD bytes exactly.
- Domain/data, ViewModel, Navigation routes, DI/security/transport, asset files, workflow,
  root/app Gradle files and gradle.properties have no diff. Only presentation, its tests,
  Portuguese resources and related documentation changed.
- The unchanged URL script was not rerun for this visual-only stage; older URL/CI results
  are not presented as new runs. No global Kotlin compiler settings were altered.

## Pending validation and limits

The cloud environment still has no emulator/system image or `/dev/kvm`; new instrumented
methods cannot be declared executed. The owner's successful functional-base instrumentation
run does not prove this new visual diff. No app screenshot, runtime pixel/contrast/scroll
measurement or new GitHub Actions run is asserted here. In particular, check the new root
insets/toolbar on the real emulator, source art readability at the smaller cover size, square
and portrait covers, remote images, Portuguese labels, dark mode and large fonts at 320dp.

The entire image is fitted without cropping; reducing its displayed size can make small
embedded credits harder to read, so actual visual acceptance remains an owner decision.
No image has been altered to hide credits. No process/repository behavior or compiler
execution strategy was changed to achieve these layout results.

## Changed files

- `README.md`
- `app/src/androidTest/java/com/example/js_comics/ui/view/ComicCoverPipelineTest.kt`
- `app/src/androidTest/java/com/example/js_comics/ui/view/ComicNavigationTest.kt`
- `app/src/androidTest/java/com/example/js_comics/ui/view/ComicVisualLayoutTest.kt`
- `app/src/main/java/com/example/js_comics/MainActivity.kt`
- `app/src/main/java/com/example/js_comics/ui/view/ComicCover.kt`
- `app/src/main/java/com/example/js_comics/ui/view/ComicDetailsScreen.kt`
- `app/src/main/java/com/example/js_comics/ui/view/ComicsListPreviews.kt`
- `app/src/main/java/com/example/js_comics/ui/view/ComicsListScreen.kt`
- `app/src/main/res/values/strings.xml`
- `app/src/test/java/com/example/js_comics/ComicPresentationTest.kt`
- `component/src/main/res/values/strings.xml`
- `docs/cover-hardening-validation.md`
- `docs/visual-refinement-validation.md`
