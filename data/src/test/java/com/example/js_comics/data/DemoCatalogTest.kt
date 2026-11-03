package com.example.js_comics.data

import com.example.js_comics.data.repository.DemoComicsRepository
import com.example.js_comics.domain.CatalogSource
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.security.MessageDigest

class DemoCatalogTest {
    @Test fun sixStableIdsAndCoverKeysDoNotRequireRemoteImages() = runTest {
        val repository = DemoComicsRepository()
        val catalog = repository.getComics()
        assertEquals(CatalogSource.DEMO, catalog.source)
        assertEquals((1..6).toList(), catalog.comics.map { it.id })
        assertEquals(listOf("aurora", "cidade_papel", "alem_orbita", "sentinela_neon", "eclipse", "vigilante"), catalog.comics.map { it.coverKey })
        assertTrue(catalog.comics.all { it.imageUrl == null })
        assertEquals(catalog, repository.getComics())
    }

    @Test fun metadataAndCompleteTextMatchTheSuppliedFictionalCatalog() = runTest {
        val comics = DemoComicsRepository().getComics().comics
        comics[0].let { comic ->
            assertEquals("Aurora: O Primeiro Sinal", comic.title)
            assertEquals("Aurora", comic.series)
            assertEquals(1, comic.issueNumber)
            assertNull(comic.volumeNumber)
            assertEquals("Ficção científica", comic.genre)
            assertEquals(2026, comic.publicationYear)
            assertEquals("Quando um sinal desconhecido surge nas fronteiras do sistema solar, a capitã Aurora descobre uma mensagem que pode mudar o destino da humanidade.", comic.shortSynopsis)
            assertEquals("4aaff7bad75984d11bd63a72cf526dfacbc82d8b85f214286ae64a981cd28c3e", sha256(comic.synopsis!!))
            assertEquals(comic.synopsis, comic.description)
        }
        comics[1].let { comic ->
            assertEquals("Cidade de Papel: O Arquiteto Invisível", comic.title)
            assertEquals("Cidade de Papel", comic.series)
            assertEquals(3, comic.issueNumber)
            assertNull(comic.volumeNumber)
            assertEquals("Fantasia urbana", comic.genre)
            assertEquals(2026, comic.publicationYear)
            assertEquals("Em uma metrópole que muda de forma todas as manhãs, dois jovens inventores investigam o desaparecimento do homem que desenhou suas ruas.", comic.shortSynopsis)
            assertEquals("d66a2af287129157efe0968cd19f42e657f998e541decc5c3d87708410beffc5", sha256(comic.synopsis!!))
            assertEquals(comic.synopsis, comic.description)
        }
        comics[2].let { comic ->
            assertEquals("Além da Órbita: Retorno a Casa", comic.title)
            assertEquals("Além da Órbita", comic.series)
            assertEquals(2, comic.issueNumber)
            assertNull(comic.volumeNumber)
            assertEquals("Aventura espacial", comic.genre)
            assertEquals(2026, comic.publicationYear)
            assertEquals("Após uma missão fracassada nos limites do espaço conhecido, cinco tripulantes precisam sobreviver e encontrar o caminho de volta à Terra.", comic.shortSynopsis)
            assertEquals("460659c20d421ec7ca679f9a20d171e3fdb5f2a969d769c94f19bf186b840615", sha256(comic.synopsis!!))
            assertEquals(comic.synopsis, comic.description)
        }
        comics[3].let { comic ->
            assertEquals("Sentinela Neon: Memórias Roubadas", comic.title)
            assertEquals("Sentinela Neon", comic.series)
            assertEquals(5, comic.issueNumber)
            assertNull(comic.volumeNumber)
            assertEquals("Cyberpunk", comic.genre)
            assertEquals(2026, comic.publicationYear)
            assertEquals("Em uma cidade onde lembranças podem ser compradas e vendidas, uma vigilante descobre que sua própria identidade foi fabricada.", comic.shortSynopsis)
            assertEquals("7fb8e131f4e9c4942ea0e7ed250b435340a86daa2d61d70c88831af3db9f5d8d", sha256(comic.synopsis!!))
            assertEquals(comic.synopsis, comic.description)
        }
        comics[4].let { comic ->
            assertEquals("Crônicas do Eclipse: O Último Guardião", comic.title)
            assertEquals("Crônicas do Eclipse", comic.series)
            assertNull(comic.issueNumber)
            assertEquals(1, comic.volumeNumber)
            assertEquals("Fantasia cósmica", comic.genre)
            assertEquals(2026, comic.publicationYear)
            assertEquals("Quando o Sol desaparece durante uma hora todos os dias, antigos guardiões despertam para impedir que a escuridão se torne permanente.", comic.shortSynopsis)
            assertEquals("c1e05f6df6d5e2ad92e53dc5dd873ef5e0dcdf190af90d08c0b79e1dc7096d30", sha256(comic.synopsis!!))
            assertEquals(comic.synopsis, comic.description)
        }
        comics[5].let { comic ->
            assertEquals("O Vigilante das Sombras: Caso Zero", comic.title)
            assertEquals("Vigilante das Sombras", comic.series)
            assertEquals(1, comic.issueNumber)
            assertNull(comic.volumeNumber)
            assertEquals("Mistério / Noir", comic.genre)
            assertEquals(2026, comic.publicationYear)
            assertEquals("Um investigador mascarado procura a origem de uma sequência de desaparecimentos ligados a uma organização que oficialmente não existe.", comic.shortSynopsis)
            assertEquals("25b57767469766fc5b99bf739417c655171f2b6b4bd2d7fef3412c391d1994ff", sha256(comic.synopsis!!))
            assertEquals(comic.synopsis, comic.description)
        }
    }

    // Fixed digests protect the complete supplied UTF-8 synopsis, including accents.
    private fun sha256(text: String) = MessageDigest.getInstance("SHA-256")
        .digest(text.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
}
