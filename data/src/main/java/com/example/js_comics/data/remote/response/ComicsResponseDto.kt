package com.example.js_comics.data.remote.response

import com.example.js_comics.domain.Comic
import com.example.js_comics.domain.ComicsException
import com.example.js_comics.domain.ComicsFailure
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

// Nullable DTO fields deliberately model incomplete/untrusted JSON.
data class ComicsResponseDto(val data: ComicsDataDto? = null)
data class ComicsDataDto(val results: List<ComicDto?>? = null)
data class ComicDto(
    val id: Int? = null,
    val title: String? = null,
    val description: String? = null,
    val thumbnail: ThumbnailDto? = null,
)
data class ThumbnailDto(val path: String? = null, val extension: String? = null)

fun ComicsResponseDto.toComics(): List<Comic> {
    val results = data?.results ?: invalidData()
    val comics = results.map { dto ->
        dto ?: invalidData()
        val id = dto.id?.takeIf { it > 0 } ?: invalidData()
        val title = dto.title?.trim()?.takeIf { it.isNotEmpty() } ?: invalidData()
        val image = dto.thumbnail?.let { thumbnail ->
            val path = thumbnail.path ?: return@let null
            val extension = thumbnail.extension ?: return@let null
            "$path.$extension".toHttpUrlOrNull()
                ?.takeIf { it.isHttps && it.username.isEmpty() && it.password.isEmpty() && it.query == null && it.fragment == null }
                ?.toString()
        }
        Comic(id, title, dto.description?.trim()?.takeIf { it.isNotEmpty() }, image)
    }
    if (comics.map { it.id }.distinct().size != comics.size) invalidData()
    return comics
}

private fun invalidData(): Nothing = throw ComicsException(ComicsFailure.INVALID_DATA)
