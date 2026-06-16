package uz.buron.ui.screens.mybookings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import uz.buron.domain.model.Booking
import uz.buron.ui.components.EmptyState
import uz.buron.ui.components.SessionChip
import uz.buron.ui.components.StatusBadge
import uz.buron.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MyBookingsScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToVenue: (String) -> Unit,
    snackbarHostState: SnackbarHostState,
    viewModel: MyBookingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel.isLoggedIn) {
        if (!viewModel.isLoggedIn) {
            onNavigateToLogin()
        } else {
            viewModel.loadBookings()
        }
    }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearSnackbar()
        }
    }

    if (!viewModel.isLoggedIn) return

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.my_bookings_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )

        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = viewModel::loadBookings,
            modifier = Modifier.fillMaxSize()
        ) {
            when {
                uiState.isLoading && uiState.bookings.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.error != null -> {
                    EmptyState(
                        message = uiState.error!!,
                        actionLabel = stringResource(R.string.retry),
                        onAction = viewModel::loadBookings
                    )
                }
                uiState.bookings.isEmpty() -> {
                    EmptyState(
                        message = stringResource(R.string.my_bookings_empty),
                        actionLabel = stringResource(R.string.my_bookings_search_cta),
                        onAction = onNavigateToSearch
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.bookings, key = { it.id }) { booking ->
                            BookingCard(
                                booking = booking,
                                onVenueClick = {
                                    if (booking.venue.id.isNotBlank()) {
                                        onNavigateToVenue(booking.venue.id)
                                    }
                                },
                                onCancel = {
                                    if (booking.status == "pending" || booking.status == "confirmed") {
                                        viewModel.showCancelDialog(booking.id)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (uiState.cancelTargetId != null) {
        AlertDialog(
            onDismissRequest = viewModel::hideCancelDialog,
            title = { Text(stringResource(R.string.booking_cancel)) },
            text = { Text(stringResource(R.string.booking_confirm_cancel)) },
            confirmButton = {
                TextButton(
                    onClick = viewModel::confirmCancel,
                    enabled = !uiState.isCancelling
                ) {
                    Text(stringResource(R.string.booking_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::hideCancelDialog) {
                    Text(stringResource(R.string.close))
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BookingCard(
    booking: Booking,
    onVenueClick: () -> Unit,
    onCancel: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = booking.venue.name,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.clickable(onClick = onVenueClick)
            )
            StatusBadge(status = booking.status)
            booking.venue.region?.let { region ->
                Text(
                    text = "$region, ${booking.venue.district.orEmpty()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Text(
                text = DateUtils.formatDisplayDate(booking.date),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
            FlowRow(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                booking.sessions.forEach { session ->
                    SessionChip(session = session)
                }
            }
            if (booking.status == "pending" || booking.status == "confirmed") {
                TextButton(
                    onClick = onCancel,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.booking_cancel),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
