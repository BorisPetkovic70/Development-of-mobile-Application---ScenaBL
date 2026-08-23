package com.example.scenabl.data.repository

import com.example.scenabl.data.remote.AuthRemoteDataSource
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

class AuthRepository(private val remote: AuthRemoteDataSource) {

    val currentUserId: String?
        get() = remote.currentUser?.uid

    fun authStateFlow(): Flow<FirebaseUser?> = remote.authStateFlow()

    suspend fun register(email: String, password: String): Result<FirebaseUser> = runCatching {
        remote.register(email, password)
    }

    suspend fun login(email: String, password: String): Result<FirebaseUser> = runCatching {
        remote.login(email, password)
    }

    fun logout() = remote.signOut()
}
