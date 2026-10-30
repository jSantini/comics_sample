package com.example.js_comics.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel as composeViewModel
import com.example.component.LoadingBar
import com.example.js_comics.R
import com.example.js_comics.domain.ComicsFailure

/** Stable selectors; accessibility meaning is provided separately by text and semantics. */
object ComicsScreenTags {
    const val LOADING = "comics_loading"
    const val CONTENT = "comics_content"
    const val EMPTY = "comics_empty"
    const val ERROR = "comics_error"
    const val RETRY = "comics_retry"
    const val DEMO = "comics_demo"
    const val REMOTE = "comics_remote"
}

@Composable
fun ComicsRoute(viewModel: ComicsViewModel = composeViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ComicsCatalogNavigation(state, viewModel::retry)
}

@Composable
fun ComicsListScreen(state: ComicsUiState, onRetry: () -> Unit, onComicClick: (Int) -> Unit = {}) {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        when (state) {
            ComicsUiState.Loading -> Box(Modifier.testTag(ComicsScreenTags.LOADING)) { LoadingBar() }
            is ComicsUiState.Content -> LazyColumn(
                modifier = Modifier.fillMaxSize().testTag(ComicsScreenTags.CONTENT),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item(key = "source") { SourceLabel(state.isDemo) }
                item(key = "heading") {
                    Text(
                        stringResource(R.string.catalog_title),
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.widthIn(max = 720.dp).fillMaxWidth().semantics { heading() },
                    )
                }
                items(state.items, key = { it.id }) { ComicCard(it, onComicClick) }
            }
            is ComicsUiState.Empty -> Column(Modifier.fillMaxSize().testTag(ComicsScreenTags.EMPTY)) {
                SourceLabel(state.isDemo, Modifier.padding(horizontal = 20.dp, vertical = 16.dp))
                StatusContent(stringResource(R.string.empty_title), stringResource(R.string.empty_catalog), onRetry)
            }
            is ComicsUiState.Error -> Box(Modifier.testTag(ComicsScreenTags.ERROR)) {
                StatusContent(stringResource(R.string.error_title), stringResource(state.failure.messageResource()), onRetry)
            }
        }
    }
}

@Composable
internal fun SourceLabel(isDemo: Boolean, modifier: Modifier = Modifier, compact: Boolean = false) {
    Surface(
        modifier.widthIn(max = 720.dp).fillMaxWidth().testTag(if (isDemo) ComicsScreenTags.DEMO else ComicsScreenTags.REMOTE),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = if (compact) 8.dp else 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(stringResource(if (isDemo) R.string.demo_catalog else R.string.remote_catalog), style = MaterialTheme.typography.titleSmall)
            if (!compact) Text(stringResource(if (isDemo) R.string.demo_description else R.string.remote_description), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
internal fun StatusContent(title: String, message: String, onRetry: () -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 480.dp).semantics { heading() })
        Spacer(Modifier.height(12.dp))
        Text(message, style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 480.dp).semantics { liveRegion = LiveRegionMode.Polite })
        Spacer(Modifier.height(24.dp))
        Button(onClick = onRetry, modifier = Modifier.heightIn(min = 48.dp).testTag(ComicsScreenTags.RETRY)) {
            Text(stringResource(R.string.retry))
        }
    }
}

@Composable
private fun ComicCard(item: ComicItem, onComicClick: (Int) -> Unit) {
    val clickLabel = stringResource(R.string.open_details, item.title)
    Card(
        modifier = Modifier.widthIn(max = 720.dp).fillMaxWidth().testTag("comic_card_${item.id}")
            .clickable(role = Role.Button, onClickLabel = clickLabel) { onComicClick(item.id) },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.width(88.dp), contentAlignment = Alignment.TopCenter) {
                ComicCover(item, Modifier.widthIn(max = 88.dp).heightIn(max = 132.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(item.title, style = MaterialTheme.typography.titleMedium)
                item.genre?.let { Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary) }
                IssueOrVolume(item)
                item.shortSynopsis?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("comic_summary_${item.id}"))
                }
            }
        }
    }
}

@Composable
internal fun IssueOrVolume(item: ComicItem) {
    item.issueNumber?.let { Text(stringResource(R.string.comic_issue, it), style = MaterialTheme.typography.labelLarge) }
    item.volumeNumber?.let { Text(stringResource(R.string.comic_volume, it), style = MaterialTheme.typography.labelLarge) }
}

internal fun ComicsFailure.messageResource(): Int = when (this) {
    ComicsFailure.NETWORK -> R.string.error_network
    ComicsFailure.TIMEOUT -> R.string.error_timeout
    ComicsFailure.HTTP -> R.string.error_http
    ComicsFailure.INVALID_DATA -> R.string.error_data
    ComicsFailure.UNEXPECTED -> R.string.error_unexpected
}
