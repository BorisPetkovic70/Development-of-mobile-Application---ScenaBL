package com.example.scenabl.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scenabl.data.model.Institucija
import com.example.scenabl.data.model.Izvodjenje
import com.example.scenabl.data.model.Naslov
import com.example.scenabl.data.repository.PerformanceRepository
import com.example.scenabl.data.repository.TitleRepository
import com.example.scenabl.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OrganizerUiState(
    val institution: Institucija? = null,
    val needsInstitutionSetup: Boolean = false,
    val institutionNameInput: String = "",
    val institutionDescriptionInput: String = "",
    val isSavingInstitution: Boolean = false,
    val titles: List<Naslov> = emptyList(),
    val performancesByTitle: Map<String, List<Izvodjenje>> = emptyMap(),
    val isLoading: Boolean = true,
    val message: String? = null,
    val isError: Boolean = false
)

/** Dashboard for organizer accounts: repertoire (title/performance CRUD) and a reservation
 * overview per performance (REQ-ORG-001 to REQ-ORG-004). An organizer's institution is created
 * lazily on first visit, since there's no separate registration step for it. */
class OrganizerViewModel(
    private val uid: String,
    private val userRepository: UserRepository,
    private val titleRepository: TitleRepository,
    private val performanceRepository: PerformanceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrganizerUiState())
    val uiState: StateFlow<OrganizerUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { loadInstitution() }
    }

    private suspend fun loadInstitution() {
        userRepository.getInstitutionByOwner(uid).fold(
            onSuccess = { institucija ->
                if (institucija == null) {
                    _uiState.update { it.copy(needsInstitutionSetup = true, isLoading = false) }
                } else {
                    _uiState.update { it.copy(institution = institucija, needsInstitutionSetup = false) }
                    observeOwnData(institucija.id)
                }
            },
            onFailure = { e -> _uiState.update { it.copy(isLoading = false, message = e.message, isError = true) } }
        )
    }

    private fun observeOwnData(institutionId: String) {
        viewModelScope.launch {
            combine(
                titleRepository.observeTitles(),
                performanceRepository.observeAllPerformances()
            ) { titles, performances ->
                val ownTitles = titles.filter { it.institutionId == institutionId }
                val ownTitleIds = ownTitles.map { it.id }.toSet()
                ownTitles to performances.filter { it.titleId in ownTitleIds }.groupBy { it.titleId }
            }
                .catch { e -> _uiState.update { it.copy(isLoading = false, message = e.message, isError = true) } }
                .collect { (titles, grouped) ->
                    _uiState.update { it.copy(titles = titles, performancesByTitle = grouped, isLoading = false) }
                }
        }
    }

    fun onInstitutionNameChange(value: String) = _uiState.update { it.copy(institutionNameInput = value) }
    fun onInstitutionDescriptionChange(value: String) = _uiState.update { it.copy(institutionDescriptionInput = value) }

    fun setupInstitution() {
        val state = _uiState.value
        if (state.institutionNameInput.isBlank()) {
            _uiState.update { it.copy(message = "Naziv ustanove je obavezan.", isError = true) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingInstitution = true, message = null) }
            val institucija = Institucija(
                naziv = state.institutionNameInput.trim(),
                opis = state.institutionDescriptionInput.trim(),
                ownerUid = uid
            )
            userRepository.createInstitution(institucija).fold(
                onSuccess = { id ->
                    val created = institucija.copy(id = id)
                    _uiState.update {
                        it.copy(isSavingInstitution = false, needsInstitutionSetup = false, institution = created)
                    }
                    observeOwnData(id)
                },
                onFailure = { e -> _uiState.update { it.copy(isSavingInstitution = false, message = e.message, isError = true) } }
            )
        }
    }

    /** Cancelling sets status=cancelled rather than deleting, preserving existing reservations (REQ-ORG-004). */
    fun cancelPerformance(id: String) = viewModelScope.launch {
        performanceRepository.cancelPerformance(id).fold(
            onSuccess = { _uiState.update { it.copy(message = "Izvođenje je otkazano.", isError = false) } },
            onFailure = { e -> _uiState.update { it.copy(message = "Otkazivanje nije uspjelo: ${e.message}", isError = true) } }
        )
    }

    fun consumeMessage() = _uiState.update { it.copy(message = null) }
}
