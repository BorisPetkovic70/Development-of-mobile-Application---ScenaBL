package com.example.scenabl.data.remote

import com.example.scenabl.data.model.Korisnik
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class UserRemoteDataSource(private val firestore: FirebaseFirestore) {

    private val users get() = firestore.collection("users")

    suspend fun createOrUpdateUser(korisnik: Korisnik) {
        users.document(korisnik.uid).set(korisnik).await()
    }

    suspend fun getUser(uid: String): Korisnik? =
        users.document(uid).get().await().toObject(Korisnik::class.java)

    fun observeUser(uid: String): Flow<Korisnik?> = callbackFlow {
        val listener = users.document(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.toObject(Korisnik::class.java))
        }
        awaitClose { listener.remove() }
    }

    suspend fun updateFields(uid: String, updates: Map<String, Any?>) {
        users.document(uid).update(updates).await()
    }
}
