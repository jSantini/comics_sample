package com.example.js_comics.ui.view

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.js_comics.domain.ComicsFailure
import com.example.js_comics.ui.theme.Js_comicsTheme

@Preview(name = "Light", showBackground = true, widthDp = 360, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", showBackground = true, widthDp = 360, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Large text", showBackground = true, widthDp = 360, heightDp = 800, fontScale = 2f)
private annotation class CatalogPreviews

private val previewItems = listOf(
    ComicItem(1, "The paper city", "Two inventors build a place where every street tells a fictional story.", null),
    ComicItem(2, "Beyond the orbit", "An imaginary crew follows a signal that might lead them home.", null),
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
