package com.example.js_comics.data.di

import com.example.js_comics.data.BuildConfig
import com.example.js_comics.data.remote.ComicsAPI
import com.example.js_comics.data.repository.DemoComicsRepository
import com.example.js_comics.data.repository.RemoteComicsRepository
import com.example.js_comics.domain.ComicsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideRepository(): ComicsRepository {
        if (BuildConfig.COMICS_BASE_URL.isBlank()) return DemoComicsRepository()
        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .callTimeout(20, TimeUnit.SECONDS)
            .followRedirects(false)
            .followSslRedirects(false)
            .build()
        val api = Retrofit.Builder()
            .baseUrl(BuildConfig.COMICS_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(ComicsAPI::class.java)
        return RemoteComicsRepository(api)
    }
}
