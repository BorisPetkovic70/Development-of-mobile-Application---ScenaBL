package com.example.scenabl.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.scenabl.data.repository.AuthRepository

/**
 * Temporary landing screen shown after login/guest access, until the real
 * HomeScreen (repertoire browsing, Phase 4) is implemented.
 */
@Composable
fun MainScreen(
    authRepository: AuthRepository,
    onProfileClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    val isLoggedIn = authRepository.currentUserId != null

    Scaffold { padding: PaddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "ScenaBL",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (isLoggedIn) "Uspješno ste prijavljeni." else "Gost mod (samo pregled) — repertoar dolazi uskoro.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            )

            if (isLoggedIn) {
                Button(onClick = onProfileClick, modifier = Modifier.fillMaxWidth()) {
                    Text("Profil")
                }
            } else {
                OutlinedButton(onClick = onLoginClick, modifier = Modifier.fillMaxWidth()) {
                    Text("Prijavi se")
                }
            }
        }
    }
}
