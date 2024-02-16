package com.example.js_comics

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.js_comics.data.remote.ComicsAPI
import com.example.js_comics.data.repository.ComicsRepository
import com.example.js_comics.data.useCase.GetComicsUseCase
import com.example.js_comics.ui.view.ComicsListUiStateError
import com.example.js_comics.ui.view.ComicsViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.whenever
import java.io.IOException

@ExperimentalCoroutinesApi
class ComicsViewModelTest {

    @Mock
    lateinit var apiService: ComicsAPI

    @Mock
    lateinit var repository: ComicsRepository

    @Mock
    lateinit var getComicsUseCase: GetComicsUseCase

    @Mock
    lateinit var viewModel: ComicsViewModel

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var coroutineRule = MainDispatcherRule()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        repository = ComicsRepository(apiService)
        getComicsUseCase = GetComicsUseCase(repository, coroutineRule.testDispatcher)
        viewModel = ComicsViewModel(getComicsUseCase)
    }

    @Test
    fun getComics_isFail() = runTest {
        whenever(repository.getComics()) doAnswer {
            throw IOException()
        }
        viewModel.getComics()
        getComicsUseCase().catch {
            assertEquals(ComicsListUiStateError(), viewModel.state.value)
        }
    }
}