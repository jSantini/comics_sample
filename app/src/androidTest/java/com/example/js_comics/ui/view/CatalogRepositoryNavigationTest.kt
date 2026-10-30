package com.example.js_comics.ui.view

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.js_comics.domain.*
import com.example.js_comics.ui.theme.Js_comicsTheme
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicInteger

/** Activity-owned real ViewModel + real NavHost + fake repository, without Hilt/network. */
@RunWith(AndroidJUnit4::class)
class CatalogRepositoryNavigationTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val catalog = ComicsCatalog(listOf(
        Comic(11, "First fixture", "First synopsis", null),
        Comic(42, "Selected fixture", "Full selected synopsis", null),
    ), CatalogSource.DEMO)
    private lateinit var controller: NavHostController

    private class CountingRepository(
        val catalog: ComicsCatalog,
        val gate: CompletableDeferred<ComicsCatalog>? = null,
    ) : ComicsRepository {
        val calls = AtomicInteger()
        override suspend fun getComics(): ComicsCatalog {
            calls.incrementAndGet()
            return gate?.await() ?: catalog
        }
    }

    private fun installRoot(repository: CountingRepository, exposeNavigation: Boolean = false): ComicsViewModel {
        lateinit var model: ComicsViewModel
        compose.activityRule.scenario.onActivity { activity ->
            model = ViewModelProvider(activity, object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    require(modelClass == ComicsViewModel::class.java)
                    @Suppress("UNCHECKED_CAST")
                    return ComicsViewModel(repository) as T
                }
            })[ComicsViewModel::class.java]
            // Reinstall the same root after Activity recreation. The Activity's saved-state
            // registry and retained ViewModelStore, not a new fake Content, restore the UI.
            activity.setContent {
                Js_comicsTheme {
                    if (exposeNavigation) {
                        val state by model.state.collectAsStateWithLifecycle()
                        val nav = rememberNavController()
                        SideEffect { controller = nav }
                        ComicsCatalogNavigation(state, model::retry, nav)
                    } else ComicsRoute(model)
                }
            }
        }
        return model
    }

    @Test fun listDetailsAndBackKeepTheSameCatalogAndOneRepositoryRequest() {
        val repository = CountingRepository(catalog)
        val model = installRoot(repository)
        compose.onNodeWithTag("comic_card_42").performScrollTo().performClick()
        compose.onNodeWithTag("details_title").performScrollTo().assertTextEquals("Selected fixture")
        val loaded = model.state.value
        compose.runOnIdle { assertEquals(1, repository.calls.get()) }
        compose.onNodeWithTag("details_back").performClick()
        compose.onNodeWithTag(ComicsScreenTags.CONTENT).assertIsDisplayed()
        compose.onNodeWithTag("comic_card_11").performScrollTo().assertExists()
        compose.runOnIdle {
            assertEquals(1, repository.calls.get())
            assertSame(loaded, model.state.value)
            assertEquals(listOf(11, 42), (model.state.value as ComicsUiState.Content).items.map { it.id })
        }
    }

    @Test fun recreatingActivityRetainsViewModelCatalogAndSelectedNavigationId() {
        val repository = CountingRepository(catalog)
        val before = installRoot(repository)
        compose.onNodeWithTag("comic_card_42").performScrollTo().performClick()
        compose.onNodeWithTag("details_title").performScrollTo().assertTextEquals("Selected fixture")
        val loaded = before.state.value
        compose.activityRule.scenario.recreate()
        val after = installRoot(repository)
        compose.onNodeWithTag("details_title").performScrollTo().assertTextEquals("Selected fixture")
        compose.runOnIdle {
            assertSame(before, after)
            assertSame(loaded, after.state.value)
            assertEquals(1, repository.calls.get())
        }
        compose.onNodeWithTag("details_back").performClick()
        compose.onNodeWithTag(ComicsScreenTags.CONTENT).assertIsDisplayed()
    }

    @Test fun nonexistentNavigationIdRecoversWithoutReloadingTheRepository() {
        val repository = CountingRepository(catalog)
        val model = installRoot(repository, exposeNavigation = true)
        compose.onNodeWithTag(ComicsScreenTags.CONTENT).assertIsDisplayed()
        val loaded = model.state.value
        compose.runOnIdle { controller.navigate("comic/999") }
        compose.onNodeWithTag("details_unavailable").assertIsDisplayed()
        compose.onNodeWithTag("details_back").performClick()
        compose.onNodeWithTag(ComicsScreenTags.CONTENT).assertIsDisplayed()
        compose.runOnIdle {
            assertEquals(1, repository.calls.get())
            assertSame(loaded, model.state.value)
        }
    }

    @Test fun detailIdResolvesWhenTheInitialCatalogFinishesLoading() {
        val gate = CompletableDeferred<ComicsCatalog>()
        val repository = CountingRepository(catalog, gate)
        installRoot(repository, exposeNavigation = true)
        compose.onNodeWithTag(ComicsScreenTags.LOADING).assertIsDisplayed()
        compose.runOnIdle { controller.navigate("comic/42") }
        compose.onNodeWithTag("comic_details").assertIsDisplayed()
        compose.onNodeWithTag("details_unavailable").assertDoesNotExist()
        gate.complete(catalog)
        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithTag("details_title").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithTag("details_title").performScrollTo().assertTextEquals("Selected fixture")
        compose.runOnIdle { assertEquals(1, repository.calls.get()) }
    }
}
