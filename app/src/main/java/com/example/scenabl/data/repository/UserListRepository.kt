package com.example.scenabl.data.repository

import com.example.scenabl.data.model.KorisnickaLista
import com.example.scenabl.data.remote.UserListRemoteDataSource
import kotlinx.coroutines.flow.Flow

class UserListRepository(private val remote: UserListRemoteDataSource) {

    suspend fun setListEntry(korisnickaLista: KorisnickaLista): Result<Unit> = runCatching {
        remote.setListEntry(korisnickaLista)
    }

    suspend fun removeListEntry(userId: String, titleId: String): Result<Unit> = runCatching {
        remote.removeListEntry(userId, titleId)
    }

    fun observeUserLists(userId: String): Flow<List<KorisnickaLista>> =
        remote.observeUserLists(userId)

    fun observeListEntry(userId: String, titleId: String): Flow<KorisnickaLista?> =
        remote.observeListEntry(userId, titleId)
}
