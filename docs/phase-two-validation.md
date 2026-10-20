# Phase 2 — local validation and review record

Date: 2026-10-20. Branch: `modernization/phase-one`.
Base: `593ee6a6ae15446fcde662fd167b846928ca44b3`.
Prepared for publication exclusively on this review branch as a separate Phase 2 commit.
No merge or master update is part of this delivery. Existing authorship is preserved and
Codex assistance is identified in the commit message. Human acceptance remains pending.

## Scope and decisions

- Add a pinned GitHub Actions workflow for relevant code PRs, selected branch pushes
  and manual dispatch. Separate build/JVM/lint verification from emulator UI execution.
- Preserve all domain/repository/ViewModel contracts, fictional demo data, URL literal
  hardening, image transport policy and the Phase 1 JVM suites. No backend or Marvel dependency.
- Keep the existing Kotlin/AGP/Gradle/JDK/SDK versions and module graph. No new library,
  persistence, navigation, feature screen or agent infrastructure was introduced.
- Refine the existing Material 3 screen: source identity, hierarchy, adaptive-height cards,
  full-width descriptions, decorative covers, scrollable recovery/loading content, headings,
  polite error announcements and a retry control at least 48dp high.
- Add deterministic screen tests with explicit states/callbacks and null image URLs. They
  use no ViewModel or Hilt test injection and do not require catalog access.
- Add four state previews, each with light, dark and 2x-font configurations. Previews are
  source definitions, not captured screenshots or proof of device rendering.

## Environment

Temurin JDK 17.0.18; Android SDK 34 / Build Tools 34.0.0; Gradle 8.2; AGP 8.2.2;
Kotlin 1.9.0; Compose compiler 1.5.1. Commands ran from the checkout root with JDK/SDK
configured. The repository toolchain was not upgraded. The pre-existing SDK XML-version
warning (older AGP versus newer SDK metadata) and the generated Hilt deprecation note
were non-blocking. Neither was hidden by changing verification settings.

## Reproducible URL checks — executed

```bash
python3 scripts/verify_catalog_url.py
```

Exit 0: **7 valid literal round-trips and 12 rejected invalid inputs**. Each valid
fixture used actual Gradle BuildConfig generation, javac compilation and Java runtime
comparison. The finalizer restored default BuildConfig. No catalog server was contacted.
The script is unchanged from the reviewed hardening commit.

## Clean build, JVM tests, lint and instrumented compilation — executed

```bash
./gradlew --no-daemon --max-workers=2 clean :app:assembleDebug \
  testDebugUnitTest :domain:test :app:lintDebug :data:lintDebug :component:lintDebug \
  :app:assembleDebugAndroidTest -PCOMICS_BASE_URL=
```

Actual outcome: **BUILD SUCCESSFUL in 1m 41s**. 171 actionable tasks: 163 executed,
8 up-to-date. The clean build generated:

- `app/build/outputs/apk/debug/app-debug.apk`
- `app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk`

Generated `BuildConfig.COMICS_BASE_URL` was verified to be the empty String (demo).

The current-run JUnit XML files were read and checked:

| JVM suite | Executed | Failures/errors/skipped |
| --- | ---: | ---: |
| ComicsViewModelTest | 6 | 0/0/0 |
| ComicImageClientTest | 7 | 0/0/0 |
| ComicsHttpTest | 7 | 0/0/0 |
| ComicsMappingTest | 6 | 0/0/0 |
| ComicsRepositoryTest | 6 | 0/0/0 |
| Total | 32 | 0/0/0 |

These are the unchanged Phase 1/hardening tests; all executed again after clean.
`domain:test` and `component:testDebugUnitTest` have NO-SOURCE outcomes, not passed tests.
No coverage percentage was measured.

Lint XML files reported zero errors: `app` has the same 15 legacy warnings
(7 UnusedAttribute in launcher artwork, 7 UnusedResources colors, 1 RedundantLabel),
while `data` and `component` have no issues. No baseline or disabled assertion/rule was
added to obtain these results. The unused old cover-placeholder string was removed rather
than introducing an additional unused-resource warning.

