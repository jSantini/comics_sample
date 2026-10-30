package com.example.js_comics.ui.view

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.js_comics.domain.ComicsFailure
import com.example.js_comics.ui.theme.Js_comicsTheme

@Preview(name = "Claro", showBackground = true, widthDp = 360, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Escuro", showBackground = true, widthDp = 360, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Texto ampliado", showBackground = true, widthDp = 360, heightDp = 800, fontScale = 2f)
@Preview(name = "Tela pequena", showBackground = true, widthDp = 320, heightDp = 640)
@Preview(name = "Escuro com texto ampliado", showBackground = true, widthDp = 320, heightDp = 640,
    fontScale = 2f, uiMode = Configuration.UI_MODE_NIGHT_YES)
private annotation class CatalogPreviews

private val previewItems = listOf(
    ComicItem(1, "Aurora: O Primeiro Sinal", "Uma transmissão pode mudar o destino da humanidade.", null,
        series = "Aurora", issueNumber = 1, genre = "Ficção científica", publicationYear = 2026,
        synopsis = "Uma missão fictícia além de Netuno investiga uma estação desaparecida.", coverKey = "aurora"),
    ComicItem(5, "Crônicas do Eclipse: O Último Guardião", "Antigos guardiões despertam.", null,
        series = "Crônicas do Eclipse", volumeNumber = 1, genre = "Fantasia cósmica", publicationYear = 2026,
        synopsis = "Durante um eclipse inexplicável, os últimos guardiões precisam reunir suas forças para salvar o amanhecer.",
        coverKey = "eclipse"),
)

@CatalogPreviews
@Composable
private fun ContentPreview() {
    Js_comicsTheme { ComicsListScreen(ComicsUiState.Content(previewItems, isDemo = true), onRetry = {}) }
}

@CatalogPreviews
@Composable
private fun LoadingPreview() {
    Js_comicsTheme { ComicsListScreen(ComicsUiState.Loading, onRetry = {}) }
}

@CatalogPreviews
@Composable
private fun EmptyPreview() {
    Js_comicsTheme { ComicsListScreen(ComicsUiState.Empty(isDemo = true), onRetry = {}) }
}

@CatalogPreviews
@Composable
private fun ErrorPreview() {
    Js_comicsTheme { ComicsListScreen(ComicsUiState.Error(ComicsFailure.NETWORK), onRetry = {}) }
}

@CatalogPreviews
@Composable
private fun DetailPreview() {
    Js_comicsTheme { ComicDetailsScreen(ComicsUiState.Content(previewItems, true), 1, {}, {}) }
}

@CatalogPreviews
@Composable
private fun PortraitDetailPreview() {
    Js_comicsTheme { ComicDetailsScreen(ComicsUiState.Content(previewItems, true), 5, {}, {}) }
}

@CatalogPreviews
@Composable
private fun RemoteWithoutMetadataPreview() {
    val remote = listOf(ComicItem(42, "História do catálogo remoto", null, null))
    Js_comicsTheme { ComicDetailsScreen(ComicsUiState.Content(remote, false), 42, {}, {}) }
}
