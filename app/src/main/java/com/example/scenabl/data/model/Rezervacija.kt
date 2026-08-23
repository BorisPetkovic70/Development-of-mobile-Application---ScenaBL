package com.example.scenabl.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Rezervacija(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val performanceId: String = "",
    val brojKarata: Int = 1,
    val status: String = ReservationStatus.ACTIVE,
    val datumKreiranja: Timestamp = Timestamp.now()
)

object ReservationStatus {
    const val ACTIVE = "active"
    const val CANCELLED = "cancelled"
}
