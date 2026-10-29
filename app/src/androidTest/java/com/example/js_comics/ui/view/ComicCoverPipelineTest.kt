package com.example.js_comics.ui.view

import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.os.Looper
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import coil.ImageLoader
import coil.decode.BitmapFactoryDecoder
import coil.decode.DataSource
import coil.decode.DecodeResult
import coil.decode.Decoder
import coil.fetch.SourceResult
import coil.intercept.Interceptor
import coil.request.ErrorResult
import coil.request.ImageResult
import coil.request.Options
import coil.request.SuccessResult
import com.example.js_comics.R
import com.example.js_comics.ui.theme.Js_comicsTheme
import kotlinx.coroutines.CompletableDeferred
import org.junit.After
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/** Intercepted fixtures never contact a server; one test decodes an actual bundled JPEG. */
@RunWith(AndroidJUnit4::class)
class ComicCoverPipelineTest {
    @get:Rule val compose = createComposeRule()
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val loaders = mutableListOf<ImageLoader>()
    private val observedData = CopyOnWriteArrayList<Any>()
    private val remote = "https://images.example.test/fixture.webp"

    @After fun closeImageLoaders() { loaders.forEach { it.shutdown() } }

    private fun loader(fail: Boolean = false, gate: CompletableDeferred<Unit>? = null): ImageLoader =
        ImageLoader.Builder(context).components {
            add(object : Interceptor {
                override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
                    observedData.add(chain.request.data)
                    gate?.await()
                    return if (fail) ErrorResult(null, chain.request, IOException("Synthetic decode failure"))
                    else SuccessResult(ColorDrawable(Color.BLUE), chain.request, DataSource.MEMORY)
                }
            })
        }.build().also { loaders.add(it) }

    private fun show(item: ComicItem, imageLoader: ImageLoader) {
        compose.setContent { Js_comicsTheme { ComicCover(item, Modifier.width(80.dp), imageLoader) } }
    }

    @Test fun knownLocalCoverTakesPrecedenceOverRemoteUrl() {
        show(ComicItem(1, "Fixture", null, remote, coverKey = "aurora"), loader())
        compose.waitUntil { observedData.isNotEmpty() }
        compose.runOnIdle { assertEquals(listOf(R.drawable.cover_aurora), observedData.toList()) }
    }

    @Test fun unknownLocalKeyFallsBackToTheRemoteModel() {
        show(ComicItem(1, "Fixture", null, remote, coverKey = "missing"), loader())
        compose.waitUntil { observedData.isNotEmpty() }
        compose.runOnIdle { assertEquals(listOf(remote), observedData.toList()) }
    }

    @Test fun absentCoverShowsPlaceholderWithoutSubmittingARequest() {
        show(ComicItem(1, "Fixture", null, null, coverKey = "missing"), loader())
        compose.onNodeWithContentDescription("Cover unavailable").assertIsDisplayed()
        compose.runOnIdle { assertTrue(observedData.isEmpty()) }
    }

    @Test fun failedLocalDecodeShowsPlaceholderWithoutTryingTheRemoteUrl() {
        show(ComicItem(1, "Fixture", null, remote, coverKey = "aurora"), loader(fail = true))
        compose.waitUntil { compose.onAllNodesWithContentDescription("Cover unavailable").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithContentDescription("Cover unavailable").assertIsDisplayed()
        compose.runOnIdle { assertEquals(listOf(R.drawable.cover_aurora), observedData.toList()) }
    }

    @Test fun failedRemoteDecodeShowsAccessiblePlaceholder() {
        show(ComicItem(1, "Fixture", null, remote), loader(fail = true))
        compose.waitUntil { compose.onAllNodesWithContentDescription("Cover unavailable").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithContentDescription("Cover unavailable").assertIsDisplayed()
    }

    @Test fun loadingAndFailureHaveDistinctAccessibleLabels() {
        val gate = CompletableDeferred<Unit>()
        show(ComicItem(1, "Fixture", null, remote), loader(fail = true, gate = gate))
        compose.onNodeWithContentDescription("Loading cover").assertIsDisplayed()
        gate.complete(Unit)
        compose.waitUntil { compose.onAllNodesWithContentDescription("Cover unavailable").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithContentDescription("Loading cover").assertDoesNotExist()
    }

    @Test fun bundledBitmapDecoderRunsOffMainAndSamplesForTheCoverConstraints() {
        val decodedOnMain = AtomicBoolean(true)
        val width = AtomicInteger()
        val imageLoader = ImageLoader.Builder(context).allowHardware(false).memoryCache(null).components {
            add(object : Decoder.Factory {
                override fun create(result: SourceResult, options: Options, imageLoader: ImageLoader): Decoder {
                    val delegate = BitmapFactoryDecoder.Factory().create(result, options, imageLoader)
                    return object : Decoder {
                        override suspend fun decode(): DecodeResult? {
                            decodedOnMain.set(Looper.myLooper() == Looper.getMainLooper())
                            return delegate.decode()?.also {
                                width.set((it.drawable as BitmapDrawable).bitmap.width)
                            }
                        }
                    }
                }
            })
        }.build().also { loaders.add(it) }
        show(ComicItem(1, "Fixture", null, null, coverKey = "aurora"), imageLoader)
        compose.waitUntil(timeoutMillis = 10_000) { width.get() > 0 }
        compose.runOnIdle {
            assertFalse("Bitmap decoder must not execute on the main thread", decodedOnMain.get())
            assertTrue("80dp cover should not decode the full 1024px source", width.get() < 1024)
        }
    }
}
