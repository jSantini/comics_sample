package com.example.js_comics.data.repository

import com.example.js_comics.data.remote.ComicsAPI
import com.example.js_comics.data.remote.response.toComics
import com.example.js_comics.domain.CatalogSource
import com.example.js_comics.domain.ComicsCatalog
import com.example.js_comics.domain.ComicsException
import com.example.js_comics.domain.ComicsFailure
import com.example.js_comics.domain.ComicsRepository
import com.google.gson.JsonParseException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import java.io.EOFException
import java.io.IOException
import java.io.InterruptedIOException

class RemoteComicsRepository(private val api: ComicsAPI) : ComicsRepository {
    override suspend fun getComics(): ComicsCatalog = try {
        val response = api.getComics()
        currentCoroutineContext().ensureActive()
        if (!response.isSuccessful) throw ComicsException(ComicsFailure.HTTP)
        val body = response.body() ?: throw ComicsException(ComicsFailure.INVALID_DATA)
        ComicsCatalog(body.toComics(), CatalogSource.REMOTE)
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (failure: InterruptedIOException) {
        currentCoroutineContext().ensureActive()
        throw ComicsException(ComicsFailure.TIMEOUT, failure)
    } catch (failure: EOFException) {
        currentCoroutineContext().ensureActive()
        throw ComicsException(ComicsFailure.INVALID_DATA, failure)
    } catch (failure: IOException) {
        currentCoroutineContext().ensureActive()
        throw ComicsException(ComicsFailure.NETWORK, failure)
    } catch (failure: JsonParseException) {
        currentCoroutineContext().ensureActive()
        throw ComicsException(ComicsFailure.INVALID_DATA, failure)
    }
}
