package com.example.js_comics.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.component.LoadingBar
import com.example.js_comics.R

/** Pure lookup: opening details never triggers another repository request. */
internal fun ComicsUiState.comicWithId(id: Int?): ComicItem? =
    (this as? ComicsUiState.Content)?.items?.firstOrNull { it.id == id && it.id > 0 }

@Composable
fun ComicDetailsScreen(state: ComicsUiState, comicId: Int?, onBack: () -> Unit, onRetry: () -> Unit) {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().testTag("comic_details"), horizontalAlignment = Alignment.CenterHorizontally) {
            TextButton(onClick = onBack, modifier = Modifier.widthIn(max = 720.dp).fillMaxWidth()
                .heightIn(min = 48.dp).testTag("details_back")) {
                Text(stringResource(R.string.back_to_catalog))
            }
            val item = state.comicWithId(comicId)
            when {
                comicId == null || comicId <= 0 -> UnavailableComic()
                state == ComicsUiState.Loading -> LoadingBar()
                state is ComicsUiState.Error -> StatusContent(
                    stringResource(R.string.error_title), stringResource(state.failure.messageResource()), onRetry)
                item == null -> UnavailableComic()
                else -> Column(
                    Modifier.weight(1f).widthIn(max = 720.dp).fillMaxWidth()
                        .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    SourceLabel((state as ComicsUiState.Content).isDemo)
                    ComicCover(item, Modifier.widthIn(max = 280.dp).fillMaxWidth().align(Alignment.CenterHorizontally))
                    Text(item.title, style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.testTag("details_title").semantics { heading() })
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        item.series?.let { Text(stringResource(R.string.comic_series, it)) }
                        IssueOrVolume(item)
                        item.genre?.let { Text(stringResource(R.string.comic_genre, it)) }
                        item.publicationYear?.let { Text(stringResource(R.string.comic_year, it)) }
                    }
                    item.synopsis?.let {
                        Text(stringResource(R.string.comic_synopsis), style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.semantics { heading() })
                        Text(it, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.testTag("details_synopsis"))
                    }
                }
            }
        }
    }
}

@Composable
private fun UnavailableComic() {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)
        .testTag("details_unavailable"), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(stringResource(R.string.comic_unavailable_title), style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.semantics { heading() })
        Text(stringResource(R.string.comic_unavailable), style = MaterialTheme.typography.bodyLarge)
    }
}
