# Cover performance and repository/navigation integration hardening

Date: 2026-10-29. Branch: `modernization/phase-one`.
HEAD remains `ba1b8e187b0e0237cf21c6273241ff39a4742cb7`.
This extends the existing uncommitted catalog/detail implementation; no commit, push,
merge, toolchain upgrade or agent infrastructure was introduced.

## Synchronous decode and final loading path

The reviewed `ComicCover.kt:41` used `Image(painterResource(resource), ...)` for local
covers. `painterResource` synchronously loads/decodes the resource during composition;
requesting a 104dp card did not request a sampled bitmap from an asynchronous loader.

The component now uses the existing Coil 2.5 `SubcomposeAsyncImage` path for both local
resource IDs and remote URLs. Requests are remembered by context/data, explicitly set
`fetcherDispatcher(Dispatchers.IO)` and `decoderDispatcher(Dispatchers.IO)`, and use
`Precision.INEXACT`. No ORIGINAL/fixed source-size request is set. Coil Compose resolves
finite constraints from the shared 2:3 cover container and samples decoding for the
card/detail target. Memory caching remains enabled in production.

The six explicit cover-key → R mappings are unchanged. A resource ID wins over a URL;
unknown keys may use the existing remote URL; no model/failure shows an accessible
placeholder. Loading has a separate “Loading cover” label. An optional ImageLoader
parameter is an internal testing seam; production still defaults to the Application's
ImageLoaderFactory and its existing HTTPS-only/no-redirect client. No relaxed trust,
HTTP allowance, host policy or network credential was introduced. Local IDs use resource
fetching and do not need HTTP. No production BitmapFactory/painterResource decode remains
in this cover component. The synchronous BitmapFactory call in the resource test is
an instrumentation-thread assertion, not a production UI path.

These are configured execution/size policies, not measured frame-time/heap gains.
The decoder thread/downsampling assertions below still require instrumented execution.

## Resource optimization — preserve original supplied bytes

The review baseline was the normalized PNG working tree: **10,459,473 bytes**. The six
files in HEAD were actually original JPEG bytes with `.png` suffixes. We verified those
JPEGs decode to exactly the same RGB pixels as the normalized PNGs. Final resources reuse
those supplied bytes under correct `.jpg` filenames, without resizing or re-encoding.
All six resource basenames and R.drawable mappings remain stable. JPEG is supported by
minSdk 21; no decoder dependency or SDK change is needed.

An intermediate lossless WebP trial produced 6,730,728 bytes with equal pixels. It was
not retained because reusing the existing JPEGs is smaller and avoids recompression.
This is not a new lossy encoding of the PNGs.

| Key | Before: normalized PNG bytes | After: original JPEG bytes | Dimensions |
| --- | ---: | ---: | --- |
| aurora | 1,532,958 | 163,708 | 1024 × 1024 |
| cidade_papel | 2,528,153 | 290,412 | 1400 × 1400 |
| alem_orbita | 665,508 | 64,745 | 565 × 800 |
| sentinela_neon | 1,795,088 | 220,418 | 1024 × 1024 |
| eclipse | 3,549,399 | 378,101 | 1170 × 1790 |
| vigilante | 388,367 | 49,293 | 512 × 640 |
| **Total** | **10,459,473** | **1,166,677** | |

Saved: **9,292,796 bytes (88.85%)** against the pre-hardening PNGs, not against the
original HEAD JPEGs. `cover-assets-metrics.json` records per-file sizes, source paths,
decoded RGB SHA-256 and final encoded-file SHA-256. Original normalized PNGs were backed
up outside the repository before replacement. Every final JPEG also matches its original
HEAD blob byte-for-byte. Pillow verified mode/dimensions/decoded bytes against the PNGs;
this is not a claim of device-side pixel comparison or perceptual user acceptance.

The final debug APK measures 13,768,513 bytes. Its six resource entries total 1,166,677
bytes, and extracting them matches the recorded JPEG SHA-256 hashes. APK contents were
checked, not inferred from source sizes. No screenshot or runtime performance metric
was produced. Visible titles, signatures and credits remain in the exact supplied bytes.

Reproduce final-byte/pixel verification with JDK-independent Python/Pillow tooling:

```python
from pathlib import Path
from io import BytesIO
from PIL import Image
import subprocess

base = "ba1b8e187b0e0237cf21c6273241ff39a4742cb7"
for current in sorted(Path("app/src/main/res/drawable-nodpi").glob("cover_*.jpg")):
    original = subprocess.check_output([
        "git", "show", f"{base}:{current.with_suffix('.png').as_posix()}"
    ])
    assert current.read_bytes() == original
    before, after = Image.open(BytesIO(original)), Image.open(current)
    assert after.format == "JPEG"
    assert (before.mode, before.size, before.tobytes()) == (after.mode, after.size, after.tobytes())
```

## Integration tests added, none removed

`CatalogRepositoryNavigationTest` uses the real ComicsViewModel, real NavHost and an
Activity-owned ViewModelProvider with a fake Repository/AtomicInteger call counter.
The existing debug test manifest provides ComponentActivity; no production/test backend,
Hilt test replacement framework or extra test dependency was added.

Four tests cover:

1. List → selected details → back with exactly one Repository call and the same Content
   instance/catalog IDs; the real ComicsRoute is used.
2. Actual `ActivityScenario.recreate()` with the same retained ViewModel/Content, restored
   selected ID and one Repository call. The empty test host reinstalls the same root using
   the recreated Activity's saved-state registry and retained ViewModelStore.
