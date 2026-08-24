package com.example.scenabl.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.scenabl.data.model.Genres
import com.example.scenabl.data.model.UserRole
import com.example.scenabl.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onLoggedOut: () -> Unit,
    onMyListsClick: () -> Unit,
    onMyReservationsClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            if (bytes != null) {
                viewModel.uploadProfileImage(bytes, "profile_${System.currentTimeMillis()}.jpg")
            }
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
            Text(text = "Profil", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                val imageUrl = state.korisnik?.profileImageUrl
                if (imageUrl != null) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Profilna slika",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(imageVector = Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(48.dp))
                }
                if (state.isUploadingImage) {
                    CircularProgressIndicator()
                }
            }
            Text(text = "Dodirnite sliku za promjenu", style = MaterialTheme.typography.bodySmall)

            OutlinedTextField(
                value = state.imeInput,
                onValueChange = viewModel::onImeChange,
                label = { Text("Ime") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.prezimeInput,
                onValueChange = viewModel::onPrezimeChange,
                label = { Text("Prezime") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text(text = "E-mail: ${state.korisnik?.email.orEmpty()}", style = MaterialTheme.typography.bodyMedium)

            if (state.korisnik?.role == UserRole.VIEWER) {
                Text(text = "Omiljeni žanrovi", style = MaterialTheme.typography.labelLarge)
                GenreChips(
                    selected = state.selectedGenres,
                    onToggle = viewModel::onGenreToggle
                )

                OutlinedButton(onClick = onMyListsClick, modifier = Modifier.fillMaxWidth()) {
                    Text("Moje liste")
                }
                OutlinedButton(onClick = onMyReservationsClick, modifier = Modifier.fillMaxWidth()) {
                    Text("Moje rezervacije")
                }
            }

            state.message?.let { message ->
                Text(
                    text = message,
                    color = if (state.isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }

            Button(
                onClick = viewModel::saveProfile,
                enabled = !state.isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text("Sačuvaj")
                }
            }

            OutlinedButton(
                onClick = {
                    viewModel.logout()
                    onLoggedOut()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Odjava")
            }
        }
    }
}

@Composable
private fun GenreChips(
    selected: Set<String>,
    onToggle: (String) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Genres.ALL.forEach { genre ->
            FilterChip(
                selected = genre in selected,
                onClick = { onToggle(genre) },
                label = { Text(genre) }
            )
        }
    }
}
