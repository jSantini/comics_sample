# Railway integration validation — 2026-11-03

## Implemented behavior

Offline is the session default. Hilt supplies the existing DemoComicsRepository and a
qualified RemoteComicsRepository. The ViewModel selects one repository per load, owns
its Job and exposes source and UI state as read-only StateFlows. A source change cancels
the previous Job and clears the previous items synchronously. ensureActive checks guard
both success and failure publication, including repositories that return after cancellation.
Retry uses the selected source; duplicate retry during a load is ignored. No remote failure
falls back to offline content. Activity recreation retains the source/catalog; process death
starts Offline again. Navigation resolves an ID exclusively against the current loaded catalog.

The public production URL is the default COMICS_BASE_URL. An explicitly empty property
builds the offline-only configuration: no Retrofit client is constructed, Offline works,
and selecting Online reports a recoverable network error. No credentials, redirects,
logging interceptor, image transport relaxation, dependency upgrades or backend edits.

## Contract and artwork

Verified directly against GET https://comics-showcase-api-production.up.railway.app/api/comics.
The checked-in response fixture has data.results containing six entries with IDs 1–6.
Mapped fields: id, title, series, issueNumber, volumeNumber, genre, publicationYear,
shortSynopsis, synopsis, coverKey and description. Required envelope, IDs, title and
unique IDs are validated. Optional metadata remains nullable; absent synopsis text falls
back to description. Supplied issue/volume/year values must be positive.

The fixture is compared as complete domain objects against DemoComicsRepository through
Retrofit/MockWebServer. ID 5 is volume 1 with no issue; other issue numbers are 1, 3, 2, 5,
and 1. The six explicit cover keys map through the existing Android resource allowlist.
All six WebP files and existing story text are unchanged. No image download is required
for the Railway catalog. Unknown keys retain existing secure image/placeholder behavior.

## Files changed

- data/build.gradle.kts: default public Railway URL; existing URL guard preserved.
- data/src/main/java/com/example/js_comics/data/di/NetworkModule.kt: both source bindings.
- data/src/main/java/com/example/js_comics/data/remote/response/ComicsResponseDto.kt: metadata mapping.
- app/src/main/java/com/example/js_comics/ui/view/ComicsViewModel.kt: session source and cancellation.
- app/src/main/java/com/example/js_comics/ui/view/ComicsCatalogNavigation.kt: selector callbacks.
- app/src/main/java/com/example/js_comics/ui/view/ComicsListScreen.kt: accessible source controls/indicator.
- app/src/main/res/values/strings.xml: Portuguese selector labels.
- app/src/test/java/com/example/js_comics/ComicsViewModelTest.kt: source/error/retry/cancellation tests.
- app/src/test/java/com/example/js_comics/image/ComicImageClientTest.kt: portable localhost TLS fixture.
- data/src/test/java/com/example/js_comics/data/ComicsMappingTest.kt: metadata/number validation.
- data/src/test/java/com/example/js_comics/data/ComicsHttpTest.kt: complete six-comic contract test.
- data/src/test/resources/railway-comics.json: captured public response, no CI network dependency.
- app/src/androidTest/java/com/example/js_comics/ui/view/CatalogRepositoryNavigationTest.kt:
  source switching, distinct source details and Activity recreation.
- scripts/verify_catalog_url.py: actual production URL round-trip case.
- README.md and this report: behavior, launch instructions and validation evidence.

## Validation

JDK 17.0.15, existing Gradle 8.2 and Android SDK configuration. No toolchain upgrade.

- JVM: **47 passed, 0 failed** (app 23, data 24).
- Lint: **0 errors**, 34 warnings (app 25, component 5, data 4).
- Production-configured debug app and instrumented APK compilation: passed.
- Final clean offline-only build (empty COMICS_BASE_URL): **BUILD SUCCESSFUL**,
  171 tasks (163 executed, 8 up-to-date); JVM tests, lint and instrumented compilation passed.
- URL/security script: **20 passed** (8 generated Java literal round-trips, 12 invalid URL rejections).
- git diff --check: passed.
- Instrumented execution: attempted on emulator-5554. Gradle/ddmlib timed out fetching
  device properties and did not produce test results. A direct adb install also stalled;
  the identified test/install attempts were stopped. **No instrumented pass is claimed.**

The initial TLS tests exposed Windows resolving loopback to kubernetes.docker.internal.
The test URL now explicitly uses localhost to match the generated certificate SAN.
Certificate, hostname, insecure-URL and redirect rejection assertions remain enabled;
production transport is unchanged.

Reproduce the checks:

```powershell
$env:JAVA_HOME = 'C:\Users\Jean Santini\.jdks\ms-17.0.15'
$env:GRADLE_USER_HOME = 'C:\Users\Jean Santini\.gradle'
.\gradlew.bat --console=plain --max-workers=2 clean :app:assembleDebug testDebugUnitTest lintDebug :app:assembleDebugAndroidTest -PCOMICS_BASE_URL=
python scripts/verify_catalog_url.py
# On a responsive emulator/device:
.\gradlew.bat :app:connectedDebugAndroidTest -PCOMICS_BASE_URL=
```

## Launch and remaining checks

Run `.\gradlew.bat :app:installDebug` with no COMICS_BASE_URL override, open Comics,
and tap Online. Alternatively pass
`-PCOMICS_BASE_URL=https://comics-showcase-api-production.up.railway.app/api/` explicitly.
Offline is always the startup selection; there is no persistence or offline cache of API data.
An empty URL intentionally disables the backend; Online then displays a network error.

The endpoint contract is verified, but end-to-end live Online rendering on Android still
needs manual verification after fixing the local emulator/ADB connection. Rerun the compiled
instrumented suite, then check Online/Offline navigation, airplane-mode recovery, long text,
dark theme, font scaling and TalkBack. Existing screenshots retain their offline provenance.
No commits, pushes or merges were made.