3. Navigation to nonexistent ID 999 and recovery back to the catalog without another call.
4. A detail ID opened while the initial Repository call is suspended, resolving to the
   selected comic when the catalog completes; no second request.

`ComicCoverPipelineTest` adds seven tests:

- Known local key takes precedence over a URL.
- Unknown key falls back to the remote model.
- Missing data shows placeholder without submitting an image request.
- A simulated local decode failure shows placeholder without attempting the remote URL.
- A simulated remote decode failure shows placeholder.
- Loading and failure labels are distinct.
- A wrapped real BitmapFactoryDecoder decodes a bundled JPEG, asserting it runs off Main
  and samples below the 1024px source width for an 80dp target.

Image source/error tests use a short-circuiting ImageLoader interceptor, so fixture HTTPS
addresses never contact an external server. The decoder test uses a bundled resource.
The injected loader does not replace production security. Existing real TLS/client tests
remain enabled. All prior 25 instrumentation methods and all JVM tests remain present.
These 11 added methods bring app instrumentation to 36; compilation is not execution.

## Model copy investigation

No production or test `.copy(...)` invocation of Comic/ComicItem was found in app/data/domain
sources at this stage. Defaults for shortSynopsis/synopsis are evaluated on construction;
a later copy changing only description would retain old synopsis fields. The actual code
constructs fresh models and never performs that update. This is a future editing-policy
risk, not a current data divergence. **No model/default/DTO/repository behavior was changed**
to fix a hypothetical use case. Existing remote mapping tests still verify absent optional
metadata, description projection and secure thumbnail filtering.

## Actual validation

JDK: Temurin 17.0.21. Existing Gradle 8.2, AGP 8.2.2, Kotlin 1.9.0, Compose compiler/BOM,
Hilt, Retrofit and Coil versions retained. No global Kotlin compiler settings or in-process
workaround added. Workflow SHAs, permissions, cache, tests and artifacts unchanged.

```bash
./gradlew --no-daemon --max-workers=2 clean :app:assembleDebug \
  testDebugUnitTest :domain:test :app:lintDebug :data:lintDebug :component:lintDebug \
  :app:assembleDebugAndroidTest -PCOMICS_BASE_URL=
```

- Intermediate WebP candidate: BUILD SUCCESSFUL in 1m15s; superseded by final resources.
- **Final JPEG command: BUILD SUCCESSFUL in 1m16s**, 171 tasks (163 executed, 8 up-to-date).
- Current-run JUnit XML: **38 tests, zero failures/errors/skips**. Presentation 3,
  ViewModel 7, image client 7, HTTP 7, mapping 6, repository 6, demo 2.
  Domain/component test tasks are NO-SOURCE, not extra passed tests.
- Lint XML: **zero errors/fatal findings**; app 23 warnings (15 legacy resource/manifest,
  8 newer-dependency recommendations), data 2 and component 5 newer-dependency warnings.
  No assertion, suppression, baseline or test was removed to obtain results.
- Debug APK and instrumented test APK generated. Test APK:
  `app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk` (1,053,715 bytes).
- **36 app instrumented methods compiled; zero instrumented tests executed locally.**
  No emulator binary/system image or `/dev/kvm` is installed. The new recreation, image
  threading and downsampling assertions are pending execution, not proven pass results.
- Image metadata/byte/pixel checks and APK extraction checks passed for all six resources.
- `git diff --check`, scoped whitespace/secret-pattern checks and preservation checks
  were executed separately. HTTP/TLS/image restriction JVM suites passed again.
- The URL verification script/configuration is unchanged; it was not rerun for this step.
  Its earlier results belong to the preceding catalog validation, not this new execution.

Temporary setup/caches/logs stay outside versioned files. No new GitHub run was triggered
because no commit or push was made. The original catalog/detail validation record remains
as historical evidence with a pointer to this newer stage.

## Separate pending decisions

- **Licensing:** see [image-licensing.md](image-licensing.md). No redistribution approval is
  asserted by image optimization or passing tests; credits were not removed.
- **Device validation:** run the expanded instrumentation suite, inspect Activity recreation,
  fonts/dark theme, placeholders and actual cover appearance. No new frame/heap metrics,
  TalkBack result, process-death result or visual sign-off is claimed.
- **Runtime limits:** one retained catalog survives configuration changes, not process death;
  startup reload after process death remains expected. ActivityScenario recreation does
  not simulate process death. No database/persistence or new navigation layer was added.

## Changed files and preservation

The complete working-tree diff against HEAD contains 36 paths, including the earlier
catalog implementation, six PNG deletions/six JPEG additions and the hardening below.
Against the pre-hardening review snapshot, all pre-existing JVM/instrumented test files,
domain/data models, demo fixtures, navigation and ViewModel sources remain byte-identical.
Only the requested cover component, cover status string, image files and relevant docs
were changed; two integration test files and three hardening/metrics/licensing docs were added.
The existing catalog validation doc only gained a historical-record pointer.

Hardening paths:

- `app/src/main/java/com/example/js_comics/ui/view/ComicCover.kt`
- `app/src/main/res/values/strings.xml`
- Six `app/src/main/res/drawable-nodpi/cover_*.png` → `cover_*.jpg` resources
- `app/src/androidTest/java/com/example/js_comics/ui/view/CatalogRepositoryNavigationTest.kt`
- `app/src/androidTest/java/com/example/js_comics/ui/view/ComicCoverPipelineTest.kt`
- `README.md`
- `docs/catalog-details-validation.md`
- `docs/cover-assets-metrics.json`
- `docs/cover-hardening-validation.md`
- `docs/image-licensing.md`
