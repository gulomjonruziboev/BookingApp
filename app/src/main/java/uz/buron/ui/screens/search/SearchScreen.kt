package uz.buron.ui.screens.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uz.buron.R
import uz.buron.ui.components.EmptyState
import uz.buron.ui.components.FilterPanel
import uz.buron.ui.components.SearchBar
import uz.buron.ui.components.VenueCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    initialQuery: String,
    onNavigateToVenue: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(initialQuery) {
        viewModel.initialize(initialQuery)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.search_title),
                style = MaterialTheme.typography.headlineMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SearchBar(
                    value = uiState.query,
                    onValueChange = viewModel::onQueryChange,
                    onSearch = viewModel::onSearch,
                    placeholder = stringResource(R.string.home_search_placeholder),
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = viewModel::toggleFilters) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filtrlar")
                }
            }
        }

        when {
            uiState.isLoading -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = stringResource(R.string.search_loading),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
            uiState.error != null -> {
                EmptyState(
                    message = uiState.error!!,
                    actionLabel = stringResource(R.string.retry),
                    onAction = viewModel::onSearch
                )
            }
            uiState.venues.isEmpty() -> {
                EmptyState(message = stringResource(R.string.search_empty))
            }
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(uiState.venues, key = { it.id }) { venue ->
                        VenueCard(
                            venue = venue,
                            onClick = { onNavigateToVenue(venue.id) }
                        )
                    }
                }

                uiState.pagination?.let { pagination ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = viewModel::prevPage,
                            enabled = pagination.page > 1
                        ) {
                            Text(stringResource(R.string.search_prev))
                        }
                        Text("${pagination.page} / ${pagination.pages}")
                        TextButton(
                            onClick = viewModel::nextPage,
                            enabled = pagination.page < pagination.pages
                        ) {
                            Text(stringResource(R.string.search_next))
                        }
                    }
                }
            }
        }
    }

    if (uiState.showFilters) {
        ModalBottomSheet(
            onDismissRequest = viewModel::toggleFilters,
            sheetState = sheetState
        ) {
            FilterPanel(
                filters = uiState.filters,
                onFiltersChange = viewModel::onFiltersChange,
                onApply = viewModel::applyFilters,
                onReset = viewModel::resetFilters
            )
        }
    }
}
