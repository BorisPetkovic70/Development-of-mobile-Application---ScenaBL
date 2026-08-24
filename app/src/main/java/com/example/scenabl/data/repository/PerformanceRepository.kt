package com.example.scenabl.data.repository

import com.example.scenabl.data.model.Izvodjenje
import com.example.scenabl.data.model.PerformanceStatus
import com.example.scenabl.data.remote.PerformanceRemoteDataSource
import kotlinx.coroutines.flow.Flow

class PerformanceRepository(private val remote: PerformanceRemoteDataSource) {

    fun observePerformancesForTitle(titleId: String): Flow<List<Izvodjenje>> =
        remote.observePerformancesForTitle(titleId)

    fun observeUpcomingPerformances(): Flow<List<Izvodjenje>> = remote.observeUpcomingPerformances()

    fun observePerformance(id: String): Flow<Izvodjenje?> = remote.observePerformance(id)

    suspend fun createPerformance(izvodjenje: Izvodjenje): Result<String> = runCatching {
        remote.createPerformance(izvodjenje)
    }

    suspend fun cancelPerformance(id: String): Result<Unit> = runCatching {
        remote.updatePerformance(id, mapOf("status" to PerformanceStatus.CANCELLED))
    }
}
