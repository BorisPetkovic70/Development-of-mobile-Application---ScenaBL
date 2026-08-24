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
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.scenabl.data.model.ReservationStatus
import com.example.scenabl.ui.util.ObserveSnackbarMessage
import com.example.scenabl.ui.util.formatDateTime
import com.example.scenabl.viewmodel.MyReservationsViewModel
import com.example.scenabl.viewmodel.ReservationItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyReservationsScreen(viewModel: MyReservationsViewModel) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    ObserveSnackbarMessage(state.message, snackbarHostState, viewModel::consumeMessage)

    Scaffold(
        topBar = { TopAppBar(title = { Text("Moje rezervacije") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when {
            state.isLoading -> Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            state.reservations.isEmpty() -> Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Nemate rezervacija.", modifier = Modifier.padding(24.dp))
            }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.reservations, key = { it.rezervacija.id }) { item ->
                    ReservationRow(item = item, onCancel = { viewModel.cancelReservation(item.rezervacija.id) })
                }
            }
        }
    }
}

@Composable
private fun ReservationRow(item: ReservationItem, onCancel: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = item.naslov?.naziv ?: "Nepoznat naslov",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            item.performance?.let { performance ->
                Text(text = performance.datumVrijeme.formatDateTime(), style = MaterialTheme.typography.bodyMedium)
                Text(text = "Sala: ${performance.sala}", style = MaterialTheme.typography.bodySmall)
            }
            Text(text = "Broj karata: ${item.rezervacija.brojKarata}", style = MaterialTheme.typography.bodySmall)
            Text(
                text = statusLabel(item.rezervacija.status),
                style = MaterialTheme.typography.bodySmall,
                color = if (item.rezervacija.status == ReservationStatus.ACTIVE) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )

            if (item.rezervacija.status == ReservationStatus.ACTIVE) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(onClick = onCancel, enabled = item.canCancel) {
                        Text("Otkaži")
                    }
                }
                if (!item.canCancel) {
                    Text(
                        text = "Otkazivanje je moguće najkasnije 2 sata prije početka izvođenja.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun statusLabel(status: String): String =
    if (status == ReservationStatus.ACTIVE) "Aktivna" else "Otkazana"
