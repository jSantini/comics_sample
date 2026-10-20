package com.example.js_comics.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel as composeViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import com.example.component.LoadingBar
import com.example.js_comics.R
import com.example.js_comics.domain.ComicsFailure

@Composable
fun ComicsRoute(viewModel: ComicsViewModel = composeViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ComicsListScreen(state, viewModel::retry)
}

@Composable
fun ComicsListScreen(state: ComicsUiState, onRetry: () -> Unit) {
    when (state) {
        ComicsUiState.Loading -> LoadingBar()
        is ComicsUiState.Content -> Column(Modifier.fillMaxSize()) {
            SourceLabel(state.isDemo)
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(state.items, key = { it.id }) { ComicCard(it) }
            }
        }
        is ComicsUiState.Empty -> Column(Modifier.fillMaxSize()) {
            SourceLabel(state.isDemo)
            StatusContent(stringResource(R.string.empty_catalog), onRetry)
        }
        is ComicsUiState.Error -> StatusContent(stringResource(state.failure.messageResource()), onRetry)
    }
}

@Composable
private fun SourceLabel(isDemo: Boolean) {
    Text(
        text = stringResource(if (isDemo) R.string.demo_catalog else R.string.remote_catalog),
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.fillMaxWidth().padding(16.dp),
    )
}

@Composable
private fun StatusContent(message: String, onRetry: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(message, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) { Text(stringResource(R.string.retry)) }
    }
}

@Composable
private fun ComicCard(item: ComicItem) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            if (item.imageUrl != null) {
                SubcomposeAsyncImage(
                    model = item.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(88.dp),
                    loading = { CoverPlaceholder() },
                    error = { CoverPlaceholder() },
                )
            } else CoverPlaceholder()
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(item.title, style = MaterialTheme.typography.titleMedium)
                item.description?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            }
        }
    }
}

@Composable
private fun CoverPlaceholder() {
    Box(
        Modifier.size(88.dp).background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(stringResource(R.string.cover_placeholder), style = MaterialTheme.typography.labelSmall)
    }
}

private fun ComicsFailure.messageResource(): Int = when (this) {
    ComicsFailure.NETWORK -> R.string.error_network
    ComicsFailure.TIMEOUT -> R.string.error_timeout
    ComicsFailure.HTTP -> R.string.error_http
    ComicsFailure.INVALID_DATA -> R.string.error_data
    ComicsFailure.UNEXPECTED -> R.string.error_unexpected
}
