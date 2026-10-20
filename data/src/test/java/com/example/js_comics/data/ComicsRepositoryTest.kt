package com.example.js_comics.data

import com.example.js_comics.data.remote.ComicsAPI
import com.example.js_comics.data.remote.response.*
import com.example.js_comics.data.repository.*
import com.example.js_comics.domain.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Response
import java.io.InterruptedIOException
import java.io.IOException
import java.net.SocketTimeoutException

@OptIn(ExperimentalCoroutinesApi::class)
class ComicsRepositoryTest {
    private fun api(block: suspend () -> ComicsResponseDto) = object : ComicsAPI {
        override suspend fun getComics() = Response.success(block())
    }
    @Test fun demoReturnsStableFictionalCatalogWithoutRemoteDependency() = runTest {
        val repository = DemoComicsRepository()
        val first = repository.getComics()
        assertEquals(CatalogSource.DEMO, first.source)
        assertEquals(3, first.comics.size)
        assertTrue(first.comics.all { it.imageUrl == null })
        assertEquals(first, repository.getComics())
    }
    @Test fun remoteMapsSuccess() = runTest {
        val result = RemoteComicsRepository(api {
            ComicsResponseDto(ComicsDataDto(listOf(ComicDto(1, "Title"))))
        }).getComics()
        assertEquals(CatalogSource.REMOTE, result.source)
        assertEquals("Title", result.comics.single().title)
    }
    @Test fun mapsOfflineAndTimeoutSeparately() = runTest {
        for ((exception, expected) in listOf(IOException() to ComicsFailure.NETWORK, SocketTimeoutException() to ComicsFailure.TIMEOUT, InterruptedIOException() to ComicsFailure.TIMEOUT)) {
            try {
                RemoteComicsRepository(api { throw exception }).getComics()
                fail("Expected domain failure")
            } catch (failure: ComicsException) { assertEquals(expected, failure.failure) }
        }
    }
    @Test fun invalidPayloadBecomesDomainFailure() = runTest {
        try {
            RemoteComicsRepository(api { ComicsResponseDto() }).getComics()
            fail("Expected invalid data")
        } catch (failure: ComicsException) { assertEquals(ComicsFailure.INVALID_DATA, failure.failure) }
    }
    @Test fun preservesCancellationExceptionIdentity() = runTest {
        val cancellation = CancellationException("test cancellation")
        try {
            RemoteComicsRepository(api { throw cancellation }).getComics()
            fail("Expected cancellation")
        } catch (actual: CancellationException) { assertSame(cancellation, actual) }
    }
    @Test fun cancellingCallerCancelsInFlightSource() = runTest {
        var cancelled = false
        val repository = RemoteComicsRepository(api {
            try { awaitCancellation() } finally { cancelled = true }
        })
        val job = launch { repository.getComics() }
        runCurrent()
        job.cancelAndJoin()
        assertTrue(job.isCancelled)
        assertTrue(cancelled)
    }
}
