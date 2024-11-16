// RetrofitInstance.kt
package com.yourpackage.wikisummarizer.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    val wikipediaApi: WikipediaApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://en.wikipedia.org/w/") // Base URL ending with /w/
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WikipediaApiService::class.java)
    }
}
