package com.example.js_comics

import com.example.js_comics.domain.Comic
import com.example.js_comics.ui.view.*
import org.junit.Assert.*
import org.junit.Test

class ComicPresentationTest {
    @Test fun localCoverKeysResolveOnlyToExplicitAndroidResources() {
        val expected = mapOf(
            "aurora" to R.drawable.cover_aurora,
            "cidade_papel" to R.drawable.cover_cidade_papel,
            "alem_orbita" to R.drawable.cover_alem_orbita,
            "sentinela_neon" to R.drawable.cover_sentinela_neon,
            "eclipse" to R.drawable.cover_eclipse,
            "vigilante" to R.drawable.cover_vigilante,
        )
        expected.forEach { (key, resource) -> assertEquals(resource, localCoverResource(key)) }
        assertNull(localCoverResource(null))
        assertNull(localCoverResource("unknown"))
        assertNull(localCoverResource("https://untrusted.example/cover"))
    }

    @Test fun suppliedCoverRatiosMatchOriginalDimensions() {
        val expected = mapOf("aurora" to 1f, "cidade_papel" to 1f, "sentinela_neon" to 1f,
            "alem_orbita" to (565f / 800f), "eclipse" to (1170f / 1790f), "vigilante" to (512f / 640f))
        expected.forEach { (key, ratio) -> assertEquals(ratio, localCoverAspectRatio(key)!!, 0.0001f) }
        assertNull(localCoverAspectRatio(null))
        assertNull(localCoverAspectRatio("unknown"))
    }

    @Test fun remoteRatiosRequirePositiveFiniteDimensions() {
        assertEquals(2f, coverAspectRatio(200f, 100f)!!, 0.0001f)
        listOf(0f, -1f, Float.NaN, Float.POSITIVE_INFINITY).forEach {
            assertNull(coverAspectRatio(it, 100f))
            assertNull(coverAspectRatio(100f, it))
        }
    }

    @Test fun presentationPreservesAllDomainMetadataAndText() {
        val item = Comic(5, "Title", "Legacy", null, "Series", null, 1,
            "Genre", 2026, "Short", "Full synopsis", "eclipse").toComicItem()
        assertEquals(ComicItem(5, "Title", "Legacy", null, "Series", null, 1,
            "Genre", 2026, "Short", "Full synopsis", "eclipse"), item)
    }

    @Test fun selectionUsesStableIdsAndNeverInventsMissingComics() {
        val item = ComicItem(42, "Remote", null, null)
        val state = ComicsUiState.Content(listOf(item), false)
        assertSame(item, state.comicWithId(42))
        listOf(null, -1, 0, 1).forEach { assertNull(state.comicWithId(it)) }
        assertNull(ComicsUiState.Loading.comicWithId(42))
        assertNull(ComicsUiState.Empty(false).comicWithId(42))
    }
}
