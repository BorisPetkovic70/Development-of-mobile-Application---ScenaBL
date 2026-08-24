package com.example.scenabl.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.scenabl.ui.util.fromUtcPickerMillis
import com.example.scenabl.ui.util.toUtcPickerMillis
import com.example.scenabl.viewmodel.OrganizerPerformanceFormViewModel
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizerPerformanceFormScreen(
    viewModel: OrganizerPerformanceFormViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) onSaved()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Novo izvođenje") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Nazad")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                Text(state.date?.toString() ?: "Odaberite datum")
            }
            OutlinedButton(onClick = { showTimePicker = true }, modifier = Modifier.fillMaxWidth()) {
                Text(state.time?.toString() ?: "Odaberite vrijeme")
            }
            OutlinedTextField(
                value = state.sala,
                onValueChange = viewModel::onSalaChange,
                label = { Text("Sala") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.kapacitetInput,
                onValueChange = viewModel::onKapacitetChange,
                label = { Text("Kapacitet") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.cijenaInput,
                onValueChange = viewModel::onCijenaChange,
                label = { Text("Cijena karte") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            state.errorMessage?.let { message ->
                Text(text = message, color = MaterialTheme.colorScheme.error)
            }

            Button(onClick = viewModel::save, enabled = !state.isSaving, modifier = Modifier.fillMaxWidth()) {
                if (state.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.padding(2.dp))
                } else {
                    Text("Zakaži izvođenje")
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialogContent(
            initialMillis = state.date?.toUtcPickerMillis(),
            onDismiss = { showDatePicker = false },
            onConfirm = { millis ->
                viewModel.onDateSelected(millis.fromUtcPickerMillis())
                showDatePicker = false
            }
        )
    }

    if (showTimePicker) {
        TimePickerDialogContent(
            initial = state.time ?: LocalTime.now(),
            onDismiss = { showTimePicker = false },
            onConfirm = { time ->
                viewModel.onTimeSelected(time)
                showTimePicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialogContent(
    initialMillis: Long?,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    val pickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
    Dialog(onDismissRequest = onDismiss) {
        Surface {
            Column {
                DatePicker(state = pickerState)
                DialogActionsRow(onDismiss = onDismiss, onConfirm = { pickerState.selectedDateMillis?.let(onConfirm) })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialogContent(
    initial: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit
) {
    val pickerState = rememberTimePickerState(initialHour = initial.hour, initialMinute = initial.minute, is24Hour = true)
    Dialog(onDismissRequest = onDismiss) {
        Surface {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "Odaberite vrijeme", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                TimePicker(state = pickerState)
                DialogActionsRow(
                    onDismiss = onDismiss,
                    onConfirm = { onConfirm(LocalTime.of(pickerState.hour, pickerState.minute)) }
                )
            }
        }
    }
}

@Composable
private fun DialogActionsRow(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        horizontalArrangement = Arrangement.End
    ) {
        TextButton(onClick = onDismiss) { Text("Otkaži") }
        TextButton(onClick = onConfirm) { Text("Potvrdi") }
    }
}
