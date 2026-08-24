package com.example.scenabl.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scenabl.data.model.Korisnik
import com.example.scenabl.data.model.UserRole
import com.example.scenabl.data.repository.AuthRepository
import com.example.scenabl.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AuthMode { LOGIN, REGISTER }

data class AuthUiState(
    val mode: AuthMode = AuthMode.LOGIN,
    val ime: String = "",
    val prezime: String = "",
    val email: String = "",
    val password: String = "",
    val role: String = UserRole.VIEWER,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
) {
    val passwordHasMinLength: Boolean get() = password.length >= 8
    val passwordHasUppercase: Boolean get() = password.any { it.isUpperCase() }
    val passwordHasDigit: Boolean get() = password.any { it.isDigit() }
    val isPasswordValid: Boolean get() = passwordHasMinLength && passwordHasUppercase && passwordHasDigit

    val isFormValid: Boolean
        get() = if (mode == AuthMode.REGISTER) {
            ime.isNotBlank() && prezime.isNotBlank() && email.isNotBlank() && isPasswordValid
        } else {
            email.isNotBlank() && password.isNotBlank()
        }
}

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onModeToggle() {
        _uiState.update {
            it.copy(
                mode = if (it.mode == AuthMode.LOGIN) AuthMode.REGISTER else AuthMode.LOGIN,
                errorMessage = null
            )
        }
    }

    fun onImeChange(value: String) = _uiState.update { it.copy(ime = value, errorMessage = null) }
    fun onPrezimeChange(value: String) = _uiState.update { it.copy(prezime = value, errorMessage = null) }
    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, errorMessage = null) }
    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, errorMessage = null) }
    fun onRoleSelected(role: String) = _uiState.update { it.copy(role = role) }

    fun submit() {
        val state = _uiState.value
        if (!state.isFormValid) {
            _uiState.update { it.copy(errorMessage = "Popunite sva polja ispravno prije nastavka.") }
            return
        }
        if (state.mode == AuthMode.REGISTER) register(state) else login(state)
    }

    fun consumeSuccess() = _uiState.update { it.copy(isSuccess = false) }

    private fun register(state: AuthUiState) = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        val authResult = authRepository.register(state.email.trim(), state.password)
        authResult.fold(
            onSuccess = { firebaseUser ->
                val korisnik = Korisnik(
                    uid = firebaseUser.uid,
                    ime = state.ime.trim(),
                    prezime = state.prezime.trim(),
                    email = state.email.trim(),
                    role = state.role
                )
                userRepository.createOrUpdateUser(korisnik).fold(
                    onSuccess = { _uiState.update { it.copy(isLoading = false, isSuccess = true) } },
                    onFailure = { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Nalog je kreiran, ali čuvanje profila nije uspjelo: ${e.message}"
                            )
                        }
                    }
                )
            },
            onFailure = { e -> _uiState.update { it.copy(isLoading = false, errorMessage = mapAuthError(e)) } }
        )
    }

    private fun login(state: AuthUiState) = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        val result = authRepository.login(state.email.trim(), state.password)
        result.fold(
            onSuccess = { _uiState.update { it.copy(isLoading = false, isSuccess = true) } },
            onFailure = { e -> _uiState.update { it.copy(isLoading = false, errorMessage = mapAuthError(e)) } }
        )
    }

    private fun mapAuthError(e: Throwable): String = when (e) {
        is FirebaseAuthUserCollisionException -> "Ovaj e-mail je već registrovan."
        is FirebaseAuthWeakPasswordException -> "Lozinka je preslaba."
        is FirebaseAuthInvalidCredentialsException -> "Neispravan e-mail ili lozinka."
        is FirebaseAuthInvalidUserException -> "Nalog sa ovim e-mailom ne postoji."
        else -> e.message?.let { "Greška: $it" } ?: "Došlo je do greške. Pokušajte ponovo."
    }
}
