package com.example.js_comics.data.remote.response

import com.example.js_comics.data.model.ComicModel

data class GetComicsResponse(
    val results: List<ComicModel> = listOf()
)
