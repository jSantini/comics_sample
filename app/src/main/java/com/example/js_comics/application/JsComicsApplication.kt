package com.example.js_comics.application

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.example.js_comics.image.comicImageClient
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class JsComicsApplication : Application(), ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader = ImageLoader.Builder(this)
        .okHttpClient { comicImageClient() }
        .build()
}
