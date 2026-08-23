package com.example.scenabl.data.remote

import com.example.scenabl.data.model.Institucija
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class InstitutionRemoteDataSource(private val firestore: FirebaseFirestore) {

    private val institutions get() = firestore.collection("institutions")

    suspend fun createInstitution(institucija: Institucija): String =
        institutions.add(institucija).await().id

    suspend fun getInstitutionByOwner(ownerUid: String): Institucija? =
        institutions.whereEqualTo("ownerUid", ownerUid).limit(1).get().await()
            .documents.firstOrNull()?.toObject(Institucija::class.java)

    fun observeInstitution(id: String): Flow<Institucija?> = callbackFlow {
        val listener = institutions.document(id).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.toObject(Institucija::class.java))
        }
        awaitClose { listener.remove() }
    }

    suspend fun updateInstitution(id: String, updates: Map<String, Any?>) {
        institutions.document(id).update(updates).await()
    }
}
