# Current-tree and history security audit

Reviewed: 2026-11-03. Android HEAD: `d2027b0d935c0e10e5a5d9e14328261601a01c1d`.

The task referenced an earlier `docs/security-history-audit.md`, but no such file was
found in the working tree or the history reachable from fetched branches. This is a
new, sanitized audit; it does not claim to reproduce an unavailable earlier report.

## A. Current feature tree

No hardcoded Marvel credentials, Marvel API endpoint, fixed signing assignments,
HTTP logging interceptor dependency, token/private-key signatures or secret assignments
were found in the reviewed current source/configuration. All tracked text was also
checked for the two historical hexadecimal credential/signature literals without printing
their values; neither occurs in the current tree. This is a scoped pattern scan and code
review, not a guarantee that every possible secret representation has been detected.

No `.env`, `local.properties`, keystore, PEM or PKCS#12 credential file is tracked.
The workspace `local.properties` exists only as an ignored local SDK configuration.
`gradle.properties` is byte-identical across master, Phase One and the feature.
The only application-defined BuildConfig field is the non-secret public COMICS_BASE_URL.

Transport review confirms: validated HTTPS base URL; no userinfo/query/fragment;
Java-literal escaping; catalog/image timeouts; both redirect settings disabled; normal
platform TLS trust; cleartext traffic disabled in the manifest; HTTPS-only Coil transport
rejecting userinfo/query/fragment. The six local artwork keys resolve only to an explicit
resource allowlist. No transport regression was found. Existing limitations (no remote
host allowlist, private-IP filtering or certificate pinning) remain documented.

## B. Reachable historical content

Root/master commit: `1f3ab8a144b1f3e68ebb381bae8b8b1a38ffcb34`.
This commit is an ancestor of all three branches and remains publicly reachable.

Sanitized evidence:

- `data/src/main/java/com/example/js_comics/data/repository/ComicsRepository.kt`
  at that commit contains a public Marvel identifier and fixed signing parameters.
- `data/src/main/java/com/example/js_comics/data/di/NetworkModule.kt`
  at that commit contains the Marvel endpoint and historical logging configuration.
- `data/build.gradle.kts` at that commit contains the former logging dependency.

No credential values, historical request URLs or patches are reproduced here.
Current-tree removal did not erase those objects. No private signing key was established
by this review. No historical credential was tested, revoked or changed, and no history
rewriting, artifact deletion or GitHub security-setting change was performed.

## C. Provider status: unknown

Whether the historical identifier/signing configuration is still active at the provider
has not been established. The owner must inspect their provider account and confirm
revocation/rotation or deactivation as applicable, without publishing credential values.
If already inactive, record that fact and its verification date. Do not treat an old
public identifier or a fixed signature as proof of an exposed private key, nor treat
removal from source as proof of provider-side revocation.

## Historical artwork and Actions artifact

The current drawable resources contain only the six replacement cover WebPs. Their
SHA-256 hashes match `docs/cover-assets-metrics.json`; the four current README screenshots
show only replacement artwork. AI generation is owner-reported provenance, not independently
verified exclusivity or rights clearance. See `image-licensing.md`.

The six earlier cover PNGs remain reachable at Phase One commit
`ba1b8e187b0e0237cf21c6273241ff39a4742cb7` and therefore in the feature ancestry.
No historical images were opened or redistributed during this audit.

GitHub metadata checked on 2026-11-03 lists artifact **12897231501** (`offline-demo-apks`)
for run **40873498892**, built from that Phase One commit. It is **not expired**, is
13,221,935 bytes and has reported expiry **2026-11-05T12:30:17Z**
(**2026-11-05 09:30:17 America/Sao_Paulo**). It still has an archive-download endpoint.
Its ZIP was deliberately not downloaded: current metadata confirms it remains listed
and eligible for download, but archive contents/download were not independently checked.
Given its build commit, treat it as potentially redistributing the earlier artwork.
No deletion is authorized by this task. Natural artifact expiry does not remove Git objects,
clones, previously downloaded APKs or copies.

## Release condition and minimum owner actions

**CONDITIONAL**, with no current-code secret blocker found:

1. Confirm provider-side status and revoke/rotate the historical configuration if applicable.
2. Resolve or explicitly approve the documented handling of earlier artwork exposure.
   Avoid promoting old APKs. Decide separately whether to authorize deletion of artifact
   12897231501 or accept its scheduled expiry; neither action sanitizes Git history.
3. Keep current-tree and historical-exposure statements distinct in the PR/README.
4. Approve any merge separately. If historical-object removal is ultimately required,
   plan it as separately authorized work; it is outside this review.

No automatic remediation, legal clearance, provider credential validity or complete
historical sanitization is claimed.
