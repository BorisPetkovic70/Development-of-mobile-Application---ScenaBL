package com.example.scenabl.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Tappable 1-5 star selector, used to input a review rating (REQ-REV-001). */
@Composable
fun RatingInput(
    rating: Int,
    onRatingChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        for (star in 1..5) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "$star zvjezdica",
                tint = if (star <= rating) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                },
                modifier = Modifier
                    .size(32.dp)
                    .clickable { onRatingChange(star) }
            )
        }
    }
}
