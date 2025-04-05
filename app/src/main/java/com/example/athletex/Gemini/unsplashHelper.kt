// UnsplashApiHelper.kt
package com.example.athletex.Gemini

import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import com.google.gson.annotations.SerializedName

object unsplashHelper {

    private const val BASE_URL = "https://api.unsplash.com/"
    private const val CLIENT_ID = "_0hQCLI3DS4jNZvcxdHDU0_O2MV0IaVXj71FZc3Txbc" // Replace with your key

    private val api: UnsplashApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(UnsplashApi::class.java)

    fun fetchImageForMeal(mealName: String, callback: (String?) -> Unit) {
        api.searchPhotos(mealName, CLIENT_ID).enqueue(object : Callback<UnsplashResponse> {
            override fun onResponse(call: Call<UnsplashResponse>, response: Response<UnsplashResponse>) {
                val imageUrl = response.body()?.results?.firstOrNull()?.urls?.regular
                callback(imageUrl)
            }

            override fun onFailure(call: Call<UnsplashResponse>, t: Throwable) {
                callback(null)
            }
        })
    }
}

interface UnsplashApi {
    @GET("search/photos")
    fun searchPhotos(
        @Query("query") query: String,
        @Query("client_id") clientId: String
    ): Call<UnsplashResponse>
}

data class UnsplashResponse(
    val results: List<UnsplashPhoto>
)

data class UnsplashPhoto(
    val urls: UnsplashUrls
)

data class UnsplashUrls(
    val regular: String
)
