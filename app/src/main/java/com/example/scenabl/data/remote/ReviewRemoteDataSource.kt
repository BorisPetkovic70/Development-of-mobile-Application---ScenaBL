package com.example.scenabl.data.remote

import com.example.scenabl.data.model.Recenzija
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Document id is `{userId}_{titleId}` so a user can only ever have one
 * review per title (REQ-REV-002) — a repeat write overwrites it in place.
 */
class ReviewRemoteDataSource(private val firestore: FirebaseFirestore) {

    private val reviews get() = firestore.collection("reviews")

    private fun reviewId(userId: String, titleId: String) = "${userId}_$titleId"

    suspend fun upsertReview(recenzija: Recenzija) {
        reviews.document(reviewId(recenzija.userId, recenzija.titleId)).set(recenzija).await()
    }

    suspend fun getReview(userId: String, titleId: String): Recenzija? =
        reviews.document(reviewId(userId, titleId)).get().await().toObject(Recenzija::class.java)

    suspend fun deleteReview(userId: String, titleId: String) {
        reviews.document(reviewId(userId, titleId)).delete().await()
    }

    fun observeReviewsForTitle(titleId: String): Flow<List<Recenzija>> = callbackFlow {
        val listener = reviews.whereEqualTo("titleId", titleId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(Recenzija::class.java) ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    /** Used by HomeScreen to compute each title's average rating (REQ-BROW-001) without one query per title. */
    fun observeAllReviews(): Flow<List<Recenzija>> = callbackFlow {
        val listener = reviews.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.toObjects(Recenzija::class.java) ?: emptyList())
        }
        awaitClose { listener.remove() }
    }
}
