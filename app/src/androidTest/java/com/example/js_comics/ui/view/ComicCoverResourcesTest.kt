package com.example.js_comics.ui.view

import android.graphics.BitmapFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ComicCoverResourcesTest {
    @Test fun allSixMappedCoversDecodeAsBundledImages() {
        val resources = InstrumentationRegistry.getInstrumentation().targetContext.resources
        val dimensions = listOf("aurora", "cidade_papel", "alem_orbita", "sentinela_neon", "eclipse", "vigilante")
            .associateWith { 1024 to 1536 }
        dimensions.forEach { (key, size) ->
            val bitmap = BitmapFactory.decodeResource(resources, localCoverResource(key)!!)
            assertNotNull("Bundled cover must decode: $key", bitmap)
            try {
                assertEquals(size.first, bitmap.width)
                assertEquals(size.second, bitmap.height)
            } finally { bitmap.recycle() }
        }
    }
}
