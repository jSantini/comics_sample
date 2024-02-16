package com.example.js_comics.data.repository

import com.example.js_comics.data.common.ComicResource
import com.example.js_comics.data.remote.ComicsAPI
import com.example.js_comics.data.remote.response.GetComicsResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ComicsRepository @Inject constructor(
    private val comicsAPI: ComicsAPI
) {

    suspend fun getComics(
    ): ComicResource<GetComicsResponse>? {
        return comicsAPI.getComics(
            ts = "1682982412",
            apikey = "b7e14bab409c70a5c4e7c2b319c09d7b",
            hash = "3482f01e9bf207a437a4b621c91339ad"
        )
    }
}