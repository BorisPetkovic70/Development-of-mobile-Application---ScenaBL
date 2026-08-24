package com.example.scenabl.ui.util

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

/**
 * Shows a transient state-change message via Snackbar and only then clears it, so operations
 * that change state always give visible feedback instead of silently succeeding or failing
 * (NFR-REL-002) rather than being cleared unseen.
 */
@Composable
fun ObserveSnackbarMessage(
    message: String?,
    snackbarHostState: SnackbarHostState,
    onConsumed: () -> Unit
) {
    LaunchedEffect(message) {
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            onConsumed()
        }
    }
}
