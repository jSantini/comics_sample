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
        assertEquals("Story", comic.synopsis)
        assertEquals("Story", comic.shortSynopsis)
        assertEquals("https://images.example.test/cover.jpg", comic.imageUrl)
    }
    @Test fun emptyCatalogIsValid() { assertTrue(response().toComics().isEmpty()) }
    @Test fun absentOptionalFieldsAreValid() {
        val comic = response(ComicDto(1, "Title", "  ")).toComics().single()
        assertNull(comic.description)
        assertNull(comic.imageUrl)
        assertNull(comic.series)
        assertNull(comic.issueNumber)
        assertNull(comic.volumeNumber)
        assertNull(comic.genre)
        assertNull(comic.publicationYear)
        assertNull(comic.coverKey)
        assertNull(comic.synopsis)
        assertNull(comic.shortSynopsis)
    }
    @Test fun rejectsMissingEnvelopeAndRequiredFields() {
        val invalid = listOf(ComicsResponseDto(), ComicsResponseDto(ComicsDataDto()), response(null), response(ComicDto(null, "Title")), response(ComicDto(1, " ")), response(ComicDto(0, "Title")))
        invalid.forEach { assertEquals(ComicsFailure.INVALID_DATA, assertThrows(ComicsException::class.java) { it.toComics() }.failure) }
    }
    @Test fun rejectsDuplicateIds() {
        assertThrows(ComicsException::class.java) { response(ComicDto(1, "One"), ComicDto(1, "Two")).toComics() }
    }
    @Test fun dropsInsecureOrCredentialBearingImageUrls() {
        listOf("http://images.example.test/a", "https://user:password@images.example.test/a", "https://images.example.test/a?token=fixture", "https://images.example.test/a#fragment", "file:///tmp/cover", "content://media/cover", "data:image/png;base64,fixture", "javascript:fixture", "not a url").forEach {
            assertNull(response(ComicDto(1, "Title", thumbnail = ThumbnailDto(it, "jpg"))).toComics().single().imageUrl)
        }
    }

    @Test fun rejectsInvalidOptionalNumbers() {
        listOf(ComicDto(1, "Title", issueNumber = 0), ComicDto(1, "Title", volumeNumber = -1),
            ComicDto(1, "Title", publicationYear = 0)).forEach {
            assertEquals(ComicsFailure.INVALID_DATA, assertThrows(ComicsException::class.java) { response(it).toComics() }.failure)
        }
    }
    @Test fun mapsDistinctSynopsisAndVolumeWithoutInventingAnIssue() {
        val comic = response(ComicDto(5, "Title", description = "Description", series = "Series",
            volumeNumber = 1, genre = "Genre", publicationYear = 2026,
            shortSynopsis = "Short", synopsis = "Full", coverKey = "eclipse")).toComics().single()
        assertEquals("Description", comic.description)
        assertEquals("Full", comic.synopsis)
        assertEquals("Short", comic.shortSynopsis)
        assertEquals(1, comic.volumeNumber)
        assertNull(comic.issueNumber)
    }
}
