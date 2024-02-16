package com.example.js_comics.data.remote

import com.example.js_comics.data.common.ComicResource
import com.example.js_comics.data.model.ComicModel
import com.example.js_comics.data.remote.response.GetComicsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ComicsAPI {

    @GET("comics")
    suspend fun getComics(
        @Query("ts") ts: String,
        @Query("apikey") apikey: String,
        @Query("hash") hash: String
    ) : ComicResource<GetComicsResponse>?
}