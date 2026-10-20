package com.example.js_comics.data

import com.example.js_comics.data.remote.ComicsAPI
import com.example.js_comics.data.repository.RemoteComicsRepository
import com.example.js_comics.domain.ComicsException
import com.example.js_comics.domain.ComicsFailure
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
class ComicsHttpTest {
    private lateinit var server: MockWebServer
    private lateinit var repository: RemoteComicsRepository
    @Before fun setup() {
        server = MockWebServer()
        server.start()
        // Loopback HTTP is confined to host JVM tests; production requires HTTPS.
        val api = Retrofit.Builder().baseUrl(server.url("/")).addConverterFactory(GsonConverterFactory.create()).build().create(ComicsAPI::class.java)
        repository = RemoteComicsRepository(api)
    }
    @After fun teardown() { server.shutdown() }
    @Test fun deserializesEnvelopeAndSendsNoProviderAuthentication() = runTest {
        server.enqueue(MockResponse().setBody("""{"data":{"results":[{"id":2,"title":"Fixture"}]}}"""))
        assertEquals("Fixture", repository.getComics().comics.single().title)
        val request = server.takeRequest(1, TimeUnit.SECONDS)!!
        assertEquals("GET", request.method)
        assertEquals("/comics", request.path)
        assertNull(request.requestUrl!!.query)
        assertNull(request.getHeader("Authorization"))
    }
    @Test fun emptyRemoteCatalogIsValid() = runTest {
        server.enqueue(MockResponse().setBody("""{"data":{"results":[]}}"""))
        assertTrue(repository.getComics().comics.isEmpty())
    }
    @Test fun httpErrorsDoNotDependOnErrorBodyFormat() = runTest {
        for (code in listOf(401, 429, 503)) {
            server.enqueue(MockResponse().setResponseCode(code).setBody("<html>unavailable</html>"))
            try { repository.getComics(); fail("Expected HTTP failure") }
            catch (failure: ComicsException) { assertEquals(ComicsFailure.HTTP, failure.failure) }
        }
    }
    @Test fun malformedJsonBecomesInvalidData() = runTest {
        server.enqueue(MockResponse().setBody("{"))
        try { repository.getComics(); fail("Expected invalid JSON failure") }
        catch (failure: ComicsException) { assertEquals(ComicsFailure.INVALID_DATA, failure.failure) }
    }
    @Test fun missingRequiredJsonFieldBecomesInvalidData() = runTest {
        server.enqueue(MockResponse().setBody("""{"data":{"results":[{"title":"No ID"}]}}"""))
        try { repository.getComics(); fail("Expected invalid payload") }
        catch (failure: ComicsException) { assertEquals(ComicsFailure.INVALID_DATA, failure.failure) }
    }
    @Test fun noContentOrNullJsonBecomesInvalidData() = runTest {
        for (response in listOf(MockResponse().setResponseCode(204), MockResponse().setBody("null"))) {
            server.enqueue(response)
            try { repository.getComics(); fail("Expected missing payload failure") }
            catch (failure: ComicsException) { assertEquals(ComicsFailure.INVALID_DATA, failure.failure) }
        }
    }
    @Test fun cancelsInFlightRetrofitRequest() = runTest {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))
        val job = launch { repository.getComics() }
        runCurrent()
        assertNotNull(server.takeRequest(5, TimeUnit.SECONDS))
        job.cancelAndJoin()
        assertTrue(job.isCancelled)
    }
}
