package com.example.scenabl.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.scenabl.data.model.ListType
import com.example.scenabl.ui.util.ObserveSnackbarMessage
import com.example.scenabl.viewmodel.ListEntryItem
import com.example.scenabl.viewmodel.MyListsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyListsScreen(
    viewModel: MyListsViewModel,
    onTitleClick: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val tabs = listOf(ListType.ZELIM_GLEDATI to "Želim gledati", ListType.ODGLEDANO to "Odgledano")
    val selectedIndex = tabs.indexOfFirst { it.first == state.selectedTab }.coerceAtLeast(0)
    val snackbarHostState = remember { SnackbarHostState() }

    ObserveSnackbarMessage(state.errorMessage, snackbarHostState, viewModel::consumeError)

    Scaffold(
        topBar = { TopAppBar(title = { Text("Moje liste") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            SecondaryTabRow(selectedTabIndex = selectedIndex) {
                tabs.forEachIndexed { index, (type, label) ->
                    Tab(
                        selected = index == selectedIndex,
                        onClick = { viewModel.onTabSelected(type) },
                        text = { Text(label) }
                    )
                }
            }

            val items = if (state.selectedTab == ListType.ZELIM_GLEDATI) state.zelimGledati else state.odgledano

            when {
                state.isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

                items.isEmpty() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (state.selectedTab == ListType.ZELIM_GLEDATI) {
                            "Nema naslova na listi \"Želim gledati\"."
                        } else {
                            "Nema odgledanih naslova."
                        },
                        modifier = Modifier.padding(24.dp)
                    )
                }

                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items, key = { it.entry.id }) { item ->
                        ListEntryRow(
                            item = item,
                            onClick = { onTitleClick(item.naslov.id) },
                            onRemove = { viewModel.removeFromList(item.naslov.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ListEntryRow(item: ListEntryItem, onClick: () -> Unit, onRemove: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (item.naslov.slikaUrl.isNotBlank()) {
                    AsyncImage(
                        model = item.naslov.slikaUrl,
                        contentDescription = item.naslov.naziv,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            Text(
                text = item.naslov.naziv,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
            )
            IconButton(onClick = onRemove) {
                Icon(Icons.Filled.Delete, contentDescription = "Ukloni sa liste")
            }
        }
    }
}
