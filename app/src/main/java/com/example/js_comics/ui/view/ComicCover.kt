package com.example.js_comics.ui.view

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import coil.ImageLoader
import coil.compose.SubcomposeAsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.size.Precision
import com.example.js_comics.R
import kotlinx.coroutines.Dispatchers

/** Only presentation knows Android resource IDs. Unknown keys never resolve dynamically. */
@DrawableRes
internal fun localCoverResource(key: String?): Int? = when (key) {
    "aurora" -> R.drawable.cover_aurora
    "cidade_papel" -> R.drawable.cover_cidade_papel
    "alem_orbita" -> R.drawable.cover_alem_orbita
    "sentinela_neon" -> R.drawable.cover_sentinela_neon
    "eclipse" -> R.drawable.cover_eclipse
    "vigilante" -> R.drawable.cover_vigilante
    else -> null
}

/** Dimensions of the supplied assets; no bitmap/header decoding during composition. */
internal fun localCoverAspectRatio(key: String?): Float? = when (key) {
    "aurora", "cidade_papel", "alem_orbita", "sentinela_neon", "eclipse", "vigilante" -> 2f / 3f
    else -> null
}

internal fun coverAspectRatio(width: Float, height: Float): Float? =
    if (width.isFinite() && height.isFinite() && width > 0f && height > 0f) {
        (width / height).takeIf { it.isFinite() && it > 0f }
    } else null

@Composable
internal fun ComicCover(
    item: ComicItem,
    modifier: Modifier = Modifier,
    imageLoader: ImageLoader = LocalContext.current.imageLoader,
) {
    val context = LocalContext.current
    val data = localCoverResource(item.coverKey) ?: item.imageUrl
    val localRatio = localCoverAspectRatio(item.coverKey)
    var remoteRatio by remember(data) { mutableStateOf<Float?>(null) }
    val request = remember(context, data) {
        data?.let {
            ImageRequest.Builder(context).data(it)
                .fetcherDispatcher(Dispatchers.IO)
                .decoderDispatcher(Dispatchers.IO)
                .precision(Precision.INEXACT)
                .build()
        }
    }
    Box(modifier.aspectRatio(localRatio ?: remoteRatio ?: (2f / 3f)).testTag("comic_cover_${item.id}")
        .background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
        if (request == null) CoverPlaceholder() else SubcomposeAsyncImage(
            model = request,
            imageLoader = imageLoader,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
            onSuccess = { success ->
                if (localRatio == null) {
                    val size = success.painter.intrinsicSize
                    if (size != Size.Unspecified) {
                        coverAspectRatio(size.width, size.height)?.let { remoteRatio = it }
                    }
                }
            },
            // No ORIGINAL size: Coil Compose resolves the finite cover constraints and samples
            // bitmap decoding on IO. Local resource IDs never use the network fetcher.
            loading = { CoverPlaceholder(loading = true) },
            error = { CoverPlaceholder() },
        )
    }
}

@Composable
private fun CoverPlaceholder(loading: Boolean = false) {
    val label = stringResource(if (loading) R.string.cover_loading else R.string.cover_unavailable)
    Box(Modifier.fillMaxSize().clearAndSetSemantics { contentDescription = label }, contentAlignment = Alignment.Center) {
        Text(stringResource(R.string.cover_monogram), style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
