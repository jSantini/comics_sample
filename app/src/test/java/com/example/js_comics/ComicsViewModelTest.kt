package com.example.js_comics

import androidx.lifecycle.ViewModelStore
import com.example.js_comics.domain.*
import com.example.js_comics.ui.view.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ComicsViewModelTest {
    @get:Rule val main = MainDispatcherRule()
    private val catalog = ComicsCatalog(listOf(Comic(1, "Fiction", "Story", null)), CatalogSource.DEMO)
    private class FakeRepository(val load: suspend (Int) -> ComicsCatalog) : ComicsRepository {
        var calls = 0
        override suspend fun getComics() = load(++calls)
    }
    @Test fun loadingThenContentMapsDomainToUi() = runTest {
        val repository = FakeRepository { catalog }
        val vm = ComicsViewModel(repository)
        assertEquals(ComicsUiState.Loading, vm.state.value)
        advanceUntilIdle()
        assertEquals(ComicsUiState.Content(listOf(ComicItem(1, "Fiction", "Story", null)), true), vm.state.value)
        assertEquals(1, repository.calls)
    }
    @Test fun emptyRemoteCatalogHasExplicitState() = runTest {
        val vm = ComicsViewModel(FakeRepository { ComicsCatalog(emptyList(), CatalogSource.REMOTE) })
        advanceUntilIdle()
        assertEquals(ComicsUiState.Empty(false), vm.state.value)
    }
    @Test fun errorRetryLoadingAndSuccessAreObserved() = runTest {
        val gate = CompletableDeferred<ComicsCatalog>()
        val repository = FakeRepository { call ->
            if (call == 1) throw ComicsException(ComicsFailure.NETWORK)
            gate.await()
        }
        val vm = ComicsViewModel(repository)
        val observed = mutableListOf<ComicsUiState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.toList(observed) }
        runCurrent()
        assertEquals(ComicsUiState.Error(ComicsFailure.NETWORK), vm.state.value)
        vm.retry()
        runCurrent()
        assertEquals(ComicsUiState.Loading, vm.state.value)
        gate.complete(catalog)
        advanceUntilIdle()
        assertEquals(listOf(ComicsUiState.Loading, ComicsUiState.Error(ComicsFailure.NETWORK), ComicsUiState.Loading,
            ComicsUiState.Content(listOf(ComicItem(1, "Fiction", "Story", null)), true)), observed)
        assertEquals(2, repository.calls)
    }
    @Test fun duplicateRetryDoesNotStartConcurrentRequests() = runTest {
        val gate = CompletableDeferred<ComicsCatalog>()
        val repository = FakeRepository { gate.await() }
        val vm = ComicsViewModel(repository)
        vm.retry()
        runCurrent()
        repeat(5) { vm.retry() }
        runCurrent()
        assertEquals(1, repository.calls)
        gate.complete(catalog)
        advanceUntilIdle()
        assertTrue(vm.state.value is ComicsUiState.Content)
    }
    @Test fun clearingViewModelCancelsSourceWithoutEmittingError() = runTest {
        var cancelled = false
        val repository = FakeRepository {
            try { awaitCancellation() } finally { cancelled = true }
        }
        val vm = ComicsViewModel(repository)
        val store = ViewModelStore()
        store.put("comics", vm)
        runCurrent()
        store.clear()
        runCurrent()
        assertTrue(cancelled)
        assertEquals(ComicsUiState.Loading, vm.state.value)
    }
    @Test fun unexpectedFailureHasSafeUiState() = runTest {
        val vm = ComicsViewModel(FakeRepository { throw IllegalStateException("internal detail") })
        advanceUntilIdle()
        assertEquals(ComicsUiState.Error(ComicsFailure.UNEXPECTED), vm.state.value)
    }
}
