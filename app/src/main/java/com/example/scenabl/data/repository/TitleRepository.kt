package com.example.scenabl.data.repository

import com.example.scenabl.data.model.Naslov
import com.example.scenabl.data.remote.ImgBbRemoteDataSource
import com.example.scenabl.data.remote.TitleRemoteDataSource
import kotlinx.coroutines.flow.Flow

class TitleRepository(
    private val remote: TitleRemoteDataSource,
    private val imgBbRemote: ImgBbRemoteDataSource
) {
    fun observeTitles(): Flow<List<Naslov>> = remote.observeTitles()

    fun observeTitle(id: String): Flow<Naslov?> = remote.observeTitle(id)

    suspend fun createTitle(naslov: Naslov): Result<String> = runCatching {
        remote.createTitle(naslov)
    }

    suspend fun updateTitle(id: String, updates: Map<String, Any?>): Result<Unit> = runCatching {
        remote.updateTitle(id, updates)
    }

    suspend fun uploadTitleImage(bytes: ByteArray, filename: String): Result<String> = runCatching {
        imgBbRemote.uploadImage(bytes, filename)
    }
}
