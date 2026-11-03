# CI SDK setup correction — review record

Date: 2026-10-21. Branch: `modernization/phase-one`.
Base: `6c1b26bf31d4369476294b6e330aae1a3795260f`.
Changes are uncommitted and await human review. No push, merge, repository setting
change or Android source/toolchain upgrade is part of this correction.

## Confirmed failure and root cause

The owner supplied the official log diagnosis for
[run 39479357638](https://github.com/jSantini/comics_sample/actions/runs/39479357638):
`android-actions/setup-android` attempted `sdkmanager tools`, which failed with
`Warning: Failed to find package 'tools'` and exit code 1. The Compose job was skipped
because it depends on the verification job. These are remote results for the base
commit, not results for this uncommitted correction.

The pinned action was inspected at
[`9fc6c4e9069bf8d3d10b2204b1fb8f6ef7065407`](https://github.com/android-actions/setup-android/tree/9fc6c4e9069bf8d3d10b2204b1fb8f6ef7065407):

- `action.yml` defaults `packages` to `tools platform-tools`.
- `src/main.ts` reads that input, splits on spaces and invokes sdkmanager once per
  package. This explains why the separate installation step never ran.
- The same source maps command-line tools download `12266719` to revision `16.0`
  and installs/exports its sdkmanager before processing the package list.
- `git ls-remote https://github.com/android-actions/setup-android.git refs/tags/v3`
  returned the existing full SHA above. The Linux archive URL
  `https://dl.google.com/android/repository/commandlinetools-linux-12266719_latest.zip`
  returned HTTP 200 to a HEAD request. This verifies availability, not archive integrity
  or execution of its contents. The action's upstream download behavior is unchanged.

## Minimal correction

Both jobs explicitly set `cmdline-tools-version: '12266719'` and
`packages: 'platform-tools platforms;android-34 build-tools;34.0.0'` on the existing
SHA-pinned action. The obsolete standalone package `tools` is never requested by this
configuration. Remove the redundant separate installation step from `verify`.

Retain the action rather than adding custom downloader/license/bootstrap scripts.
The input override uses the action's supported API and keeps the fix small. JDK 17,
Android API 34, project build tools, Gradle/AGP/Kotlin versions, all other action SHAs,
read-only permissions, caches, timeouts, KVM setup, artifacts, URL checks, JVM/lint
commands and connected Compose tests are unchanged. No secrets are needed.

The existing emulator runner was also inspected at its pinned SHA. It installs the
emulator/system image separately and does not request `tools`. Its existing bootstrap
can prefer the hosted runner's `cmdline-tools/latest` and additionally installs Build
Tools 37.0.0. This correction does not change that pre-existing action behavior or
claim to pin the entire hosted SDK/emulator distribution. Build Tools 34.0.0 are
explicitly available in both jobs. Platform-tools and the emulator remain upstream
mutable downloads; hosted Ubuntu images also evolve.

## Local validation

The checks below were executed locally. They cannot establish that a
GitHub-hosted job passes. No emulator binary or `/dev/kvm` is available locally;
Compose execution and visual review remain pending. The existing local SDK has
command-line tools 23.0, so local Gradle results do not exercise the action's 16.0
bootstrap or reproduce a fresh Ubuntu runner.

Executed checks:

- `/tmp/comics-actionlint/actionlint -version`: version 1.7.7.
- `/tmp/comics-actionlint/actionlint .github/workflows/android.yml`: exit 0.
  ShellCheck is not installed; no separate ShellCheck result is claimed.
- A Python/PyYAML comparison against `git show HEAD:.github/workflows/android.yml`
  allowed only the two SDK input overrides and removal of the redundant SDK step;
  the resulting workflow structures matched exactly. Assertions also checked both
  package lists, command-line version, JDK 17, `contents: read` and full action SHAs.
- `python3 scripts/verify_catalog_url.py`: exit 0, seven valid literal round-trips,
  12 invalid URLs rejected, demo default restored.
- `git diff --check` and whitespace checks for the new document: passed.
  Directed private-key/token-pattern checks of the two changed files found no matches.
  These checks are not a full security/dependency audit.

The full local Gradle command is:

```bash
./gradlew --no-daemon --max-workers=2 clean :app:assembleDebug \
  testDebugUnitTest :domain:test :app:lintDebug :data:lintDebug :component:lintDebug \
  :app:assembleDebugAndroidTest -PCOMICS_BASE_URL=
```

Actual outcome: **BUILD SUCCESSFUL in 2m 45s**, 171 actionable tasks (163 executed,
8 up-to-date). Both debug APK and instrumented test APK were generated. Current-run
JUnit XML reports confirm **32 tests, zero failures/errors/skips**: ViewModel 6,
image client 7, HTTP 7, mapping 6, repository 6. Domain and component test tasks
reported NO-SOURCE; they are not counted as passed tests.

Lint XML confirms zero errors in all three Android modules: app has the same 15
legacy warnings, data/component have none. The existing SDK XML-version warning
(older AGP versus newer SDK metadata) and Hilt deprecation note remain non-blocking.
No suppressions or toolchain changes were added. Compose tests **compiled only**;
no instrumentation or visual result is claimed.

Temporary logs and downloaded upstream inspection files stayed under `/tmp`, outside
the repository. The final diff contains only the workflow and this record; Android
source, test contracts, demo configuration and authorship history are unchanged.
The existing Phase 2 validation record describes its earlier execution and has not
been rewritten as if it validated this correction.

## Required follow-up after human review

After authorized publication, inspect the new GitHub run: SDK installation in both
jobs, verification checks, actual connected Compose results and artifact availability.
Do not infer success from this local build or static validation. Existing action
Node-runtime/deprecation notices are outside this targeted package-selection fix;
no broad action/toolchain upgrade was attempted. No CI approval, device execution,
visual acceptance or human approval is asserted here.
