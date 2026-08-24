package com.example.scenabl.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scenabl.data.model.Izvodjenje
import com.example.scenabl.data.repository.PerformanceRepository
import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.Date
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PerformanceFormUiState(
    val date: LocalDate? = null,
    val time: LocalTime? = null,
    val sala: String = "",
    val kapacitetInput: String = "",
    val cijenaInput: String = "",
    val isSaving: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
) {
    val isFormValid: Boolean
        get() = date != null && time != null && sala.isNotBlank() &&
            (kapacitetInput.toIntOrNull()?.let { it > 0 } == true) &&
            (cijenaInput.toDoubleOrNull()?.let { it >= 0 } == true)
}

class OrganizerPerformanceFormViewModel(
    private val titleId: String,
    private val institutionId: String,
    private val performanceRepository: PerformanceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PerformanceFormUiState())
    val uiState: StateFlow<PerformanceFormUiState> = _uiState.asStateFlow()

    fun onDateSelected(date: LocalDate) = _uiState.update { it.copy(date = date, errorMessage = null) }
    fun onTimeSelected(time: LocalTime) = _uiState.update { it.copy(time = time, errorMessage = null) }
    fun onSalaChange(value: String) = _uiState.update { it.copy(sala = value) }
    fun onKapacitetChange(value: String) = _uiState.update { it.copy(kapacitetInput = value.filter(Char::isDigit)) }
    fun onCijenaChange(value: String) =
        _uiState.update { it.copy(cijenaInput = value.filter { c -> c.isDigit() || c == '.' }) }

    fun save() {
        val state = _uiState.value
        if (!state.isFormValid) {
            _uiState.update { it.copy(errorMessage = "Popunite datum, vrijeme, salu, kapacitet i cijenu.") }
            return
        }
        val dateTime = LocalDateTime.of(state.date, state.time)
        if (dateTime.isBefore(LocalDateTime.now())) {
            _uiState.update { it.copy(errorMessage = "Datum i vrijeme moraju biti u budućnosti.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val izvodjenje = Izvodjenje(
                titleId = titleId,
                institutionId = institutionId,
                datumVrijeme = Timestamp(Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant())),
                sala = state.sala.trim(),
                kapacitet = state.kapacitetInput.toInt(),
                cijena = state.cijenaInput.toDouble()
            )
            performanceRepository.createPerformance(izvodjenje).fold(
                onSuccess = { _uiState.update { it.copy(isSaving = false, isSuccess = true) } },
                onFailure = { e -> _uiState.update { it.copy(isSaving = false, errorMessage = e.message) } }
            )
        }
    }
}
