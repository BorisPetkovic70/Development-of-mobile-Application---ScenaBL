package com.example.scenabl.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.scenabl.data.model.Izvodjenje
import com.example.scenabl.data.model.ListType
import com.example.scenabl.data.model.Recenzija
import com.example.scenabl.data.model.TitleType
import com.example.scenabl.ui.components.RatingInput
import com.example.scenabl.ui.components.RatingStars
import com.example.scenabl.ui.util.formatDate
import com.example.scenabl.ui.util.formatDateTime
import com.example.scenabl.viewmodel.ReviewItem
import com.example.scenabl.viewmodel.TitleDetailsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TitleDetailsScreen(
    viewModel: TitleDetailsViewModel,
    isLoggedIn: Boolean,
    onBack: () -> Unit,
    onReserveClick: (String) -> Unit,
    onLoginRequired: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var showReviewDialog by remember { mutableStateOf(false) }
    var isSubmittingForDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.isSubmittingReview, state.reviewErrorMessage) {
        if (isSubmittingForDialog && !state.isSubmittingReview && state.reviewErrorMessage == null) {
            showReviewDialog = false
            isSubmittingForDialog = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.naslov?.naziv ?: "Detalji naslova") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Nazad")
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            state.naslov == null -> Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Naslov nije pronađen.")
            }

            else -> {
                val naslov = state.naslov!!
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            if (naslov.slikaUrl.isNotBlank()) {
                                AsyncImage(
                                    model = naslov.slikaUrl,
                                    contentDescription = naslov.naziv,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = naslov.naziv, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text(
                                text = "${typeLabel(naslov.tip)} • ${naslov.zanr} • ${naslov.trajanje} min",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (naslov.reziser.isNotBlank()) {
                                Text(text = "Reditelj: ${naslov.reziser}", style = MaterialTheme.typography.bodyMedium)
                            }
                            if (state.institutionName.isNotBlank()) {
                                Text(text = state.institutionName, style = MaterialTheme.typography.bodyMedium)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RatingStars(rating = state.averageRating)
                                Spacer(modifier = Modifier.size(4.dp))
                                Text(
                                    text = if (state.reviewCount > 0) {
                                        "%.1f (%d recenzija)".format(state.averageRating, state.reviewCount)
                                    } else {
                                        "Nema recenzija"
                                    },
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    item {
                        WatchlistRow(
                            isLoggedIn = isLoggedIn,
                            selectedType = state.listEntry?.tipListe,
                            isToggling = state.isTogglingList,
                            onToggle = { type -> if (isLoggedIn) viewModel.onListToggle(type) else onLoginRequired() }
                        )
                    }

                    if (naslov.opis.isNotBlank()) {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(text = "Sinopsis", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(text = naslov.opis, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }

                    item {
                        Text(text = "Predstojeća izvođenja", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    if (state.upcomingPerformances.isEmpty()) {
                        item { Text("Trenutno nema zakazanih izvođenja.", style = MaterialTheme.typography.bodyMedium) }
                    } else {
                        items(state.upcomingPerformances, key = { it.id }) { performance ->
                            PerformanceRow(
                                performance = performance,
                                onReserveClick = { if (isLoggedIn) onReserveClick(performance.id) else onLoginRequired() }
                            )
                        }
                    }

                    item { HorizontalDivider() }

                    item {
                        Text(text = "Recenzije", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    item {
                        ReviewCallToAction(
                            isLoggedIn = isLoggedIn,
                            canReview = state.canReview,
                            hasMyReview = state.myReview != null,
                            onWriteReview = { if (isLoggedIn) showReviewDialog = true else onLoginRequired() },
                            onDeleteReview = viewModel::deleteReview
                        )
                    }

                    if (state.reviews.isEmpty()) {
                        item { Text("Još nema recenzija za ovaj naslov.", style = MaterialTheme.typography.bodyMedium) }
                    } else {
                        items(state.reviews, key = { it.recenzija.id }) { reviewItem ->
                            ReviewRow(reviewItem)
                        }
                    }
                }
            }
        }
    }

    if (showReviewDialog) {
        ReviewFormDialog(
            initial = state.myReview,
            isSubmitting = state.isSubmittingReview,
            errorMessage = state.reviewErrorMessage,
            onDismiss = { showReviewDialog = false },
            onSubmit = { ocjena, komentar ->
                isSubmittingForDialog = true
                viewModel.submitReview(ocjena, komentar)
            }
        )
    }
}

@Composable
private fun ReviewCallToAction(
    isLoggedIn: Boolean,
    canReview: Boolean,
    hasMyReview: Boolean,
    onWriteReview: () -> Unit,
    onDeleteReview: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onWriteReview, enabled = !isLoggedIn || canReview) {
                Text(if (hasMyReview) "Uredi recenziju" else "Ostavi recenziju")
            }
            if (hasMyReview) {
                OutlinedButton(onClick = onDeleteReview) { Text("Obriši recenziju") }
            }
        }
        if (isLoggedIn && !canReview) {
            Text(
                text = "Dostupno nakon što označite naslov kao odgledan",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ReviewFormDialog(
    initial: Recenzija?,
    isSubmitting: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSubmit: (Int, String) -> Unit
) {
    var rating by remember { mutableStateOf(initial?.ocjena ?: 0) }
    var comment by remember { mutableStateOf(initial?.komentar ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "Ocijenite naslov", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                RatingInput(rating = rating, onRatingChange = { rating = it })
                OutlinedTextField(
                    value = comment,
                    onValueChange = { if (it.length <= 500) comment = it },
                    label = { Text("Komentar (opciono)") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("${comment.length}/500") }
                )
                errorMessage?.let { Text(text = it, color = MaterialTheme.colorScheme.error) }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss, enabled = !isSubmitting) { Text("Otkaži") }
                    Spacer(modifier = Modifier.size(8.dp))
                    Button(onClick = { onSubmit(rating, comment) }, enabled = !isSubmitting && rating in 1..5) {
                        if (isSubmitting) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp))
                        } else {
                            Text("Sačuvaj")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WatchlistRow(
    isLoggedIn: Boolean,
    selectedType: String?,
    isToggling: Boolean,
    onToggle: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = selectedType == ListType.ZELIM_GLEDATI,
                onClick = { onToggle(ListType.ZELIM_GLEDATI) },
                enabled = !isToggling,
                label = { Text("Želim gledati") }
            )
            FilterChip(
                selected = selectedType == ListType.ODGLEDANO,
                onClick = { onToggle(ListType.ODGLEDANO) },
                enabled = !isToggling,
                label = { Text("Odgledano") }
            )
        }
        if (!isLoggedIn) {
            Text(
                text = "Prijavite se da biste dodavali naslove na lične liste.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PerformanceRow(performance: Izvodjenje, onReserveClick: () -> Unit) {
    val remaining = (performance.kapacitet - performance.rezervisano).coerceAtLeast(0)
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = performance.datumVrijeme.formatDateTime(), fontWeight = FontWeight.Bold)
            Text(text = "Sala: ${performance.sala}", style = MaterialTheme.typography.bodySmall)
            Text(text = "Cijena: %.2f".format(performance.cijena), style = MaterialTheme.typography.bodySmall)
            Text(
                text = if (remaining > 0) "Preostalo mjesta: $remaining" else "Rasprodato",
                style = MaterialTheme.typography.bodySmall,
                color = if (remaining > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.size(8.dp))
            Button(onClick = onReserveClick, enabled = remaining > 0) {
                Text(if (remaining > 0) "Rezerviši" else "Rasprodato")
            }
        }
    }
}

@Composable
private fun ReviewRow(reviewItem: ReviewItem) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = reviewItem.reviewerName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.size(8.dp))
            RatingStars(rating = reviewItem.recenzija.ocjena.toDouble())
        }
        Text(text = reviewItem.recenzija.datum.formatDate(), style = MaterialTheme.typography.bodySmall)
        if (reviewItem.recenzija.komentar.isNotBlank()) {
            Text(text = reviewItem.recenzija.komentar, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private fun typeLabel(tip: String): String = if (tip == TitleType.BIOSKOP) "Bioskop" else "Pozorište"
