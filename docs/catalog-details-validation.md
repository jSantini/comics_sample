# Offline catalog and details — implementation and validation

Date: 2026-10-29. Working branch: `modernization/phase-one`.
Base: `ba1b8e187b0e0237cf21c6273241ff39a4742cb7`. No commit, push or merge.
Human review and visual/device acceptance remain pending.

> This is the historical pre-hardening execution record. Current images and added integration
> coverage are described in [cover-hardening-validation.md](cover-hardening-validation.md).
> PNG metrics/filenames and 25 compiled instrumentation methods below refer to that earlier snapshot.

## Recovery and historical inspection

The environment restart lost the previous uncommitted implementation, temporary fixtures
and JDK/SDK installation. Initial `git status --short` and `git diff --stat` were empty;
`git log -1 --oneline` showed `1f3ab8a` on the environment's `work` branch. The published
`modernization/phase-one` ref was fetched without tags and checked out at `ba1b8e1`.
That clean base preserved Phase 1/2, the SDK CI correction, Windows support and all six
original cover files. Only the lost/uncompleted catalog/detail scope was reapplied.

Inspection of the fetched branch ancestry, changed paths and original list source found
no earlier comic detail screen/navigation to reuse. The original screen exposed list,
item and image composables only. Existing list states and image security were reused.

## Decisions and preserved boundaries

- Append optional metadata to `Comic` and `ComicItem`; retain the original four constructor
  fields, including `description`. Existing call sites remain source-compatible. Provider
  description defaults to synopsis/short synopsis; absent remote series/edition/year/genre
  remain null. The remote DTO contract and validation are unchanged.
- Demo IDs 1–6 are stable and ordered, with the supplied titles, series, editions, 2026,
  genres and complete short/full Portuguese synopses. Eclipse is volume 1 with no issue.
- `:domain` stays Kotlin/JVM with no Android resource or navigation references. Only
  `ComicCover.kt` maps six explicit keys to Android drawables. Remote IDs overlapping demo
  IDs do not receive a demo cover: remote DTOs supply no cover key.
- Keep Hilt and the activity-scoped catalog ViewModel above the NavHost. Navigation Compose
  2.7.7 is the only added dependency, compatible with Kotlin 1.9.0/Compose 1.5.x and existing
  lifecycle 2.7.0. No Room, DataStore, Paging, new feature modules or backend.
- Routes carry only ID text, safely parsed as Int. Details look up the current shared catalog
  without another request. Missing/malformed/nonpositive/unavailable IDs show recovery.
  Explicit back and system back use the navigation stack. Saveable NavController state
  retains the selected ID; the ViewModel survives configuration changes. Process death
  reloads the catalog once through normal startup. No remote catalog is persisted.
- Catalog and details use the same cover component: local drawable first, otherwise the
  existing secure Coil loader, otherwise a labeled unavailable placeholder. Demo URLs stay
  null. HTTPS, redirect, TLS, cleartext, BuildConfig and cancellation protections are unchanged.
- Cards have ripple/click semantics, stable IDs, content-driven heights, genre/edition and
  short text. Detail metadata/full text scroll separately from a 48dp minimum back control.
  Portrait 2:3 cover containers use Fit instead of cropping differently shaped source art.

## Supplied assets and licensing

All six supplied `.png` files actually contained JPEG data. They were re-encoded to PNG,
without resizing, cropping, recoloring or generating new art. Pillow decoded each original
Git blob and current file; dimensions, mode and RGB bytes matched exactly. PNG signatures
and successful full decoding were checked for all six. Android resource packaging and
on-device decode are separate checks below.

| Key | Dimensions |
| --- | --- |
| aurora | 1024 × 1024 |
| cidade_papel | 1400 × 1400 |
| alem_orbita | 565 × 800 |
| sentinela_neon | 1024 × 1024 |
| eclipse | 1170 × 1790 |
| vigilante | 512 × 640 |

The covers were visually inspected as supplied, not as rendered app screenshots. The city
image visibly says “The Clockwork City” and credits “Dr. Tsvika B.P.”; the eclipse image
also contains a signature. Existing embedded credits/art were preserved. No provenance or
redistribution license was supplied. The owner must confirm image rights and suitability
for these fictional titles before distribution; this task does not establish licensing.

## Reproducible validation

JDK 17 and SDK Platform 34 / Build Tools 34.0.0 were installed outside the repository.
The recreated environment's Java did not initially use its network proxy; SDK manifest
retrieval failed and license acceptance had to be repeated after setting process-local
proxy/trust-store options. This is environment setup, not a project SDK/CI regression.
No proxy, trust-store path, SDK directory or compiler workaround was added to project config.

```bash
./gradlew --no-daemon --max-workers=2 clean :app:assembleDebug \
  testDebugUnitTest :domain:test :app:lintDebug :data:lintDebug :component:lintDebug \
  :app:assembleDebugAndroidTest -PCOMICS_BASE_URL=

python3 scripts/verify_catalog_url.py
```

Actual results:

- Clean command: **BUILD SUCCESSFUL in 9m 51s**, 171 actionable tasks (167 executed,
  4 up-to-date), with Temurin JDK 17.0.21. Kotlin 1.9.0, Gradle 8.2, AGP 8.2.2,
  compiler 1.5.1, existing BOM and Hilt 2.50 were retained.
- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`.
- Instrumented APK: `app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk`.
  All **25 app instrumented test methods compiled**, including 16 added methods. None ran locally.
- Current-run JUnit XML: **38 executed, zero failures/errors/skips**. Counts:
  ComicPresentationTest 3, ComicsViewModelTest 7, ComicImageClientTest 7,
  ComicsHttpTest 7, ComicsMappingTest 6, ComicsRepositoryTest 6, DemoCatalogTest 2.
  Domain/component test tasks are NO-SOURCE, not passed tests.
- Lint XML: zero errors/fatal findings. App has 23 warnings: 7 UnusedAttribute,
  7 UnusedResources, 1 RedundantLabel and 8 GradleDependency. Data has 2
  GradleDependency warnings; component has 5. The dependency warnings recommend newer
  available versions, including Navigation; no broad upgrade or suppression was applied.
  The 15 app resource/manifest warnings are the existing categories. These actual totals
  supersede the counts from earlier validation runs for this checkout.
- URL script: exit 0, **7 valid generated-Java literal round-trips and 12 rejected inputs**;
  demo BuildConfig restored in the finalizer. No catalog/image provider was contacted.
- AAPT processed all six real PNG resources. Independent Pillow decoding and comparison
  against `git show HEAD:<path>` confirmed unchanged dimensions/mode/pixels for each asset.
  The Android resource decoding test compiled but was not executed.
- `git diff --check`, new-file whitespace checks, scoped file-list checks and directed
  private-key/token-pattern scans: passed. No local configuration, APK/report/cache,
  credential, AGENTS.md or agent infrastructure was added. This is not a full secret/CVE audit.
- The workflow, root toolchain, `gradle.properties`, remote repository/DTO implementation,
  Hilt providers and HTTPS-only image client are unchanged. No global Kotlin compiler
  configuration or in-process strategy was introduced.

Temporary setup/downloads/caches remain in `/workspace/.comics-tools` (outside the checkout);
command logs remain under `/tmp/comics-*.log`. The first SDK manifest/license attempts
failed during environment preparation and were corrected using process-local proxy options.
The first complete project validation succeeded; no project compile/test failure was hidden.

## Added/extended behavioral coverage

- Demo: six stable IDs/keys, no remote images, repeatable source, exact metadata/short text
  and fixed UTF-8 digests of each complete supplied synopsis.
- Remote mapping: newly optional metadata stays absent, descriptions are preserved.
- Presentation/JVM: six explicit drawable mappings, unknown keys, full metadata projection,
  stable ID lookup and no extra repository load for repeated detail selections.
- Instrumented: real NavHost/card callbacks, correct selected ID, explicit and system back,
  saved destination restoration, complete details, issue versus volume, incomplete remote
  metadata, missing/invalid/unavailable IDs, malformed route, loading/empty/error recovery,
  dark details with 2x fonts at 320dp, and decoding all six bundled Android drawables.
- Existing HTTP/TLS/image restrictions, ViewModel retry/cancellation/concurrent-load and
  loading/content/empty/error/retry Compose suites remain enabled.

## Limitations and next review

No emulator/system image or `/dev/kvm` is available locally. Instrumented compilation is
not execution; no new connected result or app screenshot is claimed. Previews were defined,
not rendered or exported. Manual review still needs TalkBack, small-screen and large-font
rendering, visual contrast, images, navigation and process-recreation behavior. The saved-state
instrumentation test does not itself prove full process-death behavior.

The GitHub workflow is unchanged and can run the expanded instrumentation suite after
review/authorized publication. The earlier green CI run does not validate this uncommitted
diff. No CI pass, production readiness, human approval or measured performance is asserted.

Suggested review/commit grouping (not committed):

1. Domain metadata, six demo fixtures and repository/mapping tests.
2. Lossless cover encoding, presentation mapping, catalog/detail navigation and UI.
3. Presentation/navigation/resource tests, previews and documentation/evidence.

## Changed files

- `README.md`
- `app/build.gradle.kts`
- `app/src/androidTest/java/com/example/js_comics/ui/view/ComicCoverResourcesTest.kt`
- `app/src/androidTest/java/com/example/js_comics/ui/view/ComicNavigationTest.kt`
- `app/src/main/java/com/example/js_comics/ui/view/ComicCover.kt`
- `app/src/main/java/com/example/js_comics/ui/view/ComicDetailsScreen.kt`
- `app/src/main/java/com/example/js_comics/ui/view/ComicsCatalogNavigation.kt`
- `app/src/main/java/com/example/js_comics/ui/view/ComicsListPreviews.kt`
- `app/src/main/java/com/example/js_comics/ui/view/ComicsListScreen.kt`
- `app/src/main/java/com/example/js_comics/ui/view/ComicsViewModel.kt`
- `app/src/main/res/drawable-nodpi/cover_alem_orbita.png`
- `app/src/main/res/drawable-nodpi/cover_aurora.png`
- `app/src/main/res/drawable-nodpi/cover_cidade_papel.png`
- `app/src/main/res/drawable-nodpi/cover_eclipse.png`
- `app/src/main/res/drawable-nodpi/cover_sentinela_neon.png`
- `app/src/main/res/drawable-nodpi/cover_vigilante.png`
- `app/src/main/res/values/strings.xml`
- `app/src/test/java/com/example/js_comics/ComicPresentationTest.kt`
- `app/src/test/java/com/example/js_comics/ComicsViewModelTest.kt`
- `data/src/main/java/com/example/js_comics/data/repository/DemoComicsRepository.kt`
- `data/src/test/java/com/example/js_comics/data/ComicsMappingTest.kt`
- `data/src/test/java/com/example/js_comics/data/ComicsRepositoryTest.kt`
- `data/src/test/java/com/example/js_comics/data/DemoCatalogTest.kt`
- `docs/catalog-details-validation.md`
- `domain/src/main/kotlin/com/example/js_comics/domain/ComicsRepository.kt`
