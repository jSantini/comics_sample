package com.example.js_comics.ui.view

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.js_comics.R
import com.example.js_comics.domain.ComicsFailure
import com.example.js_comics.ui.theme.Js_comicsTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Screen-only instrumentation tests: injected states, null image URLs, no ViewModel/Hilt/network. */
@RunWith(AndroidJUnit4::class)
class ComicsListScreenTest {
    @get:Rule val compose = createComposeRule()
    private val items = listOf(ComicItem(1, "Fictional story", "A fictional adventure for a deterministic UI test.", null))
    private fun text(id: Int) = InstrumentationRegistry.getInstrumentation().targetContext.getString(id)

    @Test
    fun loadingExposesProgressAndNoRetry() {
        compose.setContent { Js_comicsTheme { ComicsListScreen(ComicsUiState.Loading, {}) } }
        compose.onNodeWithTag(ComicsScreenTags.LOADING).assertIsDisplayed()
        compose.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertIsDisplayed()
        compose.onNodeWithTag(ComicsScreenTags.RETRY).assertDoesNotExist()
    }

    @Test
    fun contentShowsFictionAndDemoIdentity() {
        compose.setContent { Js_comicsTheme { ComicsListScreen(ComicsUiState.Content(items, true), {}) } }
        compose.onNodeWithTag(ComicsScreenTags.CONTENT).assertIsDisplayed()
        compose.onNodeWithTag(ComicsScreenTags.DEMO).assertIsDisplayed()
        compose.onNodeWithText(items.single().title).assertIsDisplayed()
        compose.onNodeWithText(items.single().description!!).assertIsDisplayed()
        compose.onNodeWithText(text(R.string.catalog_title)).assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
        compose.onNodeWithTag(ComicsScreenTags.ERROR).assertDoesNotExist()
    }

    @Test
    fun remoteContentDoesNotClaimToBeOfflineDemo() {
        compose.setContent { Js_comicsTheme { ComicsListScreen(ComicsUiState.Content(items, false), {}) } }
        compose.onNodeWithTag(ComicsScreenTags.REMOTE).assertIsDisplayed()
        compose.onNodeWithTag(ComicsScreenTags.DEMO).assertDoesNotExist()
    }

    @Test
    fun emptyStateOffersAccessibleRetry() {
        var calls = 0
        compose.setContent { Js_comicsTheme { ComicsListScreen(ComicsUiState.Empty(true), { calls++ }) } }
        compose.onNodeWithTag(ComicsScreenTags.EMPTY).assertIsDisplayed()
        compose.onNodeWithText(text(R.string.empty_title)).assertIsDisplayed()
        compose.onNodeWithTag(ComicsScreenTags.RETRY)
            .assertIsDisplayed().assertIsEnabled().assertHasClickAction().assertHeightIsAtLeast(48.dp).performClick()
        compose.runOnIdle { assertEquals(1, calls) }
    }

    @Test
    fun errorShowsRecoveryMessageAndInvokesRetryOnce() {
        var calls = 0
        compose.setContent { Js_comicsTheme { ComicsListScreen(ComicsUiState.Error(ComicsFailure.NETWORK), { calls++ }) } }
        compose.onNodeWithTag(ComicsScreenTags.ERROR).assertIsDisplayed()
        compose.onNodeWithText(text(R.string.error_network)).assertIsDisplayed()
        compose.onNodeWithTag(ComicsScreenTags.RETRY).assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals(1, calls) }
    }

    @Test
    fun retryCanTransitionFromErrorThroughLoadingToContent() {
        var state by mutableStateOf<ComicsUiState>(ComicsUiState.Error(ComicsFailure.TIMEOUT))
        compose.setContent { Js_comicsTheme { ComicsListScreen(state, { state = ComicsUiState.Loading }) } }
        compose.onNodeWithTag(ComicsScreenTags.RETRY).performClick()
        compose.onNodeWithTag(ComicsScreenTags.LOADING).assertIsDisplayed()
        compose.onNodeWithTag(ComicsScreenTags.ERROR).assertDoesNotExist()
        compose.runOnIdle { state = ComicsUiState.Content(items, true) }
        compose.onNodeWithTag(ComicsScreenTags.CONTENT).assertIsDisplayed()
        compose.onNodeWithText(items.single().title).assertIsDisplayed()
    }

    @Test
    fun largeFontDarkContentRemainsScrollable() {
        val longItems = (1..8).map { ComicItem(it, "Fictional story $it with a long title", "A long fictional description that wraps without a fixed card height.", null) }
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = 2f)) {
                Js_comicsTheme(useDarkTheme = true) { ComicsListScreen(ComicsUiState.Content(longItems, true), {}) }
            }
        }
        compose.onNodeWithTag(ComicsScreenTags.CONTENT).performScrollToNode(hasText(longItems.last().title))
        compose.onNodeWithText(longItems.last().title).assertIsDisplayed()
    }

    @Test
    fun largeFontDarkErrorKeepsRetryReachable() {
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = 2f)) {
                Js_comicsTheme(useDarkTheme = true) { ComicsListScreen(ComicsUiState.Error(ComicsFailure.INVALID_DATA), {}) }
            }
        }
        compose.onNodeWithTag(ComicsScreenTags.RETRY).performScrollTo().assertIsDisplayed().assertIsEnabled().assertHeightIsAtLeast(48.dp)
    }
}
