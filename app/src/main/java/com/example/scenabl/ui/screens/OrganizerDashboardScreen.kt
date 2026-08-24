package com.example.scenabl.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.scenabl.data.model.Izvodjenje
import com.example.scenabl.data.model.Naslov
import com.example.scenabl.data.model.PerformanceStatus
import com.example.scenabl.ui.util.ObserveSnackbarMessage
import com.example.scenabl.ui.util.formatDateTime
import com.example.scenabl.viewmodel.OrganizerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizerDashboardScreen(
    viewModel: OrganizerViewModel,
    onCreateTitle: (institutionId: String) -> Unit,
    onEditTitle: (institutionId: String, titleId: String) -> Unit,
    onAddPerformance: (institutionId: String, titleId: String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    ObserveSnackbarMessage(state.message, snackbarHostState, viewModel::consumeMessage)

    Scaffold(
        topBar = { TopAppBar(title = { Text(state.institution?.naziv ?: "Organizator") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when {
            state.isLoading -> Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            state.needsInstitutionSetup -> InstitutionSetupForm(
                nameInput = state.institutionNameInput,
                descriptionInput = state.institutionDescriptionInput,
                isSaving = state.isSavingInstitution,
                onNameChange = viewModel::onInstitutionNameChange,
                onDescriptionChange = viewModel::onInstitutionDescriptionChange,
                onSave = viewModel::setupInstitution,
                modifier = Modifier.fillMaxSize().padding(padding)
            )

            else -> {
                val institutionId = state.institution?.id.orEmpty()
                Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                    Button(
                        onClick = { onCreateTitle(institutionId) },
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    ) {
                        Text("Novi naslov")
                    }

                    if (state.titles.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Još nemate kreiranih naslova.", modifier = Modifier.padding(24.dp))
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.titles, key = { it.id }) { naslov ->
                                TitleManagementCard(
                                    naslov = naslov,
                                    performances = state.performancesByTitle[naslov.id].orEmpty(),
                                    onEdit = { onEditTitle(institutionId, naslov.id) },
                                    onAddPerformance = { onAddPerformance(institutionId, naslov.id) },
                                    onCancelPerformance = viewModel::cancelPerformance
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InstitutionSetupForm(
    nameInput: String,
    descriptionInput: String,
    isSaving: Boolean,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(text = "Podaci o ustanovi", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            text = "Prije kreiranja repertoara unesite osnovne podatke o vašoj ustanovi (pozorište ili bioskop).",
            style = MaterialTheme.typography.bodyMedium
        )
        OutlinedTextField(
            value = nameInput,
            onValueChange = onNameChange,
            label = { Text("Naziv ustanove") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = descriptionInput,
            onValueChange = onDescriptionChange,
            label = { Text("Opis (opciono)") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = onSave, enabled = !isSaving, modifier = Modifier.fillMaxWidth()) {
            if (isSaving) CircularProgressIndicator(modifier = Modifier.padding(2.dp)) else Text("Sačuvaj")
        }
    }
}

@Composable
private fun TitleManagementCard(
    naslov: Naslov,
    performances: List<Izvodjenje>,
    onEdit: () -> Unit,
    onAddPerformance: () -> Unit,
    onCancelPerformance: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = naslov.naziv, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = "${naslov.zanr} • ${naslov.trajanje} min", style = MaterialTheme.typography.bodySmall)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onEdit) { Text("Uredi naslov") }
                OutlinedButton(onClick = onAddPerformance) { Text("Novo izvođenje") }
            }

            if (performances.isNotEmpty()) {
                HorizontalDivider()
                performances.sortedBy { it.datumVrijeme }.forEach { performance ->
                    PerformanceManagementRow(performance, onCancel = { onCancelPerformance(performance.id) })
                }
            }
        }
    }
}

@Composable
private fun PerformanceManagementRow(performance: Izvodjenje, onCancel: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = performance.datumVrijeme.formatDateTime(), style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "Sala: ${performance.sala} • Rezervisano: ${performance.rezervisano}/${performance.kapacitet}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = if (performance.status == PerformanceStatus.CANCELLED) "Otkazano" else "Zakazano",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (performance.status == PerformanceStatus.CANCELLED) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
            }
            if (performance.status == PerformanceStatus.SCHEDULED) {
                OutlinedButton(onClick = onCancel) { Text("Otkaži") }
            }
        }
    }
}