## Compose tests — compiled, NOT executed locally

`ComicsListScreenTest` contains eight new tests:

1. Loading exposes indeterminate progress and no retry.
2. Content exposes fixture text, demo identity and a heading.
3. Remote content does not claim demo mode.
4. Empty state offers an enabled retry with click semantics and a 48dp minimum height.
5. Error presents a safe recovery message and calls retry once.
6. Injected retry transitions error → loading → content.
7. Dark content at 2x font remains scrollable to the final fixture item.
8. Dark error at 2x font keeps retry reachable through scrolling.

All eight compiled into the instrumented test APK; the pre-existing app context test is
also retained. Compilation is not execution. There is no local connected-test report.

Preflight found no Android emulator binary, no installed system-image directory and no
`/dev/kvm`. No configured device/emulator was available, so connected tests were not
attempted. This is a local environment limitation, not a passing UI-test result or a
confirmed application failure. Run on a ready device/emulator with:

```bash
./gradlew --no-daemon --max-workers=2 :app:connectedDebugAndroidTest -PCOMICS_BASE_URL=
```

A preliminary compilation caught the older Compose matcher signature requiring an explicit
`ProgressBarRangeInfo.Indeterminate`. That test was corrected and the final clean build
compiled successfully; assertions were not removed. Device rendering, TalkBack, font
clipping, contrast, actual image decoding and screenshots still need human/device review.

## GitHub Actions — local configuration checks, remote outcomes separate

`.github/workflows/android.yml` has two jobs:

- `verify`: Temurin 17, SDK 34, Gradle cache, URL checks, clean debug/JVM/lint checks and
  test-APK compilation. Uploads XML/HTML reports and demo/test APKs for seven days.
- `compose`: depends on verify, configures KVM on an ephemeral Ubuntu 24.04 runner and
  runs the app instrumentation tests on an Android 34 google_apis x86_64 emulator.
  Uploads connected-test reports, including after a failure when reports exist.

Relevant code PRs and pushes to `master`, `main` and `modernization/phase-one` are
configured; documentation-only changes are excluded. Concurrency cancels obsolete runs.
There are no release, deployment, repository-write or merge steps.

All six action repositories are pinned to full commits resolved from their public version
tags during this task. Checkout credentials are not persisted; token permissions are only
`contents: read`. PR Gradle caches are read-only; no catalog credentials or secret inputs
are configured. Demo is explicitly selected in workflow commands/environment. SDK,
Gradle dependencies and emulator downloads require Internet, unlike demo app runtime.

Static workflow validation: **actionlint 1.7.7, exit 0**. Its official release archive
was SHA-256 checked against the release checksum file before use. A separate YAML check
verified event keys, SHA pins, read-only permissions and the empty catalog property.
ShellCheck was not installed; no independent shellcheck result is claimed.

This record captures local validation before publication. Runner/KVM availability,
action execution, artifacts and instrumented outcomes must be checked for the published
commit separately. A syntactically valid workflow is not evidence of a successful CI run.
Local Compose tests were compiled only; no local device or visual result is claimed.

## Security and repository hygiene

Directed checks cover new/modified source, fixtures, workflow and docs: no known legacy
authentication values or common real-secret patterns; no private keys, keystores, local
configuration, APKs, generated reports, caches or temporary validation tools in the diff.
Test certificates remain runtime-generated fixtures. Screenshots are marked Not captured;
no images, performance metrics or CI badges were invented.

The demo still has no remote image URLs or network repository dependency. Catalog/image
TLS and redirect policies are unchanged; cleartext remains disabled in the packaged app.
Workflow artifacts are limited to generated test/lint results and debug APKs, with no
secret/environment dumps. Historical credential rotation remains an owner responsibility.
These directed searches do not constitute a complete security or dependency-CVE audit.

`git diff --check` and new-file whitespace checks passed. The complete review diff includes
new workflow/test/preview/documentation files. Phase 1 history is preserved, with Phase 2
prepared as a separate review commit. No AGENTS.md or agent infrastructure was added.
Human acceptance remains pending.
