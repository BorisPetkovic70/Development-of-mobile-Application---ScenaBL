package com.example.scenabl.data.remote

import com.example.scenabl.data.model.KorisnickaLista
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Document id is `{userId}_{titleId}` so a title can never be in both
 * personal lists at once (REQ-LIST-003) — switching lists overwrites the
 * same document instead of needing a separate remove-then-add step.
 */
class UserListRemoteDataSource(private val firestore: FirebaseFirestore) {

    private val userLists get() = firestore.collection("userLists")

    private fun entryId(userId: String, titleId: String) = "${userId}_$titleId"

    suspend fun setListEntry(korisnickaLista: KorisnickaLista) {
        userLists.document(entryId(korisnickaLista.userId, korisnickaLista.titleId))
            .set(korisnickaLista).await()
    }

    suspend fun removeListEntry(userId: String, titleId: String) {
        userLists.document(entryId(userId, titleId)).delete().await()
    }

    fun observeUserLists(userId: String): Flow<List<KorisnickaLista>> = callbackFlow {
        val listener = userLists.whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(KorisnickaLista::class.java) ?: emptyList())
            }
        awaitClose { listener.remove() }
    }
}
