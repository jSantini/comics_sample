package com.example.js_comics.data.useCase

import com.example.js_comics.data.common.ComicResource
import com.example.js_comics.data.model.ComicModel
import com.example.js_comics.data.repository.ComicsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GetComicsUseCase @Inject constructor(
    private val repository: ComicsRepository,
    private val defaultDispatcher: CoroutineDispatcher
) {

    private var comicsList = listOf<ComicModel>()

    suspend operator fun invoke(): Flow<ComicResource<List<ComicModel>>> =
        flow {
            try {
                emit(ComicResource.loading())
                val response = repository.getComics()
                if (response?.data == null) {
                    emit(ComicResource.error())
                } else {
                    comicsList = response.data.results
                    emit(ComicResource.success(comicsList))
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                emit(ComicResource.error(e))
            }
        }.flowOn(defaultDispatcher)
}