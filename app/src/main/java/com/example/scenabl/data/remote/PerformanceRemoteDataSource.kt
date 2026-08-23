package com.example.scenabl.data.remote

import com.example.scenabl.data.model.Izvodjenje
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class PerformanceRemoteDataSource(private val firestore: FirebaseFirestore) {

    private val performances get() = firestore.collection("performances")

    fun observePerformancesForTitle(titleId: String): Flow<List<Izvodjenje>> = callbackFlow {
        val listener = performances.whereEqualTo("titleId", titleId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(Izvodjenje::class.java) ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    fun observePerformance(id: String): Flow<Izvodjenje?> = callbackFlow {
        val listener = performances.document(id).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.toObject(Izvodjenje::class.java))
        }
        awaitClose { listener.remove() }
    }

    suspend fun createPerformance(izvodjenje: Izvodjenje): String =
        performances.add(izvodjenje).await().id

    suspend fun updatePerformance(id: String, updates: Map<String, Any?>) {
        performances.document(id).update(updates).await()
    }
}
