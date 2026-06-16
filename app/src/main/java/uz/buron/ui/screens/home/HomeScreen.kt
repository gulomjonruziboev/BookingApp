package uz.buron.ui.screens.home

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uz.buron.R
import uz.buron.ui.components.EmptyState
import uz.buron.ui.components.PrimaryButton
import uz.buron.ui.components.SearchBar
import uz.buron.ui.components.VenueCard
import uz.buron.ui.components.VenueCardSkeleton

@Composable
fun HomeScreen(
    onNavigateToSearch: (String) -> Unit,
    onNavigateToVenue: (String) -> Unit,
    onNavigateToContact: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showLocationRationale by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.loadPopularVenues()
        }
    }

    LaunchedEffect(Unit) {
        if (viewModel.shouldShowLocationPermissionPrompt(context)) {
            showLocationRationale = true
        }
    }

    if (showLocationRationale) {
        AlertDialog(
            onDismissRequest = {
                showLocationRationale = false
            },
            title = { Text("Joylashuv ruxsati") },
            text = { Text(stringResource(R.string.location_rationale)) },
            confirmButton = {
                TextButton(onClick = {
                    showLocationRationale = false
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }) {
                    Text("Ruxsat berish")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showLocationRationale = false
                    viewModel.onLocationPermissionDeclined()
                }) {
                    Text("Keyinroq")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SearchBar(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                onSearch = { onNavigateToSearch(uiState.searchQuery) },
                placeholder = stringResource(R.string.home_search_placeholder)
            )
        }

        item {
            Text(
                text = stringResource(R.string.home_popular_venues),
                style = MaterialTheme.typography.headlineMedium
            )
            uiState.detectedRegion?.let { region ->
                Text(
                    text = "Viloyat: $region",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (uiState.isLoading) {
            items(3) {
                VenueCardSkeleton()
            }
        } else if (uiState.error != null) {
            item {
                EmptyState(
                    message = uiState.error!!,
                    actionLabel = stringResource(R.string.retry),
                    onAction = viewModel::loadPopularVenues
                )
            }
        } else if (uiState.venues.isEmpty()) {
            item {
                EmptyState(message = stringResource(R.string.search_empty))
            }
        } else {
            items(uiState.venues, key = { it.id }) { venue ->
                VenueCard(venue = venue, onClick = { onNavigateToVenue(venue.id) })
            }
        }

        item {
            PrimaryButton(
                text = stringResource(R.string.home_see_more),
                onClick = { onNavigateToSearch("") }
            )
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.home_contact_teaser),
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onNavigateToContact) {
                    Text(stringResource(R.string.nav_contact))
                }
            }
        }
    }
}
