package com.example.scenabl.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scenabl.data.model.Genres
import com.example.scenabl.data.model.Naslov
import com.example.scenabl.data.model.TitleType
import com.example.scenabl.data.repository.TitleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TitleFormUiState(
    val naziv: String = "",
    val opis: String = "",
    val reziser: String = "",
    val trajanjeInput: String = "",
    val zanr: String = Genres.ALL.first(),
    val tip: String = TitleType.POZORISTE,
    val slikaUrl: String = "",
    val isUploadingImage: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
) {
    /** naziv, žanr/tip (always set), trajanje and slika are required (REQ-ORG-001). */
    val isFormValid: Boolean
        get() = naziv.isNotBlank() && slikaUrl.isNotBlank() && (trajanjeInput.toIntOrNull()?.let { it > 0 } == true)
}

class OrganizerTitleFormViewModel(
    private val titleId: String?,
    private val institutionId: String,
    private val titleRepository: TitleRepository
) : ViewModel() {

    val isEditMode: Boolean get() = titleId != null

    private val _uiState = MutableStateFlow(TitleFormUiState(isLoading = isEditMode))
    val uiState: StateFlow<TitleFormUiState> = _uiState.asStateFlow()

    init {
        if (titleId != null) {
            viewModelScope.launch {
                val naslov = titleRepository.observeTitle(titleId).first()
                if (naslov != null) {
                    _uiState.update {
                        it.copy(
                            naziv = naslov.naziv,
                            opis = naslov.opis,
                            reziser = naslov.reziser,
                            trajanjeInput = naslov.trajanje.toString(),
                            zanr = naslov.zanr,
                            tip = naslov.tip,
                            slikaUrl = naslov.slikaUrl,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Naslov nije pronađen.") }
                }
            }
        }
    }

    fun onNazivChange(value: String) = _uiState.update { it.copy(naziv = value, errorMessage = null) }
    fun onOpisChange(value: String) = _uiState.update { it.copy(opis = value) }
    fun onReziserChange(value: String) = _uiState.update { it.copy(reziser = value) }
    fun onTrajanjeChange(value: String) = _uiState.update { it.copy(trajanjeInput = value.filter(Char::isDigit)) }
    fun onZanrChange(value: String) = _uiState.update { it.copy(zanr = value) }
    fun onTipChange(value: String) = _uiState.update { it.copy(tip = value) }

    fun uploadImage(bytes: ByteArray, filename: String) = viewModelScope.launch {
        _uiState.update { it.copy(isUploadingImage = true, errorMessage = null) }
        titleRepository.uploadTitleImage(bytes, filename).fold(
            onSuccess = { url -> _uiState.update { it.copy(isUploadingImage = false, slikaUrl = url) } },
            onFailure = { e ->
                _uiState.update { it.copy(isUploadingImage = false, errorMessage = "Otpremanje slike nije uspjelo: ${e.message}") }
            }
        )
    }

    fun save() {
        val state = _uiState.value
        if (!state.isFormValid) {
            _uiState.update { it.copy(errorMessage = "Popunite naziv, trajanje i sliku prije čuvanja.") }
            return
        }
        val trajanje = state.trajanjeInput.toInt()
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val result = if (titleId == null) {
                titleRepository.createTitle(
                    Naslov(
                        naziv = state.naziv.trim(),
                        opis = state.opis.trim(),
                        reziser = state.reziser.trim(),
                        trajanje = trajanje,
                        zanr = state.zanr,
                        tip = state.tip,
                        slikaUrl = state.slikaUrl,
                        institutionId = institutionId
                    )
                ).map { }
            } else {
                titleRepository.updateTitle(
                    titleId,
                    mapOf(
                        "naziv" to state.naziv.trim(),
                        "opis" to state.opis.trim(),
                        "reziser" to state.reziser.trim(),
                        "trajanje" to trajanje,
                        "zanr" to state.zanr,
                        "tip" to state.tip,
                        "slikaUrl" to state.slikaUrl
                    )
                )
            }
            result.fold(
                onSuccess = { _uiState.update { it.copy(isSaving = false, isSuccess = true) } },
                onFailure = { e -> _uiState.update { it.copy(isSaving = false, errorMessage = e.message) } }
            )
        }
    }
}
