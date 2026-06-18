package uz.buron.ui.screens.venue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uz.buron.data.api.ApiException
import uz.buron.data.repository.AuthRepository
import uz.buron.data.repository.BookingRepository
import uz.buron.data.repository.ReviewRepository
import uz.buron.data.repository.VenueRepository
import uz.buron.domain.model.CalendarDay
import uz.buron.domain.model.Review
import uz.buron.domain.model.Venue
import uz.buron.util.Constants
import uz.buron.util.DateUtils
import uz.buron.util.PhoneUtils
import uz.buron.util.Validation
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class VenueDetailUiState(
    val isLoading: Boolean = true,
    val venue: Venue? = null,
    val reviews: List<Review> = emptyList(),
    val calendar: Map<String, CalendarDay> = emptyMap(),
    val currentMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate? = null,
    val selectedSessions: Set<String> = emptySet(),
    val availableSessions: List<String> = emptyList(),
    val showBookingSheet: Boolean = false,
    val bookingSuccess: Boolean = false,
    val bookingError: String? = null,
    val isSubmittingBooking: Boolean = false,
    val guestName: String = "",
    val guestPhoneDigits: String = "",
    val guestNameError: String? = null,
    val guestPhoneError: String? = null,
    val reviewRating: Int = 5,
    val reviewComment: String = "",
    val reviewCommentError: String? = null,
    val isSubmittingReview: Boolean = false,
    val canReview: Boolean = false,
    val isCheckingReviewEligibility: Boolean = false,
    val error: String? = null,
    val snackbarMessage: String? = null
)

