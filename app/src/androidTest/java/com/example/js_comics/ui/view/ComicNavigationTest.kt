package com.example.js_comics.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.runtime.SideEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.espresso.Espresso.pressBack
import androidx.test.platform.app.InstrumentationRegistry
import com.example.js_comics.R
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.js_comics.domain.ComicsFailure
import com.example.js_comics.ui.theme.Js_comicsTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Real NavHost and injected state; no Hilt, repository or remote images. */
@RunWith(AndroidJUnit4::class)
class ComicNavigationTest {
    @get:Rule val compose = createComposeRule()
    private val items = listOf(
        ComicItem(11, "First story", "Short introduction", null, series = "Series A", issueNumber = 3,
            genre = "Science fiction", publicationYear = 2026, synopsis = "The full fictional synopsis.", coverKey = "aurora"),
        ComicItem(42, "Second story", null, null),
    )
    private val state = ComicsUiState.Content(items, true)
    private fun text(id: Int, vararg args: Any) =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(id, *args)

    @Test fun wholeCardCallsBackWithItsStableId() {
        var selected: Int? = null
        compose.setContent { Js_comicsTheme { ComicsListScreen(state, {}, { selected = it }) } }
        compose.onNodeWithTag("comic_card_42").performScrollTo().assertHasClickAction().performClick()
        compose.runOnIdle { assertEquals(42, selected) }
    }

    @Test fun catalogOpensCorrectDetailAndBackButtonReturns() {
        compose.setContent { Js_comicsTheme { ComicsCatalogNavigation(state, {}) } }
        compose.onNodeWithTag("comic_card_42").performScrollTo().performClick()
        compose.onNodeWithTag("details_title").performScrollTo().assertTextEquals("Second story")
        compose.onNodeWithTag("details_back").performClick()
        compose.onNodeWithTag(ComicsScreenTags.CONTENT).assertIsDisplayed()
    }

    @Test fun systemBackReturnsToCatalog() {
        compose.setContent { Js_comicsTheme { ComicsCatalogNavigation(state, {}) } }
        compose.onNodeWithTag("comic_card_11").performScrollTo().performClick()
        compose.onNodeWithTag("comic_details").assertIsDisplayed()
        pressBack()
        compose.onNodeWithTag(ComicsScreenTags.CONTENT).assertIsDisplayed()
    }

    @Test fun savedDestinationRestoresSelectedId() {
        val restoration = StateRestorationTester(compose)
        restoration.setContent { Js_comicsTheme { ComicsCatalogNavigation(state, {}) } }
        compose.onNodeWithTag("comic_card_42").performScrollTo().performClick()
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithTag("details_title").performScrollTo().assertTextEquals("Second story")
    }

    @Test fun detailShowsMetadataAndFullSynopsisInsteadOfShortText() {
        compose.setContent { Js_comicsTheme { ComicDetailsScreen(state, 11, {}, {}) } }
        listOf(text(R.string.comic_series, "Series A"), text(R.string.comic_issue, 3), text(R.string.comic_genre, "Science fiction"), text(R.string.comic_year, 2026)).forEach {
            compose.onNodeWithText(it).performScrollTo().assertIsDisplayed()
        }
        compose.onNodeWithTag("details_synopsis").performScrollTo().assertTextEquals(items.first().synopsis!!)
        compose.onNodeWithText("Short introduction").assertDoesNotExist()
    }

    @Test fun volumeDoesNotInventAnIssue() {
        val item = ComicItem(5, "Volume story", null, null, volumeNumber = 1)
        compose.setContent { Js_comicsTheme { ComicDetailsScreen(ComicsUiState.Content(listOf(item), true), 5, {}, {}) } }
        compose.onNodeWithText(text(R.string.comic_volume, 1)).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText(text(R.string.comic_issue, 0).substringBefore("0"), substring = true).assertDoesNotExist()
    }

