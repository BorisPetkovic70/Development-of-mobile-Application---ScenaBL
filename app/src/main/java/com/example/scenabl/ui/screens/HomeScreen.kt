package com.example.scenabl.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.scenabl.data.model.Genres
import com.example.scenabl.data.model.TitleType
import com.example.scenabl.ui.components.RatingStars
import com.example.scenabl.ui.util.formatDateTime
import com.example.scenabl.ui.util.fromUtcPickerMillis
import com.example.scenabl.ui.util.toUtcPickerMillis
import com.example.scenabl.viewmodel.HomeViewModel
import com.example.scenabl.viewmodel.RepertoireItem
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    isLoggedIn: Boolean,
    onTitleClick: (String) -> Unit,
    onLoginClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var showDateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ScenaBL") },
                actions = {
                    if (!isLoggedIn) {
                        IconButton(onClick = onLoginClick) {
                            Icon(Icons.Filled.Person, contentDescription = "Prijavi se")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = viewModel::onSearchQueryChange,
                    label = { Text("Pretraga naslova") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                TypeToggle(selected = state.selectedType, onSelected = viewModel::onTypeSelected)

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Genres.ALL.forEach { genre ->
                        FilterChip(
                            selected = genre in state.selectedGenres,
                            onClick = { viewModel.onGenreToggle(genre) },
                            label = { Text(genre) }
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { showDateDialog = true }) {
                        Icon(Icons.Filled.DateRange, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(dateRangeLabel(state.dateFrom, state.dateTo))
                    }
                    if (state.hasActiveFilters) {
                        TextButton(onClick = viewModel::resetFilters) {
                            Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.size(4.dp))
                            Text("Resetuj filtere")
                        }
                    }
                }
            }

            when {
                state.isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

                state.errorMessage != null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Greška pri učitavanju repertoara: ${state.errorMessage}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(24.dp)
                    )
                }

                state.items.isEmpty() -> EmptyRepertoire(
                    hasActiveFilters = state.hasActiveFilters,
                    onReset = viewModel::resetFilters
                )

                else -> LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.items, key = { it.performance.id }) { item ->
                        RepertoireCard(item = item, onClick = { onTitleClick(item.title.id) })
                    }
                }
            }
        }
    }

    if (showDateDialog) {
        DateRangeFilterDialog(
            initialFrom = state.dateFrom,
            initialTo = state.dateTo,
            onDismiss = { showDateDialog = false },
            onConfirm = { from, to -> viewModel.onDateRangeSelected(from, to) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TypeToggle(selected: String?, onSelected: (String?) -> Unit) {
    val options = listOf(null to "Oba", TitleType.POZORISTE to "Pozorište", TitleType.BIOSKOP to "Bioskop")
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, (value, label) ->
            SegmentedButton(
                selected = selected == value,
                onClick = { onSelected(value) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size)
            ) {
                Text(label)
            }
        }
    }
}

@Composable
private fun RepertoireCard(item: RepertoireItem, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (item.title.slikaUrl.isNotBlank()) {
                    AsyncImage(
                        model = item.title.slikaUrl,
                        contentDescription = item.title.naziv,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            Spacer(modifier = Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = item.title.naziv, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = item.institutionName, style = MaterialTheme.typography.bodySmall)
                Text(text = item.performance.datumVrijeme.formatDateTime(), style = MaterialTheme.typography.bodySmall)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RatingStars(rating = item.averageRating)
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = if (item.reviewCount > 0) "(${item.reviewCount})" else "Nema ocjena",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyRepertoire(hasActiveFilters: Boolean, onReset: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (hasActiveFilters) "Nema izvođenja koja odgovaraju filterima" else "Trenutno nema zakazanih izvođenja.",
            style = MaterialTheme.typography.bodyLarge
        )
        if (hasActiveFilters) {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(onClick = onReset) { Text("Resetuj filtere") }
        }
    }
}

private fun dateRangeLabel(from: LocalDate?, to: LocalDate?): String = when {
    from == null -> "Datum"
    to == null || to == from -> from.toString()
    else -> "$from – $to"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRangeFilterDialog(
    initialFrom: LocalDate?,
    initialTo: LocalDate?,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate?, LocalDate?) -> Unit
) {
    val pickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = initialFrom?.toUtcPickerMillis(),
        initialSelectedEndDateMillis = initialTo?.toUtcPickerMillis()
    )

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                DateRangePicker(state = pickerState, modifier = Modifier.weight(1f))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { onConfirm(null, null); onDismiss() }) { Text("Obriši") }
                    Spacer(modifier = Modifier.size(8.dp))
                    TextButton(onClick = onDismiss) { Text("Otkaži") }
                    Spacer(modifier = Modifier.size(8.dp))
                    TextButton(onClick = {
                        val from = pickerState.selectedStartDateMillis?.fromUtcPickerMillis()
                        val to = (pickerState.selectedEndDateMillis ?: pickerState.selectedStartDateMillis)
                            ?.fromUtcPickerMillis()
                        onConfirm(from, to)
                        onDismiss()
                    }) { Text("Primijeni") }
                }
            }
        }
    }
}