@HiltViewModel
class VenueDetailViewModel @Inject constructor(
    private val venueRepository: VenueRepository,
    private val bookingRepository: BookingRepository,
    private val reviewRepository: ReviewRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VenueDetailUiState())
    val uiState: StateFlow<VenueDetailUiState> = _uiState.asStateFlow()

    val isLoggedIn: Boolean get() = authRepository.isLoggedIn()
    val currentUser get() = authRepository.getCurrentUser()

    fun loadVenue(venueId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val venueResult = venueRepository.getVenue(venueId)
            val reviewsResult = reviewRepository.getVenueReviews(venueId)
            venueResult.onSuccess { venue ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        venue = venue,
                        reviews = reviewsResult.getOrDefault(emptyList())
                    )
                }
                loadCalendar(venueId, _uiState.value.currentMonth)
                checkReviewEligibility(venueId)
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, error = mapError(error)) }
            }
        }
    }

    fun loadCalendar(venueId: String, month: YearMonth, clearSelection: Boolean = true) {
        viewModelScope.launch {
            val monthStr = DateUtils.toMonthString(month)
            bookingRepository.getCalendar(venueId, monthStr)
                .onSuccess { response ->
                    _uiState.update { state ->
                        val selectedDate = if (clearSelection) null else state.selectedDate
                        val availableSessions = if (clearSelection) {
                            emptyList()
                        } else {
                            selectedDate?.let { selected ->
                                response.calendar[DateUtils.toCalendarKey(selected)]?.available
                            } ?: emptyList()
                        }
                        state.copy(
                            currentMonth = month,
                            calendar = response.calendar,
                            selectedDate = selectedDate,
                            selectedSessions = if (clearSelection) emptySet() else {
                                state.selectedSessions.intersect(availableSessions.toSet())
                            },
                            availableSessions = availableSessions
                        )
                    }
                }
        }
    }

    fun onMonthChange(venueId: String, month: YearMonth) {
        loadCalendar(venueId, month)
    }

    fun onDateSelected(date: LocalDate, available: List<String>) {
        _uiState.update {
            it.copy(
                selectedDate = date,
                availableSessions = available,
                selectedSessions = emptySet()
            )
        }
    }

    fun onSessionToggle(session: String) {
        if (session !in _uiState.value.availableSessions) return
        _uiState.update { state ->
            val updated = state.selectedSessions.toMutableSet()
            if (updated.contains(session)) updated.remove(session) else updated.add(session)
            state.copy(selectedSessions = updated)
        }
    }

    fun showBookingSheet() {
        _uiState.update { it.copy(showBookingSheet = true, bookingSuccess = false, bookingError = null) }
    }

    fun hideBookingSheet() {
        _uiState.update {
            it.copy(
                showBookingSheet = false,
                bookingSuccess = false,
                bookingError = null,
                guestName = "",
                guestPhoneDigits = "",
                guestNameError = null,
                guestPhoneError = null
            )
        }
    }

    fun onGuestNameChange(name: String) {
        _uiState.update { it.copy(guestName = name, guestNameError = null) }
    }

    fun onGuestPhoneChange(digits: String) {
        _uiState.update { it.copy(guestPhoneDigits = digits, guestPhoneError = null) }
    }

    fun submitBooking(venueId: String) {
        val state = _uiState.value
        if (state.isSubmittingBooking) return

        val date = state.selectedDate ?: return
        if (state.selectedSessions.isEmpty()) return

        val user = currentUser
        var clientName = user?.fullName
        var clientPhone = user?.phone

        if (!isLoggedIn) {
            val nameError = Validation.validateName(state.guestName, "Ism")
            val normalizedPhone = PhoneUtils.normalizePhoneUz("+998${state.guestPhoneDigits}")
            val phoneError = PhoneUtils.validatePhoneUz(normalizedPhone)
            if (nameError != null || phoneError != null) {
                _uiState.update {
                    it.copy(guestNameError = nameError, guestPhoneError = phoneError)
                }
                return
            }
            clientName = state.guestName.trim()
            clientPhone = normalizedPhone
        }

        if (clientName.isNullOrBlank() || clientPhone.isNullOrBlank()) {
            _uiState.update { it.copy(bookingError = "Ism va telefon talab qilinadi") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingBooking = true, bookingError = null) }

            val monthStr = DateUtils.toMonthString(state.currentMonth)
            val calendarResult = bookingRepository.getCalendar(venueId, monthStr)
            if (calendarResult.isFailure) {
                _uiState.update {
                    it.copy(
                        isSubmittingBooking = false,
                        bookingError = mapError(calendarResult.exceptionOrNull()!!)
                    )
                }
                return@launch
            }

            val freshCalendar = calendarResult.getOrThrow().calendar
            val dateKey = DateUtils.toCalendarKey(date)
            val dayData = freshCalendar[dateKey]
            val availableNow = dayData?.available ?: emptyList()
            val orderedSessions = Constants.SESSION_ORDER.filter { it in state.selectedSessions }
            val unavailableSessions = orderedSessions.filter { it !in availableNow }

            if (unavailableSessions.isNotEmpty()) {
                _uiState.update {
                    it.copy(
                        isSubmittingBooking = false,
                        calendar = freshCalendar,
                        availableSessions = availableNow,
                        selectedSessions = state.selectedSessions.intersect(availableNow.toSet()),
                        bookingError = "Tanlangan vaqt endi band. Iltimos, boshqa vaqtni tanlang."
                    )
                }
                return@launch
            }

            bookingRepository.createBooking(
                venueId = venueId,
                date = DateUtils.toIsoDateString(date),
                sessions = orderedSessions,
                clientName = clientName,
                clientPhone = clientPhone
            ).onSuccess {
                _uiState.update {
                    it.copy(
                        isSubmittingBooking = false,
                        bookingSuccess = true,
                        calendar = freshCalendar,
                        snackbarMessage = "Bron qabul qilindi!"
                    )
                }
                loadCalendar(venueId, state.currentMonth, clearSelection = false)
                if (isLoggedIn) {
                    checkReviewEligibility(venueId)
                }
            }.onFailure { error ->
                val message = mapError(error)
                loadCalendar(venueId, state.currentMonth, clearSelection = false)
                _uiState.update {
                    it.copy(
                        isSubmittingBooking = false,
                        bookingError = message
                    )
                }
            }
        }
    }

    fun onReviewRatingChange(rating: Int) {
        _uiState.update { it.copy(reviewRating = rating) }
    }

    fun onReviewCommentChange(comment: String) {
        _uiState.update { it.copy(reviewComment = comment, reviewCommentError = null) }
    }

    fun submitReview(venueId: String) {
        val state = _uiState.value
        val user = currentUser ?: return
        if (!state.canReview) {
            _uiState.update {
                it.copy(snackbarMessage = REVIEW_BOOKING_REQUIRED_MESSAGE)
            }
            return
        }
        val commentError = Validation.validateReviewComment(state.reviewComment)
        if (commentError != null) {
            _uiState.update { it.copy(reviewCommentError = commentError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingReview = true) }
            reviewRepository.createReview(
                venueId = venueId,
                authorName = user.fullName,
                rating = state.reviewRating,
                comment = state.reviewComment.trim()
            ).onSuccess {
                val reviews = reviewRepository.getVenueReviews(venueId).getOrDefault(emptyList())
                val venue = venueRepository.getVenue(venueId).getOrNull()
                _uiState.update {
                    it.copy(
                        isSubmittingReview = false,
                        reviews = reviews,
                        venue = venue ?: it.venue,
                        reviewComment = "",
                        snackbarMessage = "Sharh qo'shildi"
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSubmittingReview = false,
                        snackbarMessage = mapError(error)
                    )
                }
            }
        }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    private fun checkReviewEligibility(venueId: String) {
        if (!isLoggedIn) {
            _uiState.update { it.copy(canReview = false, isCheckingReviewEligibility = false) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingReviewEligibility = true) }
            bookingRepository.hasConfirmedBookingForVenue(venueId)
                .onSuccess { canReview ->
                    _uiState.update {
                        it.copy(
                            canReview = canReview,
                            isCheckingReviewEligibility = false
                        )
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            canReview = false,
                            isCheckingReviewEligibility = false
                        )
                    }
                }
        }
    }

    private fun mapError(error: Throwable): String = when (error) {
        is ApiException.Network -> "Internet aloqasi yo'q"
        is ApiException.RateLimited -> "Juda ko'p so'rov. Keyinroq urinib ko'ring."
        is ApiException.Server -> "Server xatosi. Keyinroq urinib ko'ring."
        is ApiException.Client -> mapClientError(error.message)
        else -> error.message?.let(::mapClientError) ?: "Xatolik yuz berdi"
    }

    private fun mapClientError(message: String?): String {
        if (message.isNullOrBlank()) return "Xatolik"
        return when {
            message.contains("already booked", ignoreCase = true) ->
                "Tanlangan vaqt endi band. Iltimos, boshqa vaqtni tanlang."
            else -> message
        }
    }

    companion object {
        const val REVIEW_BOOKING_REQUIRED_MESSAGE =
            "Sharh qoldirish uchun bron tasdiqlangan bo'lishi kerak"
    }
}
