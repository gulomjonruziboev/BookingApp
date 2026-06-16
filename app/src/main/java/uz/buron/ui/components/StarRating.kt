package uz.buron.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import uz.buron.ui.theme.WarningYellow

@Composable
fun StarRating(
    rating: Double,
    modifier: Modifier = Modifier,
    showValue: Boolean = true
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        repeat(5) { index ->
            val filled = rating >= index + 1
            val half = !filled && rating > index && rating < index + 1
            Icon(
                imageVector = if (filled || half) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = null,
                tint = WarningYellow,
                modifier = Modifier.size(16.dp)
            )
        }
        if (showValue) {
            Text(
                text = String.format(" %.1f", rating),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
