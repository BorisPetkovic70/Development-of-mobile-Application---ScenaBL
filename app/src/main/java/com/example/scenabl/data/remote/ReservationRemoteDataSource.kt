package com.example.scenabl.data.remote

import com.example.scenabl.data.model.Izvodjenje
import com.example.scenabl.data.model.ReservationStatus
import com.example.scenabl.data.model.Rezervacija
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Reservation creation/cancellation run as Firestore transactions so the
 * capacity check and the `rezervisano` counter update on the performance
 * document stay atomic under concurrent reservations (REQ-RES-002/003).
 */
class ReservationRemoteDataSource(private val firestore: FirebaseFirestore) {

    private val reservations get() = firestore.collection("reservations")
    private val performances get() = firestore.collection("performances")

    suspend fun createReservation(userId: String, performanceId: String, brojKarata: Int): String {
        val reservationRef = reservations.document()
        val performanceRef = performances.document(performanceId)
        firestore.runTransaction { transaction ->
            val izvodjenje = transaction.get(performanceRef).toObject(Izvodjenje::class.java)
                ?: error("Izvođenje ne postoji")
            val preostalo = izvodjenje.kapacitet - izvodjenje.rezervisano
            if (preostalo < brojKarata) {
                error("Nema dovoljno slobodnih mjesta (dostupno: $preostalo)")
            }
            val rezervacija = Rezervacija(
                userId = userId,
                performanceId = performanceId,
                brojKarata = brojKarata,
                status = ReservationStatus.ACTIVE
            )
            transaction.set(reservationRef, rezervacija)
            transaction.update(performanceRef, "rezervisano", izvodjenje.rezervisano + brojKarata)
        }.await()
        return reservationRef.id
    }

    suspend fun cancelReservation(reservationId: String) {
        val reservationRef = reservations.document(reservationId)
        firestore.runTransaction { transaction ->
            val rezervacija = transaction.get(reservationRef).toObject(Rezervacija::class.java)
                ?: error("Rezervacija ne postoji")
            if (rezervacija.status != ReservationStatus.ACTIVE) {
                error("Rezervacija više nije aktivna")
            }
            val performanceRef = performances.document(rezervacija.performanceId)
            val izvodjenje = transaction.get(performanceRef).toObject(Izvodjenje::class.java)
                ?: error("Izvođenje ne postoji")

            transaction.update(reservationRef, "status", ReservationStatus.CANCELLED)
            transaction.update(
                performanceRef,
                "rezervisano",
                (izvodjenje.rezervisano - rezervacija.brojKarata).coerceAtLeast(0)
            )
        }.await()
    }

    fun observeUserReservations(userId: String): Flow<List<Rezervacija>> = callbackFlow {
        val listener = reservations.whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(Rezervacija::class.java) ?: emptyList())
            }
        awaitClose { listener.remove() }
    }
}
