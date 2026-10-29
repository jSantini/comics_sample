# Image licensing — pending owner verification

Date: 2026-10-29. The developer supplied all six images; no replacement artwork was
created or downloaded during catalog implementation or cover hardening.

| Current asset | Observed credit/title | Rights evidence |
| --- | --- | --- |
| `cover_aurora.jpg` | No authorship established by this engineering review | Not supplied |
| `cover_cidade_papel.jpg` | Embedded “The Clockwork City” and “Dr. Tsvika B.P.” | Not supplied |
| `cover_alem_orbita.jpg` | No authorship established by this engineering review | Not supplied |
| `cover_sentinela_neon.jpg` | No authorship established by this engineering review | Not supplied |
| `cover_eclipse.jpg` | Embedded signature; ownership not established | Not supplied |
| `cover_vigilante.jpg` | No authorship established by this engineering review | Not supplied |

Hardening reuses the original supplied JPEG bytes under correct `.jpg` extensions instead
of storing the intermediate normalized PNGs. Dimensions and every decoded RGB pixel match
the PNGs, including visible signatures/credits. No crop, recoloring, credit removal or
replacement was used to conceal provenance. Correcting an extension does not confer rights.

Before public redistribution of these assets or an APK containing them, the owner must
confirm source, permission/license, attribution requirements and suitability for association
with the supplied fictional comic titles. The city cover visibly names a different work.

No image is declared original, licensed or fit for redistribution by this document. No
legal ownership investigation or infringement determination was performed. This pending
licensing decision is separate from build/test success and from visual acceptance.
