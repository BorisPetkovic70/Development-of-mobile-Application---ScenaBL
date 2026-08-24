package com.example.scenabl.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scenabl.data.model.Izvodjenje
import com.example.scenabl.data.model.Naslov
import com.example.scenabl.data.model.ReservationStatus
import com.example.scenabl.data.model.Rezervacija
import com.example.scenabl.data.repository.PerformanceRepository
import com.example.scenabl.data.repository.ReservationRepository
import com.example.scenabl.data.repository.TitleRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val CANCELLATION_CUTOFF_SECONDS = 2 * 60 * 60L

data class ReservationItem(
    val rezervacija: Rezervacija,
    val performance: Izvodjenje?,
    val naslov: Naslov?
) {
    /** Cancellable while active and more than 2 hours remain before showtime (REQ-RES-004). */
    val canCancel: Boolean
        get() = rezervacija.status == ReservationStatus.ACTIVE &&
            performance != null &&
            Timestamp.now().seconds < performance.datumVrijeme.seconds - CANCELLATION_CUTOFF_SECONDS
}

data class MyReservationsUiState(
    val reservations: List<ReservationItem> = emptyList(),
    val isLoading: Boolean = true,
    val message: String? = null
)

class MyReservationsViewModel(
    private val userId: String,
    private val reservationRepository: ReservationRepository,
    private val performanceRepository: PerformanceRepository,
    private val titleRepository: TitleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyReservationsUiState())
    val uiState: StateFlow<MyReservationsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                reservationRepository.observeUserReservations(userId),
                performanceRepository.observeAllPerformances(),
                titleRepository.observeTitles()
            ) { reservations, performances, titles ->
                val performanceById = performances.associateBy { it.id }
                val titleById = titles.associateBy { it.id }
                reservations
                    .sortedByDescending { it.datumKreiranja }
                    .map { rezervacija ->
                        val performance = performanceById[rezervacija.performanceId]
                        ReservationItem(
                            rezervacija = rezervacija,
                            performance = performance,
                            naslov = performance?.let { titleById[it.titleId] }
                        )
                    }
            }
                .catch { e -> _uiState.update { it.copy(isLoading = false, message = e.message) } }
                .collect { items -> _uiState.update { it.copy(reservations = items, isLoading = false) } }
        }
    }

    fun cancelReservation(reservationId: String) = viewModelScope.launch {
        reservationRepository.cancelReservation(reservationId).fold(
            onSuccess = { _uiState.update { it.copy(message = "Rezervacija je otkazana.") } },
            onFailure = { e -> _uiState.update { it.copy(message = "Otkazivanje nije uspjelo: ${e.message}") } }
        )
    }

    fun consumeMessage() = _uiState.update { it.copy(message = null) }
}
