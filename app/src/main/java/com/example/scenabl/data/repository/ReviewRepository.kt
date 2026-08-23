package com.example.scenabl.data.repository

import com.example.scenabl.data.model.Recenzija
import com.example.scenabl.data.remote.ReviewRemoteDataSource
import kotlinx.coroutines.flow.Flow

class ReviewRepository(private val remote: ReviewRemoteDataSource) {

    suspend fun upsertReview(recenzija: Recenzija): Result<Unit> = runCatching {
        remote.upsertReview(recenzija)
    }

    suspend fun getReview(userId: String, titleId: String): Result<Recenzija?> = runCatching {
        remote.getReview(userId, titleId)
    }

    suspend fun deleteReview(userId: String, titleId: String): Result<Unit> = runCatching {
        remote.deleteReview(userId, titleId)
    }

    fun observeReviewsForTitle(titleId: String): Flow<List<Recenzija>> =
        remote.observeReviewsForTitle(titleId)
}
