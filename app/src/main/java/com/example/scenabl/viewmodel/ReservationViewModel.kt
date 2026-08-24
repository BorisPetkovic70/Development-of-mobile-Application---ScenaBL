package com.example.scenabl.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scenabl.data.model.Izvodjenje
import com.example.scenabl.data.model.Naslov
import com.example.scenabl.data.repository.PerformanceRepository
import com.example.scenabl.data.repository.ReservationRepository
import com.example.scenabl.data.repository.TitleRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReservationUiState(
    val naslov: Naslov? = null,
    val performance: Izvodjenje? = null,
    val ticketCount: Int = 1,
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
) {
    val remainingSeats: Int
        get() = ((performance?.kapacitet ?: 0) - (performance?.rezervisano ?: 0)).coerceAtLeast(0)
}

@OptIn(ExperimentalCoroutinesApi::class)
class ReservationViewModel(
    private val performanceId: String,
    private val userId: String,
    private val reservationRepository: ReservationRepository,
    private val performanceRepository: PerformanceRepository,
    private val titleRepository: TitleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReservationUiState())
    val uiState: StateFlow<ReservationUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            performanceRepository.observePerformance(performanceId).collect { performance ->
                _uiState.update { it.copy(performance = performance, isLoading = false) }
            }
        }

        viewModelScope.launch {
            performanceRepository.observePerformance(performanceId)
                .map { it?.titleId }
                .filterNotNull()
                .distinctUntilChanged()
                .flatMapLatest { titleId -> titleRepository.observeTitle(titleId) }
                .collect { naslov -> _uiState.update { it.copy(naslov = naslov) } }
        }
    }

    fun increment() = _uiState.update { it.copy(ticketCount = (it.ticketCount + 1).coerceAtMost(10)) }

    fun decrement() = _uiState.update { it.copy(ticketCount = (it.ticketCount - 1).coerceAtLeast(1)) }

    fun confirmReservation() = viewModelScope.launch {
        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
        val ticketCount = _uiState.value.ticketCount
        reservationRepository.createReservation(userId, performanceId, ticketCount).fold(
            onSuccess = { _uiState.update { it.copy(isSubmitting = false, isSuccess = true) } },
            onFailure = { e -> _uiState.update { it.copy(isSubmitting = false, errorMessage = e.message ?: "Rezervacija nije uspjela.") } }
        )
    }
}
