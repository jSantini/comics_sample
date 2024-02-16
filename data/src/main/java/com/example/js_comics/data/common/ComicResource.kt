package com.example.js_comics.data.common

data class ComicResource<out T>(val status: Status, val data: T?, val error: Error? = null) {

    enum class Status {
        OK,
        ERROR,
        LOADING,
    }

    companion object {

        fun <T> success(data: T): ComicResource<T> {
            return ComicResource(Status.OK, data, null)
        }

        fun <T> error(throwable: Throwable? = null): ComicResource<T> {
            return ComicResource(Status.ERROR, null, HttpErrorParser.parseError(throwable))
        }

        fun <T> loading(): ComicResource<T> {
            return ComicResource(Status.LOADING, null, null)
        }

    }

}