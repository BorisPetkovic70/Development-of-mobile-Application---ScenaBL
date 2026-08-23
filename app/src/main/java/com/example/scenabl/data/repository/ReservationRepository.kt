package com.example.scenabl.data.repository

import com.example.scenabl.data.model.Rezervacija
import com.example.scenabl.data.remote.ReservationRemoteDataSource
import kotlinx.coroutines.flow.Flow

class ReservationRepository(private val remote: ReservationRemoteDataSource) {

    suspend fun createReservation(
        userId: String,
        performanceId: String,
        brojKarata: Int
    ): Result<String> = runCatching {
        remote.createReservation(userId, performanceId, brojKarata)
    }

    suspend fun cancelReservation(reservationId: String): Result<Unit> = runCatching {
        remote.cancelReservation(reservationId)
    }

    fun observeUserReservations(userId: String): Flow<List<Rezervacija>> =
        remote.observeUserReservations(userId)
}
