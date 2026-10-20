package com.example.js_comics.data

import com.example.js_comics.data.remote.response.*
import com.example.js_comics.domain.ComicsException
import com.example.js_comics.domain.ComicsFailure
import org.junit.Assert.*
import org.junit.Test

class ComicsMappingTest {
    private fun response(vararg comics: ComicDto?) = ComicsResponseDto(ComicsDataDto(comics.toList()))

    @Test fun mapsValidFieldsAndTrimsText() {
        val comic = response(ComicDto(7, " Title ", " Story ", ThumbnailDto("https://images.example.test/cover", "jpg"))).toComics().single()
        assertEquals(7, comic.id)
        assertEquals("Title", comic.title)
        assertEquals("Story", comic.description)
        assertEquals("https://images.example.test/cover.jpg", comic.imageUrl)
    }
    @Test fun emptyCatalogIsValid() { assertTrue(response().toComics().isEmpty()) }
    @Test fun absentOptionalFieldsAreValid() {
        val comic = response(ComicDto(1, "Title", "  ")).toComics().single()
        assertNull(comic.description)
        assertNull(comic.imageUrl)
    }
    @Test fun rejectsMissingEnvelopeAndRequiredFields() {
        val invalid = listOf(ComicsResponseDto(), ComicsResponseDto(ComicsDataDto()), response(null), response(ComicDto(null, "Title")), response(ComicDto(1, " ")), response(ComicDto(0, "Title")))
        invalid.forEach { assertEquals(ComicsFailure.INVALID_DATA, assertThrows(ComicsException::class.java) { it.toComics() }.failure) }
    }
    @Test fun rejectsDuplicateIds() {
        assertThrows(ComicsException::class.java) { response(ComicDto(1, "One"), ComicDto(1, "Two")).toComics() }
    }
    @Test fun dropsInsecureOrCredentialBearingImageUrls() {
        listOf("http://images.example.test/a", "https://user:password@images.example.test/a", "https://images.example.test/a?token=fixture", "not a url").forEach {
            assertNull(response(ComicDto(1, "Title", thumbnail = ThumbnailDto(it, "jpg"))).toComics().single().imageUrl)
        }
    }
}
