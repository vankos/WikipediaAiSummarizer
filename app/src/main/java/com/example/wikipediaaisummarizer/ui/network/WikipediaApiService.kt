// WikipediaApiService.kt
package com.yourpackage.wikisummarizer.network

import WikiResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WikipediaApiService {
    @GET("api.php")
    fun getArticleContent(
        @Query("format") format: String = "json",
        @Query("action") action: String = "query",
        @Query("prop") prop: String = "extracts",
        @Query("explaintext") explaintext: Boolean = true,
        @Query("redirects") redirects: Int = 1,
        @Query("titles") titles: String
    ): Call<WikiResponse>
}
