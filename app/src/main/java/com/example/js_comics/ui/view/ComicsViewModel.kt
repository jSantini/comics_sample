package com.example.js_comics.ui.view

import androidx.annotation.MainThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.js_comics.domain.CatalogSource
import com.example.js_comics.domain.ComicsException
import com.example.js_comics.domain.ComicsFailure
import com.example.js_comics.domain.ComicsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ComicItem(
    val id: Int,
    val title: String,
    val description: String?,
    val imageUrl: String?,
    val series: String? = null,
    val issueNumber: Int? = null,
    val volumeNumber: Int? = null,
    val genre: String? = null,
    val publicationYear: Int? = null,
    val shortSynopsis: String? = description,
    val synopsis: String? = description,
    val coverKey: String? = null,
)

internal fun com.example.js_comics.domain.Comic.toComicItem() = ComicItem(
    id = id,
    title = title,
    description = description,
    imageUrl = imageUrl,
    series = series,
    issueNumber = issueNumber,
    volumeNumber = volumeNumber,
    genre = genre,
    publicationYear = publicationYear,
    shortSynopsis = shortSynopsis,
    synopsis = synopsis,
    coverKey = coverKey,
)
sealed interface ComicsUiState {
    data object Loading : ComicsUiState
    data class Content(val items: List<ComicItem>, val isDemo: Boolean) : ComicsUiState
    data class Empty(val isDemo: Boolean) : ComicsUiState
    data class Error(val failure: ComicsFailure) : ComicsUiState
}

@HiltViewModel
class ComicsViewModel @Inject constructor(private val repository: ComicsRepository) : ViewModel() {
    private val mutableState = MutableStateFlow<ComicsUiState>(ComicsUiState.Loading)
    val state: StateFlow<ComicsUiState> = mutableState.asStateFlow()
    private var loadJob: Job? = null

    init { retry() }

    /** UI actions run on Main. Ignore duplicate requests until the active load ends. */
    @MainThread
    fun retry() {
        if (loadJob?.isActive == true) return
        loadJob = viewModelScope.launch {
            mutableState.value = ComicsUiState.Loading
            try {
                val catalog = repository.getComics()
                currentCoroutineContext().ensureActive()
                val isDemo = catalog.source == CatalogSource.DEMO
                mutableState.value = if (catalog.comics.isEmpty()) ComicsUiState.Empty(isDemo) else {
                    ComicsUiState.Content(catalog.comics.map { it.toComicItem() }, isDemo)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: ComicsException) {
                currentCoroutineContext().ensureActive()
                mutableState.value = ComicsUiState.Error(failure.failure)
            } catch (_: Exception) {
                currentCoroutineContext().ensureActive()
                mutableState.value = ComicsUiState.Error(ComicsFailure.UNEXPECTED)
            }
        }
    }
}
