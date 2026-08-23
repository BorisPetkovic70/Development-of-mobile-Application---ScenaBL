package com.example.scenabl.data.repository

import com.example.scenabl.data.model.Institucija
import com.example.scenabl.data.model.Korisnik
import com.example.scenabl.data.remote.ImgBbRemoteDataSource
import com.example.scenabl.data.remote.InstitutionRemoteDataSource
import com.example.scenabl.data.remote.UserRemoteDataSource
import kotlinx.coroutines.flow.Flow

class UserRepository(
    private val userRemote: UserRemoteDataSource,
    private val institutionRemote: InstitutionRemoteDataSource,
    private val imgBbRemote: ImgBbRemoteDataSource
) {
    suspend fun createOrUpdateUser(korisnik: Korisnik): Result<Unit> = runCatching {
        userRemote.createOrUpdateUser(korisnik)
    }

    suspend fun getUser(uid: String): Result<Korisnik?> = runCatching {
        userRemote.getUser(uid)
    }

    fun observeUser(uid: String): Flow<Korisnik?> = userRemote.observeUser(uid)

    suspend fun updateProfile(uid: String, updates: Map<String, Any?>): Result<Unit> = runCatching {
        userRemote.updateFields(uid, updates)
    }

    suspend fun createInstitution(institucija: Institucija): Result<String> = runCatching {
        institutionRemote.createInstitution(institucija)
    }

    suspend fun getInstitutionByOwner(ownerUid: String): Result<Institucija?> = runCatching {
        institutionRemote.getInstitutionByOwner(ownerUid)
    }

    fun observeInstitution(id: String): Flow<Institucija?> = institutionRemote.observeInstitution(id)

    suspend fun uploadProfileImage(bytes: ByteArray, filename: String): Result<String> = runCatching {
        imgBbRemote.uploadImage(bytes, filename)
    }
}
