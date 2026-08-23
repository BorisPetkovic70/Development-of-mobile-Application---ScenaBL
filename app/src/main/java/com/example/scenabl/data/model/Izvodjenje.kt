package com.example.scenabl.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Izvodjenje(
    @DocumentId
    val id: String = "",
    val titleId: String = "",
    val institutionId: String = "",
    val datumVrijeme: Timestamp = Timestamp.now(),
    val sala: String = "",
    val kapacitet: Int = 0,
    val rezervisano: Int = 0,
    val cijena: Double = 0.0,
    val status: String = PerformanceStatus.SCHEDULED
)

object PerformanceStatus {
    const val SCHEDULED = "scheduled"
    const val CANCELLED = "cancelled"
}
