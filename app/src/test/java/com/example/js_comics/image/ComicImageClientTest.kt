package com.example.js_comics.image

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.HeldCertificate
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.IOException
import javax.net.ssl.SSLHandshakeException

/** Real image transport over loopback TLS; no Android/Coil decoding is claimed. */
class ComicImageClientTest {
    private lateinit var server: MockWebServer
    private lateinit var client: OkHttpClient

    @Before
    fun setup() {
        val certificate = HeldCertificate.Builder().addSubjectAlternativeName("localhost").build()
        val serverTrust = HandshakeCertificates.Builder().heldCertificate(certificate).build()
        server = MockWebServer().apply {
            useHttps(serverTrust.sslSocketFactory(), false)
            start()
        }
        // Trust this test certificate only. Production uses the platform TLS trust store.
        val clientTrust = HandshakeCertificates.Builder().addTrustedCertificate(certificate.certificate).build()
        client = comicImageClient().newBuilder()
            .sslSocketFactory(clientTrust.sslSocketFactory(), clientTrust.trustManager)
            .build()
    }

    @After
    fun teardown() {
        client.connectionPool.evictAll()
        client.dispatcher.executorService.shutdown()
        server.shutdown()
    }

    @Test
    fun directHttpsImageCanBeDownloaded() {
        server.enqueue(MockResponse().setHeader("Content-Type", "image/png").setBody("fixture image bytes"))
        client.newCall(Request.Builder().url(server.url("/cover.png")).build()).execute().use {
            assertEquals(200, it.code)
            assertEquals("fixture image bytes", it.body!!.string())
        }
        assertEquals("/cover.png", server.takeRequest().path)
    }

    @Test
    fun rejectsHttpBeforeSendingARequest() {
        MockWebServer().use { http ->
            http.start()
            assertThrows(IOException::class.java) {
                client.newCall(Request.Builder().url(http.url("/cover.png")).build()).execute().close()
            }
            assertEquals(0, http.requestCount)
        }
    }

    @Test
    fun rejectsUserinfoQueryAndFragmentBeforeNetwork() {
        val base = server.url("/cover.png")
        val invalid = listOf(
            base.newBuilder().username("fixture-user").password("fixture-password").build(),
            base.newBuilder().query("token=fixture").build(),
            base.newBuilder().fragment("fixture").build(),
        )
        invalid.forEach { url ->
            assertThrows(IOException::class.java) {
                client.newCall(Request.Builder().url(url).build()).execute().close()
            }
        }
        assertEquals(0, server.requestCount)
    }

    @Test
    fun doesNotFollowHttpsRedirectEvenOnSameHost() {
        server.enqueue(MockResponse().setResponseCode(302).setHeader("Location", server.url("/target.png")))
        server.enqueue(MockResponse().setBody("must not be downloaded"))
        client.newCall(Request.Builder().url(server.url("/cover.png")).build()).execute().use {
            assertEquals(302, it.code)
        }
        assertEquals(1, server.requestCount)
    }

    @Test
    fun doesNotFollowRedirectToAnotherHost() {
        MockWebServer().use { target ->
            target.start()
            val destination = target.url("/target.png").newBuilder().scheme("https").host("127.0.0.1").build()
            server.enqueue(MockResponse().setResponseCode(307).setHeader("Location", destination))
            client.newCall(Request.Builder().url(server.url("/cover.png")).build()).execute().use {
                assertEquals(307, it.code)
            }
            assertEquals(0, target.requestCount)
        }
    }

    @Test
    fun doesNotFollowHttpsToHttpDowngrade() {
        MockWebServer().use { target ->
            target.start()
            server.enqueue(MockResponse().setResponseCode(301).setHeader("Location", target.url("/target.png")))
            client.newCall(Request.Builder().url(server.url("/cover.png")).build()).execute().use {
                assertEquals(301, it.code)
            }
            assertEquals(0, target.requestCount)
        }
    }

    @Test
    fun productionClientRejectsUntrustedCertificate() {
        val productionClient = comicImageClient()
        try {
            assertThrows(SSLHandshakeException::class.java) {
                productionClient.newCall(Request.Builder().url(server.url("/cover.png")).build()).execute().close()
            }
            assertEquals(0, server.requestCount)
        } finally {
            productionClient.connectionPool.evictAll()
            productionClient.dispatcher.executorService.shutdown()
        }
    }
}
