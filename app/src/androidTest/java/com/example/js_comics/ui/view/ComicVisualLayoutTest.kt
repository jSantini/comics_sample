package com.example.js_comics.ui.view

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import coil.ImageLoader
import coil.decode.DataSource
import coil.intercept.Interceptor
import coil.request.SuccessResult
import com.example.js_comics.R
import com.example.js_comics.ui.theme.Js_comicsTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ComicVisualLayoutTest {
    @get:Rule val compose = createComposeRule()
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val item = ComicItem(1, "Aurora: O Primeiro Sinal", "Resumo de uma história fictícia. ".repeat(30), null,
        issueNumber = 1, genre = "Ficção científica", series = "Aurora", publicationYear = 2026,
        synopsis = "Sinopse completa da história fictícia.", coverKey = "aurora")

    @Test fun portraitCatalogCoverPreservesTwoToThreeBoundsWithoutCropping() {
        compose.setContent { Js_comicsTheme { ComicsListScreen(ComicsUiState.Content(listOf(item), true), {}) } }
        val bounds = compose.onNodeWithTag("comic_cover_1", useUnmergedTree = true).getUnclippedBoundsInRoot()
        assertEquals((bounds.right.value - bounds.left.value) * 1.5f, bounds.bottom.value - bounds.top.value, 1f)
    }

    @Test fun largeTextCatalogKeepsSummaryToThreeLinesBesideCover() {
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 2f)) {
                Js_comicsTheme(useDarkTheme = true) {
                    Box(Modifier.width(320.dp)) { ComicsListScreen(ComicsUiState.Content(listOf(item), true), {}) }
                }
            }
        }
        val summary = compose.onNodeWithTag("comic_summary_1", useUnmergedTree = true)
        compose.onNodeWithTag(ComicsScreenTags.CONTENT).performScrollToNode(hasTestTag("comic_card_1"))
        summary.performScrollTo().assertIsDisplayed()
        val layouts = mutableListOf<TextLayoutResult>()
        summary.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
        assertEquals(3, layouts.single().lineCount)
        assertTrue(layouts.single().isLineEllipsized(2))
        val cover = compose.onNodeWithTag("comic_cover_1", useUnmergedTree = true).getUnclippedBoundsInRoot()
        assertTrue(summary.getUnclippedBoundsInRoot().left >= cover.right)
        compose.onNodeWithTag("comic_card_1").assertHasClickAction()
    }

    @Test fun detailsLimitCoverHeightAndPlaceMetadataBeforeImage() {
        compose.setContent { Js_comicsTheme { ComicDetailsScreen(ComicsUiState.Content(listOf(item), true), 1, {}, {}) } }
        val cover = compose.onNodeWithTag("comic_cover_1").performScrollTo().getUnclippedBoundsInRoot()
        assertTrue("Detail cover must fit within 200dp", cover.bottom.value - cover.top.value <= 200.5f)
        val year = compose.onNodeWithText(context.getString(R.string.comic_year, 2026)).getUnclippedBoundsInRoot()
        val title = compose.onNodeWithTag("details_title").getUnclippedBoundsInRoot()
        assertTrue(title.top < year.top)
        assertTrue(year.bottom <= cover.top)
    }

    @Test fun detailToolbarHasPortugueseBackButtonAtTheStart() {
        compose.setContent { Js_comicsTheme { ComicDetailsScreen(ComicsUiState.Content(listOf(item), true), 1, {}, {}) } }
        val back = compose.onNodeWithTag("details_back").assertHasClickAction()
            .assertHeightIsAtLeast(48.dp).assertWidthIsAtLeast(48.dp)
        compose.onNodeWithContentDescription(context.getString(R.string.back_to_catalog)).assertIsDisplayed()
        val button = back.getUnclippedBoundsInRoot()
        val bar = compose.onNodeWithTag("details_app_bar").getUnclippedBoundsInRoot()
        assertTrue(button.left + button.right < bar.left + bar.right)
        compose.onNodeWithText(context.getString(R.string.details_screen_title)).assertIsDisplayed()
    }

    @Test fun largeFontBackTargetAcceptsTouchOutsideTheVisualIcon() {
        var backs = 0
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 2f)) {
                Js_comicsTheme(useDarkTheme = true) {
                    Box(Modifier.width(320.dp)) {
                        ComicDetailsScreen(ComicsUiState.Content(listOf(item), true), 1, { backs++ }, {})
                    }
                }
            }
        }
        compose.onNodeWithTag("details_back").assertIsDisplayed().assertHasClickAction()
            .assertHeightIsAtLeast(48.dp).assertWidthIsAtLeast(48.dp)
            // The centered 24dp icon starts at x=12dp inside the 48dp target.
            // This tap exercises the actual button hit area, not a semantic click.
            .performTouchInput { click(Offset(3.dp.toPx(), 24.dp.toPx())) }
        compose.runOnIdle { assertEquals(1, backs) }
        compose.onNodeWithContentDescription(context.getString(R.string.back_to_catalog)).assertIsDisplayed()
    }

    @Test fun remoteLandscapeCoverAdoptsItsRatioAndStaysWithinDetailBounds() {
        val bitmap = Bitmap.createBitmap(200, 100, Bitmap.Config.ARGB_8888)
        val loader = ImageLoader.Builder(context).components {
            add(object : Interceptor {
                override suspend fun intercept(chain: Interceptor.Chain) =
                    SuccessResult(BitmapDrawable(context.resources, bitmap), chain.request, DataSource.MEMORY)
            })
        }.build()
        try {
            compose.setContent {
                Js_comicsTheme {
                    ComicCover(ComicItem(42, "Remoto", null, "https://images.example.test/fixture"),
                        Modifier.widthIn(max = 280.dp).heightIn(max = 200.dp), loader)
                }
            }
            compose.waitUntil {
                val bounds = compose.onNodeWithTag("comic_cover_42").getUnclippedBoundsInRoot()
                kotlin.math.abs((bounds.right.value - bounds.left.value) / (bounds.bottom.value - bounds.top.value) - 2f) < 0.02f
            }
            val bounds = compose.onNodeWithTag("comic_cover_42").getUnclippedBoundsInRoot()
            assertTrue("Remote cover must fit within 200dp", bounds.bottom.value - bounds.top.value <= 200.5f)
        } finally { loader.shutdown() }
    }
}
