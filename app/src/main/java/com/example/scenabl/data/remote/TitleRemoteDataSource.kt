package com.example.scenabl.data.remote

import com.example.scenabl.data.model.Naslov
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class TitleRemoteDataSource(private val firestore: FirebaseFirestore) {

    private val titles get() = firestore.collection("titles")

    fun observeTitles(): Flow<List<Naslov>> = callbackFlow {
        val listener = titles.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.toObjects(Naslov::class.java) ?: emptyList())
        }
        awaitClose { listener.remove() }
    }

    fun observeTitle(id: String): Flow<Naslov?> = callbackFlow {
        val listener = titles.document(id).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.toObject(Naslov::class.java))
        }
        awaitClose { listener.remove() }
    }

    suspend fun createTitle(naslov: Naslov): String = titles.add(naslov).await().id

    suspend fun updateTitle(id: String, updates: Map<String, Any?>) {
        titles.document(id).update(updates).await()
    }
}
