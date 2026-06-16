package uz.buron.ui.screens.venue

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import uz.buron.R
import uz.buron.domain.model.Review
import uz.buron.ui.components.AvailabilityCalendar
import uz.buron.ui.components.EmptyState
import uz.buron.ui.components.PrimaryButton
import uz.buron.ui.components.StarRating
import uz.buron.ui.screens.booking.BookingBottomSheet
import uz.buron.util.DateUtils
import uz.buron.util.PriceFormatter

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun VenueDetailScreen(
    venueId: String,
    snackbarHostState: SnackbarHostState,
    viewModel: VenueDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(venueId) {
        viewModel.loadVenue(venueId)
    }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearSnackbar()
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
            }
        }
        uiState.error != null -> {
            EmptyState(
                message = uiState.error!!,
                actionLabel = stringResource(R.string.retry),
                onAction = { viewModel.loadVenue(venueId) }
            )
        }
        uiState.venue != null -> {
            val venue = uiState.venue!!
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    if (venue.images.isNotEmpty()) {
                        val pagerState = rememberPagerState(pageCount = { venue.images.size })
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                        ) { page ->
                            AsyncImage(
                                model = venue.images[page],
                                contentDescription = venue.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }

                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = venue.name, style = MaterialTheme.typography.headlineLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        StarRating(rating = venue.rating)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${PriceFormatter.format(venue.pricePerSession)} ${stringResource(R.string.venue_per_session)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = venue.description, style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null)
                            Text(
                                text = venue.address,
                                modifier = Modifier.padding(start = 8.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Text(
                            text = "${venue.region}, ${venue.district}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${venue.phone}"))
                                context.startActivity(intent)
                            }
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null)
                            Text(text = venue.phone, modifier = Modifier.padding(start = 8.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.venue_capacity, venue.capacity),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = stringResource(
                                R.string.venue_owner,
                                venue.owner.firstName,
                                venue.owner.lastName
                            ),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (!venue.mapLink.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(venue.mapLink))
                                    context.startActivity(intent)
                                }
                            ) {
                                Text(stringResource(R.string.venue_map_link))
                            }
                        }
                    }
                }

                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            text = stringResource(R.string.venue_select_date_sessions),
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        AvailabilityCalendar(
                            yearMonth = uiState.currentMonth,
                            calendar = uiState.calendar,
                            selectedDate = uiState.selectedDate,
                            selectedSessions = uiState.selectedSessions,
                            onMonthChange = { viewModel.onMonthChange(venueId, it) },
                            onDateSelected = viewModel::onDateSelected,
                            onSessionToggle = viewModel::onSessionToggle
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        PrimaryButton(
                            text = stringResource(R.string.venue_book),
                            onClick = viewModel::showBookingSheet,
                            enabled = uiState.selectedDate != null && uiState.selectedSessions.isNotEmpty()
                        )
                    }
                }

                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.venue_reviews),
                            style = MaterialTheme.typography.titleLarge
                        )
                        if (viewModel.isLoggedIn) {
                            when {
                                uiState.isCheckingReviewEligibility -> {
                                    Text(
                                        text = stringResource(R.string.loading),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                uiState.canReview -> {
                                    ReviewFormSection(
                                        rating = uiState.reviewRating,
                                        comment = uiState.reviewComment,
                                        commentError = uiState.reviewCommentError,
                                        isSubmitting = uiState.isSubmittingReview,
                                        onRatingChange = viewModel::onReviewRatingChange,
                                        onCommentChange = viewModel::onReviewCommentChange,
                                        onSubmit = { viewModel.submitReview(venueId) }
                                    )
                                }
                                else -> {
                                    Text(
                                        text = stringResource(R.string.review_booking_required),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        if (uiState.reviews.isEmpty()) {
                            Text(
                                text = stringResource(R.string.venue_no_reviews),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                items(uiState.reviews, key = { it.id }) { review ->
                    ReviewCard(
                        review = review,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

            BookingBottomSheet(
                visible = uiState.showBookingSheet,
                selectedDate = uiState.selectedDate,
                selectedSessions = uiState.selectedSessions,
                isLoggedIn = viewModel.isLoggedIn,
                guestName = uiState.guestName,
                guestPhoneDigits = uiState.guestPhoneDigits,
                guestNameError = uiState.guestNameError,
                guestPhoneError = uiState.guestPhoneError,
                isSubmitting = uiState.isSubmittingBooking,
                bookingSuccess = uiState.bookingSuccess,
                bookingError = uiState.bookingError,
                onDismiss = viewModel::hideBookingSheet,
                onGuestNameChange = viewModel::onGuestNameChange,
                onGuestPhoneChange = viewModel::onGuestPhoneChange,
                onSubmit = { viewModel.submitBooking(venueId) }
            )
        }
    }
}

@Composable
private fun ReviewCard(review: Review, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = review.authorName, style = MaterialTheme.typography.titleMedium)
            StarRating(rating = review.rating.toDouble(), showValue = false)
            Text(text = review.comment, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = DateUtils.formatReviewDate(review.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReviewFormSection(
    rating: Int,
    comment: String,
    commentError: String?,
    isSubmitting: Boolean,
    onRatingChange: (Int) -> Unit,
    onCommentChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = stringResource(R.string.review_add), style = MaterialTheme.typography.titleMedium)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = "$rating",
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.review_rating)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                (5 downTo 1).forEach { value ->
                    DropdownMenuItem(
                        text = { Text("$value") },
                        onClick = {
                            onRatingChange(value)
                            expanded = false
                        }
                    )
                }
            }
        }
        uz.buron.ui.components.FormTextField(
            value = comment,
            onValueChange = onCommentChange,
            label = stringResource(R.string.review_comment),
            error = commentError,
            singleLine = false,
            minLines = 3
        )
        PrimaryButton(
            text = stringResource(R.string.review_submit),
            onClick = onSubmit,
            enabled = !isSubmitting
        )
    }
}
