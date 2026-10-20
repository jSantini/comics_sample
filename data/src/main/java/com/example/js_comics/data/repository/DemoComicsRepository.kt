package com.example.js_comics.data.repository

import com.example.js_comics.domain.CatalogSource
import com.example.js_comics.domain.Comic
import com.example.js_comics.domain.ComicsCatalog
import com.example.js_comics.domain.ComicsRepository
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/** Fictional, embedded catalog: no network, credentials or image downloads. */
class DemoComicsRepository : ComicsRepository {
    override suspend fun getComics(): ComicsCatalog {
        currentCoroutineContext().ensureActive()
        return ComicsCatalog(
            listOf(
                Comic(1, "Aurora: o primeiro sinal", "Uma exploradora encontra uma transmissão entre as estrelas.", null),
                Comic(2, "Cidade de papel", "Dois inventores redesenham uma cidade imaginária.", null),
                Comic(3, "Além da órbita", "Uma tripulação aprende a voltar para casa.", null),
            ),
            CatalogSource.DEMO,
        )
    }
}
