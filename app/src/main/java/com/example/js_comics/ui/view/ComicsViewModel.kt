package com.example.js_comics.ui.view

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.js_comics.data.common.ComicResource
import com.example.js_comics.data.model.ComicModel
import com.example.js_comics.data.useCase.GetComicsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ComicsViewModel @Inject constructor(
    private val getComicsUseCase: GetComicsUseCase
) : ViewModel() {

    val state = mutableStateOf<ComicsListUiState>(Loading)

    init {
        getComics()
    }

    fun getComics() = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            getComicsUseCase().collect(::handleResponse)
        }
    }

    private suspend fun handleResponse(it: ComicResource<List<ComicModel>>) =
        withContext(Dispatchers.Main) {
            when (it.status) {
                ComicResource.Status.LOADING -> state.value = Loading
                ComicResource.Status.OK -> state.value =
                    ComicsListUiStateReady(comics = it.data)

                ComicResource.Status.ERROR -> state.value =
                    ComicsListUiStateError(error = it.error?.data?.message)
            }
        }
}

sealed class ComicsListUiState
data class ComicsListUiStateReady(val comics: List<ComicModel>?) :
    ComicsListUiState()

data object Loading : ComicsListUiState()
class ComicsListUiStateError(val error: String? = null) : ComicsListUiState()