package com.example.scenabl.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.scenabl.data.model.UserRole
import com.example.scenabl.viewmodel.AuthMode
import com.example.scenabl.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onAuthenticated: () -> Unit,
    onGuestContinue: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            viewModel.consumeSuccess()
            onAuthenticated()
        }
    }

    Scaffold { padding: PaddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "ScenaBL",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (state.mode == AuthMode.LOGIN) "Prijava" else "Registracija",
                style = MaterialTheme.typography.titleMedium
            )

            if (state.mode == AuthMode.REGISTER) {
                OutlinedTextField(
                    value = state.ime,
                    onValueChange = viewModel::onImeChange,
                    label = { Text("Ime") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = state.prezime,
                    onValueChange = viewModel::onPrezimeChange,
                    label = { Text("Prezime") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = state.email,
                onValueChange = viewModel::onEmailChange,
                label = { Text("E-mail") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.password,
                onValueChange = viewModel::onPasswordChange,
                label = { Text("Lozinka") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            if (state.mode == AuthMode.REGISTER) {
                PasswordRequirementRow("Najmanje 8 karaktera", state.passwordHasMinLength)
                PasswordRequirementRow("Bar jedno veliko slovo", state.passwordHasUppercase)
                PasswordRequirementRow("Bar jedna cifra", state.passwordHasDigit)

                Text(text = "Uloga naloga", style = MaterialTheme.typography.labelLarge)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = state.role == UserRole.VIEWER,
                            onClick = { viewModel.onRoleSelected(UserRole.VIEWER) },
                            label = { Text("Gledalac") }
                        )
                        FilterChip(
                            selected = state.role == UserRole.ORGANIZER,
                            onClick = { viewModel.onRoleSelected(UserRole.ORGANIZER) },
                            label = { Text("Ustanova") }
                        )
                    }
                }
            }

            state.errorMessage?.let { message ->
                Text(text = message, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = viewModel::submit,
                enabled = !state.isLoading,
                colors = ButtonDefaults.buttonColors(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.padding(2.dp))
                } else {
                    Text(if (state.mode == AuthMode.LOGIN) "Prijavi se" else "Registruj se")
                }
            }

            TextButton(onClick = viewModel::onModeToggle, modifier = Modifier.fillMaxWidth()) {
                Text(
                    if (state.mode == AuthMode.LOGIN) "Nemate nalog? Registrujte se"
                    else "Već imate nalog? Prijavite se"
                )
            }

            TextButton(onClick = onGuestContinue, modifier = Modifier.fillMaxWidth()) {
                Text("Nastavi kao gost")
            }
        }
    }
}

@Composable
private fun PasswordRequirementRow(text: String, satisfied: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(
            imageVector = if (satisfied) Icons.Filled.Check else Icons.Filled.Close,
            contentDescription = null,
            tint = if (satisfied) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
        Text(text = text, style = MaterialTheme.typography.bodySmall)
    }
}
