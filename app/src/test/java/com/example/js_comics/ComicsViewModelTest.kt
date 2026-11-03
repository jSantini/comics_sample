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
    @Test fun detailsLookupReusesLoadedCatalogWithoutAnotherRequest() = runTest {
        val comic = Comic(1, "Fiction", "Full", null, series = "Series", issueNumber = 1,
            genre = "Genre", publicationYear = 2026, shortSynopsis = "Short", coverKey = "aurora")
        val repository = FakeRepository { ComicsCatalog(listOf(comic), CatalogSource.DEMO) }
        val vm = ComicsViewModel(repository)
        advanceUntilIdle()
        val selected = vm.state.value.comicWithId(1)!!
        assertEquals("Full", selected.synopsis)
        assertEquals("Short", selected.shortSynopsis)
        assertEquals("aurora", selected.coverKey)
        assertEquals("Series", selected.series)
        repeat(3) { vm.state.value.comicWithId(1) }
        assertNull(vm.state.value.comicWithId(99))
        assertEquals(1, repository.calls)
    }
    @Test fun unexpectedFailureHasSafeUiState() = runTest {
        val vm = ComicsViewModel(FakeRepository { throw IllegalStateException("internal detail") })
        advanceUntilIdle()
        assertEquals(ComicsUiState.Error(ComicsFailure.UNEXPECTED), vm.state.value)
    }

    @Test fun offlineDefaultSwitchBothWaysAndRetryActiveSource() = runTest {
        val offline = FakeRepository { catalog }
        val remote = FakeRepository { ComicsCatalog(listOf(Comic(1, "Online", "Remote story", null)), CatalogSource.REMOTE) }
        val vm = ComicsViewModel(offline, remote)
        assertEquals(CatalogSource.DEMO, vm.source.value)
        advanceUntilIdle()
        assertEquals(0, remote.calls)
        vm.selectSource(CatalogSource.REMOTE)
        assertEquals(ComicsUiState.Loading, vm.state.value)
        assertNull(vm.state.value.comicWithId(1))
        advanceUntilIdle()
        assertEquals("Remote story", vm.state.value.comicWithId(1)!!.synopsis)
        assertFalse((vm.state.value as ComicsUiState.Content).isDemo)
        vm.retry()
        advanceUntilIdle()
        assertEquals(2, remote.calls)
        vm.selectSource(CatalogSource.DEMO)
        advanceUntilIdle()
        assertEquals("Story", vm.state.value.comicWithId(1)!!.synopsis)
        vm.retry()
        advanceUntilIdle()
        assertEquals(3, offline.calls)
    }

    @Test fun onlineFailureDoesNotFallBackAndRetryUsesOnline() = runTest {
        val offline = FakeRepository { catalog }
        val remote = FakeRepository { throw ComicsException(ComicsFailure.NETWORK) }
        val vm = ComicsViewModel(offline, remote)
        advanceUntilIdle()
        vm.selectSource(CatalogSource.REMOTE)
        advanceUntilIdle()
        assertEquals(ComicsUiState.Error(ComicsFailure.NETWORK), vm.state.value)
        assertNull(vm.state.value.comicWithId(1))
        vm.retry()
        advanceUntilIdle()
        assertEquals(2, remote.calls)
        assertEquals(1, offline.calls)
    }

    @Test fun rapidSwitchCancelsOldSourceEvenWhenItReturnsLate() = runTest {
        val late = CompletableDeferred<Unit>()
        var cancelled = false
        val remote = FakeRepository {
            try { awaitCancellation() } catch (_: CancellationException) {
                cancelled = true
                withContext(NonCancellable) { late.await() }
            }
            catalog.copy(source = CatalogSource.REMOTE)
        }
        val vm = ComicsViewModel(FakeRepository { catalog }, remote)
        advanceUntilIdle()
        vm.selectSource(CatalogSource.REMOTE)
        runCurrent()
        vm.selectSource(CatalogSource.DEMO)
        runCurrent()
        assertTrue(cancelled)
        late.complete(Unit)
        advanceUntilIdle()
        assertTrue((vm.state.value as ComicsUiState.Content).isDemo)
        repeat(5) {
            vm.selectSource(CatalogSource.REMOTE)
            vm.selectSource(CatalogSource.DEMO)
        }
        advanceUntilIdle()
        assertTrue((vm.state.value as ComicsUiState.Content).isDemo)
    }

    @Test fun emptyOnlineCatalogKeepsRemoteSource() = runTest {
        val vm = ComicsViewModel(FakeRepository { catalog },
            FakeRepository { ComicsCatalog(emptyList(), CatalogSource.REMOTE) })
        advanceUntilIdle()
        vm.selectSource(CatalogSource.REMOTE)
        advanceUntilIdle()
        assertEquals(ComicsUiState.Empty(false), vm.state.value)
    }
}
