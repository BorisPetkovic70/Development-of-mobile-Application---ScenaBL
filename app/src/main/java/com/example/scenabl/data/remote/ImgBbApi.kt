package com.example.scenabl.data.remote

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ImgBbApi {
    @Multipart
    @POST("1/upload")
    suspend fun uploadImage(
        @Query("key") apiKey: String,
        @Part image: MultipartBody.Part
    ): ImgBbUploadResponse
}

data class ImgBbUploadResponse(
    val data: ImgBbData? = null,
    val success: Boolean = false,
    val status: Int = 0
)

data class ImgBbData(
    val url: String? = null,
    @SerializedName("display_url")
    val displayUrl: String? = null
)
