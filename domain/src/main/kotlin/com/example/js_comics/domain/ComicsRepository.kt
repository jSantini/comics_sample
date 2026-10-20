package com.example.js_comics.domain

data class Comic(val id: Int, val title: String, val description: String?, val imageUrl: String?)
enum class CatalogSource { DEMO, REMOTE }
data class ComicsCatalog(val comics: List<Comic>, val source: CatalogSource)

interface ComicsRepository {
    /** A single refresh. Implementations preserve coroutine cancellation. */
    suspend fun getComics(): ComicsCatalog
}

enum class ComicsFailure { NETWORK, TIMEOUT, HTTP, INVALID_DATA, UNEXPECTED }
class ComicsException(val failure: ComicsFailure, cause: Throwable? = null) : Exception(cause)
