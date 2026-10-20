package com.example.js_comics.data.remote

import com.example.js_comics.data.remote.response.ComicsResponseDto
import retrofit2.Response
import retrofit2.http.GET

/** Public catalog gateway. Provider authentication belongs on the server. */
interface ComicsAPI {
    @GET("comics")
    suspend fun getComics(): Response<ComicsResponseDto>
}
