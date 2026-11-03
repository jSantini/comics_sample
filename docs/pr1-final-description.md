## Summary

Modernize the Android Comics Showcase for a small, demonstrable portfolio project:

- Modular Kotlin :app, :domain, :data and :component architecture; separate transport DTOs,
  domain models and UI items; Compose MVVM, read-only StateFlow, structured coroutines,
  Hilt, Retrofit/OkHttp and Navigation Compose.
- Offline is the startup default. Online loads the public Railway API; source selection,
  loading/content/empty/error states and active-source retry are explicit. Source changes
  cancel old loads and clear old details; failures never silently display offline content.
- Six fictional Portuguese comics with complete metadata and synopses, stable IDs and
  correct issue/volume distinction. Eclipse is volume 1, not issue 1.
- Six owner-supplied AI-generated replacement WebPs with documented provenance/hashes,
  local-first Coil rendering, sampled decoding and secure unknown-cover fallback.
- Portuguese accessible cards/details, light/dark themes and four real Windows emulator
  screenshots. Screenshots are Offline captures predating the source selector, not Online evidence.

## Backend

Repository: https://github.com/jSantini/comics-showcase-api

Public API: https://comics-showcase-api-production.up.railway.app

`GET /api/comics` uses the `data.results` envelope. Maps id, title, series, issueNumber,
volumeNumber, genre, publicationYear, shortSynopsis, synopsis, coverKey and description.
COMICS_BASE_URL defaults to the public HTTPS Railway prefix; an explicitly empty value
supports an offline-only build. Known cover keys use bundled artwork in both modes.
Health and the six-comic catalog were checked again on 2026-11-03.

## Validation

Code HEAD reviewed: `d2027b0d935c0e10e5a5d9e14328261601a01c1d`.

- Previous local checks reverified from retained evidence: **47 JVM tests** (23 app + 24
  data), zero failures/errors/skips; **20 URL checks** (8 valid round-trips, 12 rejected
  inputs); clean offline-only build and app/instrumented APK compilation passed.
- Previous local lint: **0 errors, 34 warnings** (app 25, component 5, data 4).
- Independently checked GitHub [run 41658203476](https://github.com/jSantini/comics_sample/actions/runs/41658203476)
  for d2027b0: both jobs completed **success**.
  [Verify](https://github.com/jSantini/comics_sample/actions/runs/41658203476/job/125994107467)
  confirms URL checks and a clean successful build;
  [Compose](https://github.com/jSantini/comics_sample/actions/runs/41658203476/job/125995709199)
  logs show **43 instrumented tests** and a successful connected-test task.
- Separately, the owner reports **43 instrumented tests passed on Windows** and visually
  verified the live Online catalog. These Windows/visual checks were not rerun in this review.
- Backend automated tests previously passed according to the owner; not rerun here.
- Current review: ancestry/diff inventory, sanitized security/history review, cover hashes,
  screenshot inspection, public API checks and both integration-range whitespace checks.

Earlier local ADB timeouts remain documented as historical results, not the current overall
instrumented status. No coverage percentage or new CI execution is invented. Documentation-only
follow-ups may be skipped by workflow paths-ignore; code validation remains tied to d2027b0.

## Security

No Marvel credentials, Marvel API usage, fixed signature assignments, sensitive HTTP logging,
tracked .env/local.properties/keystores, or token/private-key signatures were found in the
reviewed current tree. The two historical identifier/signature literals are absent from all
current tracked text. Pattern scanning is not an exhaustive proof of absence.

HTTPS validation, Java-literal escaping, normal TLS trust, no catalog/image redirects,
cleartext rejection and restricted Coil URLs remain intact. BuildConfig contains only the
non-secret public catalog URL. gradle.properties is unchanged against both target branches.

## Historical considerations

Current-tree cleanup is **not historical removal**:

- Root/master commit `1f3ab8a144b1f3e68ebb381bae8b8b1a38ffcb34` remains reachable and contains
  a public Marvel identifier, fixed signing parameters, the old endpoint and logging setup.
  No values are reproduced. Provider-side activity/revocation is **unknown**; the owner must
  verify and revoke/rotate as applicable. No historical credential was tested or revoked here.
- Previous artwork remains reachable in Phase One/history. Current WebP hashes and the four
  screenshots were checked; generation provenance is owner-reported, not independent rights clearance.
- Artifact **12897231501**, `offline-demo-apks`, from
  [run 40873498892](https://github.com/jSantini/comics_sample/actions/runs/40873498892) is still
  listed and **not expired** as of this review, with expiry **2026-11-05T12:30:17Z**. Treat the
  APK from its old Phase One commit as potentially containing earlier artwork. Its ZIP was
  not downloaded/opened; no deletion is authorized. Expiry does not erase Git history or copies.
- Release assessment: **CONDITIONAL** on owner confirmation/handling of historical provider
  configuration and earlier-artwork exposure. A passing CI run is not merge or rights approval.

Sanitized [security audit](https://github.com/jSantini/comics_sample/blob/feature/offline-comics-details/docs/security-history-audit.md)
and [final integration plan](https://github.com/jSantini/comics_sample/blob/feature/offline-comics-details/docs/final-integration-review.md)
record the reviewed snapshot. Their documentation-only publication was separately approved;
that approval does not authorize merging.
Existing provenance: [image-licensing.md](https://github.com/jSantini/comics_sample/blob/feature/offline-comics-details/docs/image-licensing.md).

## Scope

No backend database, login, pagination, favorites or unrelated features. Offline works
independently of Railway; Online requires connectivity and backend availability. No persistent
remote cache or source persistence across process death. No dependency/toolchain upgrade.

Keep this PR **Draft**, targeting **modernization/phase-one**. At this snapshot the feature
is 13 commits ahead / 0 behind Phase One; Phase One is 8 ahead / 0 behind master. PR #1
contains 54 changed files; the complete future master diff contains 82 before review docs.
Preserve all commits with a normal merge commit after explicit later approval. Only after
stage one completes should a separate Phase One -> master PR be created and validated.
No merge, Ready transition, base retargeting, branch/artifact deletion or history rewrite
was performed by this review. Human ownership, review and final approval remain with the maintainer.
