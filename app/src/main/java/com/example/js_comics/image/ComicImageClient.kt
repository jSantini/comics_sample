package com.example.js_comics.image

import okhttp3.OkHttpClient
import java.io.IOException
import java.util.concurrent.TimeUnit

/** Image transport only: never shares catalog credentials or follows redirects. */
internal fun comicImageClient(): OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(10, TimeUnit.SECONDS)
    .readTimeout(15, TimeUnit.SECONDS)
    .callTimeout(20, TimeUnit.SECONDS)
    .followRedirects(false)
    .followSslRedirects(false)
    .addInterceptor { chain ->
        val url = chain.request().url
        if (!url.isHttps || url.username.isNotEmpty() || url.password.isNotEmpty() ||
            url.query != null || url.fragment != null
        ) {
            throw IOException("Unsupported image URL")
        }
        chain.proceed(chain.request())
    }
    .build()
