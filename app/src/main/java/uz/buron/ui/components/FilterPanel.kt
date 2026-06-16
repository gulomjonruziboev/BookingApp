package uz.buron.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import uz.buron.data.repository.VenueSearchParams
import uz.buron.util.Constants

data class SearchFilters(
    val region: String? = null,
    val minPrice: String = "",
    val maxPrice: String = "",
    val minCapacity: String = "",
    val minRating: Double? = null,
    val sort: String = "rating",
    val order: String = "desc"
) {
    fun toParams(query: String?, page: Int, limit: Int = 12): VenueSearchParams =
        VenueSearchParams(
            query = query,
            region = region,
            minPrice = minPrice.toLongOrNull(),
            maxPrice = maxPrice.toLongOrNull(),
            minCapacity = minCapacity.toIntOrNull(),
            minRating = minRating,
            sort = sort,
            order = order,
            page = page,
            limit = limit
        )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterPanel(
    filters: SearchFilters,
    onFiltersChange: (SearchFilters) -> Unit,
    onApply: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Filtrlar", style = MaterialTheme.typography.titleLarge)

        DropdownField(
            label = "Viloyat",
            value = filters.region ?: "Barcha viloyatlar",
            options = listOf("Barcha viloyatlar") + Constants.UZBEK_REGIONS,
            onSelected = { selected ->
                onFiltersChange(
                    filters.copy(region = if (selected == "Barcha viloyatlar") null else selected)
                )
            }
        )

        FormTextField(
            value = filters.minPrice,
            onValueChange = { onFiltersChange(filters.copy(minPrice = it)) },
            label = "Min narx",
            keyboardType = KeyboardType.Number
        )

        FormTextField(
            value = filters.maxPrice,
            onValueChange = { onFiltersChange(filters.copy(maxPrice = it)) },
            label = "Max narx",
            keyboardType = KeyboardType.Number
        )

        FormTextField(
            value = filters.minCapacity,
            onValueChange = { onFiltersChange(filters.copy(minCapacity = it)) },
            label = "Min sig'im",
            keyboardType = KeyboardType.Number
        )

        DropdownField(
            label = "Min reyting",
            value = when (filters.minRating) {
                3.0 -> "3+"
                4.0 -> "4+"
                5.0 -> "5"
                else -> "Barchasi"
            },
            options = listOf("Barchasi", "3+", "4+", "5"),
            onSelected = { selected ->
                val rating = when (selected) {
                    "3+" -> 3.0
                    "4+" -> 4.0
                    "5" -> 5.0
                    else -> null
                }
                onFiltersChange(filters.copy(minRating = rating))
            }
        )

        DropdownField(
            label = "Tartiblash",
            value = when (filters.sort) {
                "price" -> "Narx"
                "name" -> "Nomi"
                else -> "Reyting"
            },
            options = listOf("Reyting", "Narx", "Nomi"),
            onSelected = { selected ->
                val sort = when (selected) {
                    "Narx" -> "price"
                    "Nomi" -> "name"
                    else -> "rating"
                }
                onFiltersChange(filters.copy(sort = sort))
            }
        )

        DropdownField(
            label = "Tartib",
            value = if (filters.order == "asc") "O'sish" else "Kamayish",
            options = listOf("Kamayish", "O'sish"),
            onSelected = { selected ->
                onFiltersChange(filters.copy(order = if (selected == "O'sish") "asc" else "desc"))
            }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextButton(onClick = onReset, modifier = Modifier.weight(1f)) {
                Text("Tozalash")
            }
            PrimaryButton(
                text = "Qo'llash",
                onClick = onApply,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownField(
    label: String,
    value: String,
    options: List<String>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (label, color) = when (status) {
        "pending" -> "Kutilmoqda" to androidx.compose.ui.graphics.Color(0xFFEAB308)
        "confirmed" -> "Tasdiqlangan" to androidx.compose.ui.graphics.Color(0xFF16A34A)
        "cancelled" -> "Bekor qilingan" to androidx.compose.ui.graphics.Color(0xFFDC2626)
        else -> status to MaterialTheme.colorScheme.surfaceVariant
    }
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = color,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun SessionChip(session: String, modifier: Modifier = Modifier) {
    val label = Constants.SESSION_LABELS[session] ?: session
    Box(
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.primaryContainer,
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}
