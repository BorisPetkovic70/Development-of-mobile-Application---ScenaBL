package com.example.scenabl.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scenabl.data.model.Korisnik
import com.example.scenabl.data.repository.AuthRepository
import com.example.scenabl.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val korisnik: Korisnik? = null,
    val imeInput: String = "",
    val prezimeInput: String = "",
    val selectedGenres: Set<String> = emptySet(),
    val isUploadingImage: Boolean = false,
    val isSaving: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false
)

class ProfileViewModel(
    private val uid: String,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userRepository.observeUser(uid).collect { korisnik ->
                if (korisnik != null) {
                    _uiState.update {
                        it.copy(
                            korisnik = korisnik,
                            imeInput = korisnik.ime,
                            prezimeInput = korisnik.prezime,
                            selectedGenres = korisnik.favoriteGenres.toSet()
                        )
                    }
                }
            }
        }
    }

    fun onImeChange(value: String) = _uiState.update { it.copy(imeInput = value, message = null) }
    fun onPrezimeChange(value: String) = _uiState.update { it.copy(prezimeInput = value, message = null) }

    fun onGenreToggle(genre: String) = _uiState.update {
        val updated = if (genre in it.selectedGenres) it.selectedGenres - genre else it.selectedGenres + genre
        it.copy(selectedGenres = updated)
    }

    fun uploadProfileImage(bytes: ByteArray, filename: String) = viewModelScope.launch {
        _uiState.update { it.copy(isUploadingImage = true, message = null) }
        userRepository.uploadProfileImage(bytes, filename).fold(
            onSuccess = { url ->
                userRepository.updateProfile(uid, mapOf("profileImageUrl" to url)).fold(
                    onSuccess = { _uiState.update { it.copy(isUploadingImage = false) } },
                    onFailure = { e ->
                        _uiState.update {
                            it.copy(isUploadingImage = false, message = "Greška pri čuvanju slike: ${e.message}", isError = true)
                        }
                    }
                )
            },
            onFailure = { e ->
                _uiState.update {
                    it.copy(isUploadingImage = false, message = "Otpremanje slike nije uspjelo: ${e.message}", isError = true)
                }
            }
        )
    }

    fun saveProfile() = viewModelScope.launch {
        val state = _uiState.value
        if (state.imeInput.isBlank() || state.prezimeInput.isBlank()) {
            _uiState.update { it.copy(message = "Ime i prezime su obavezni.", isError = true) }
            return@launch
        }
        _uiState.update { it.copy(isSaving = true, message = null) }
        val updates = mapOf(
            "ime" to state.imeInput.trim(),
            "prezime" to state.prezimeInput.trim(),
            "favoriteGenres" to state.selectedGenres.toList()
        )
        userRepository.updateProfile(uid, updates).fold(
            onSuccess = { _uiState.update { it.copy(isSaving = false, message = "Profil je sačuvan.", isError = false) } },
            onFailure = { e ->
                _uiState.update { it.copy(isSaving = false, message = "Čuvanje nije uspjelo: ${e.message}", isError = true) }
            }
        )
    }

    fun logout() = authRepository.logout()
}