    @Test fun incompleteRemoteItemDoesNotInventMetadata() {
        compose.setContent { Js_comicsTheme { ComicDetailsScreen(ComicsUiState.Content(listOf(items.last()), false), 42, {}, {}) } }
        compose.onNodeWithTag("details_title").performScrollTo().assertTextEquals("Second story")
        compose.onNodeWithTag(ComicsScreenTags.REMOTE).assertExists()
        compose.onNodeWithTag("details_synopsis").assertDoesNotExist()
        listOf(text(R.string.comic_series, ""), text(R.string.comic_issue, 0).substringBefore("0"), text(R.string.comic_volume, 0).substringBefore("0"), text(R.string.comic_genre, ""), text(R.string.comic_year, 0).substringBefore("0")).forEach {
            compose.onNodeWithText(it, substring = true).assertDoesNotExist()
        }
        compose.onNodeWithContentDescription(text(R.string.cover_unavailable)).assertExists()
    }

    @Test fun invalidIdHasRecoveryAndBackCallback() {
        var backs = 0
        compose.setContent { Js_comicsTheme { ComicDetailsScreen(state, -1, { backs++ }, {}) } }
        compose.onNodeWithTag("details_unavailable").assertIsDisplayed()
        compose.onNodeWithTag("details_back").assertHeightIsAtLeast(48.dp).performClick()
        compose.runOnIdle { assertEquals(1, backs) }
    }

    @Test fun missingIdHasRecovery() {
        compose.setContent { Js_comicsTheme { ComicDetailsScreen(state, null, {}, {}) } }
        compose.onNodeWithTag("details_unavailable").assertIsDisplayed()
    }

    @Test fun unavailableIdInLoadedCatalogHasRecovery() {
        compose.setContent { Js_comicsTheme { ComicDetailsScreen(state, 99, {}, {}) } }
        compose.onNodeWithTag("details_unavailable").assertIsDisplayed()
    }

    @Test fun malformedNavigationArgumentHasSafeRecovery() {
        lateinit var controller: NavHostController
        compose.setContent {
            val navController = rememberNavController()
            SideEffect { controller = navController }
            Js_comicsTheme { ComicsCatalogNavigation(state, {}, navController) }
        }
        compose.runOnIdle { controller.navigate("comic/not-a-number") }
        compose.onNodeWithTag("details_unavailable").assertIsDisplayed()
    }

    @Test fun detailLoadingWaitsForCatalogAndKeepsBackAvailable() {
        compose.setContent { Js_comicsTheme { ComicDetailsScreen(ComicsUiState.Loading, 11, {}, {}) } }
        compose.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertIsDisplayed()
        compose.onNodeWithTag("details_back").assertIsDisplayed()
        compose.onNodeWithTag("details_unavailable").assertDoesNotExist()
    }

    @Test fun detailEmptyCatalogHasSafeRecovery() {
        compose.setContent { Js_comicsTheme { ComicDetailsScreen(ComicsUiState.Empty(true), 11, {}, {}) } }
        compose.onNodeWithTag("details_unavailable").assertIsDisplayed()
    }

    @Test fun detailErrorKeepsRetryAndBackAvailable() {
        var calls = 0
        compose.setContent { Js_comicsTheme { ComicDetailsScreen(ComicsUiState.Error(ComicsFailure.NETWORK), 11, {}, { calls++ }) } }
        compose.onNodeWithTag(ComicsScreenTags.RETRY).performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1, calls) }
        compose.onNodeWithTag("details_back").assertIsDisplayed()
    }

    @Test fun largeFontDarkDetailCanReachSynopsisOnSmallWidth() {
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 2f)) {
                Js_comicsTheme(useDarkTheme = true) {
                    Box(Modifier.width(320.dp)) { ComicDetailsScreen(state, 11, {}, {}) }
                }
            }
        }
        compose.onNodeWithTag("details_synopsis").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("details_back").assertIsDisplayed()
    }
}
