package com.example.scenabl.data.remote

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class ImgBbRemoteDataSource(
    private val api: ImgBbApi,
    private val apiKey: String
) {
    suspend fun uploadImage(bytes: ByteArray, filename: String): String {
        val requestBody = bytes.toRequestBody("image/*".toMediaType())
        val part = MultipartBody.Part.createFormData("image", filename, requestBody)
        val response = api.uploadImage(apiKey, part)
        return response.data?.url
            ?: error("ImgBB upload nije uspio (success=${response.success})")
    }
}
