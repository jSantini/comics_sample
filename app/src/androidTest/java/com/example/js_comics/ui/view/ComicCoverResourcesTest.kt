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
        val dimensions = mapOf(
            "aurora" to (1024 to 1024), "cidade_papel" to (1400 to 1400),
            "alem_orbita" to (565 to 800), "sentinela_neon" to (1024 to 1024),
            "eclipse" to (1170 to 1790), "vigilante" to (512 to 640),
        )
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
