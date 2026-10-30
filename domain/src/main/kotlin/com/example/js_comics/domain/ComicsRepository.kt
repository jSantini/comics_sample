package com.example.js_comics.domain

/** Optional metadata is absent when the provider does not supply it.
 * The first four fields retain the existing source contract.
 */
data class Comic(
    val id: Int,
    val title: String,
    val description: String?,
    val imageUrl: String?,
    val series: String? = null,
    val issueNumber: Int? = null,
    val volumeNumber: Int? = null,
    val genre: String? = null,
    val publicationYear: Int? = null,
    val shortSynopsis: String? = description,
    val synopsis: String? = description,
    val coverKey: String? = null,
)
enum class CatalogSource { DEMO, REMOTE }
data class ComicsCatalog(val comics: List<Comic>, val source: CatalogSource)

interface ComicsRepository {
    /** A single refresh. Implementations preserve coroutine cancellation. */
    suspend fun getComics(): ComicsCatalog
}

enum class ComicsFailure { NETWORK, TIMEOUT, HTTP, INVALID_DATA, UNEXPECTED }
class ComicsException(val failure: ComicsFailure, cause: Throwable? = null) : Exception(cause)
