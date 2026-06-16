package uz.buron.ui.screens.mybookings

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
import uz.buron.domain.model.Booking
import javax.inject.Inject

data class MyBookingsUiState(
    val isLoading: Boolean = true,
    val bookings: List<Booking> = emptyList(),
    val error: String? = null,
    val cancelTargetId: String? = null,
    val isCancelling: Boolean = false,
    val snackbarMessage: String? = null
)

@HiltViewModel
class MyBookingsViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyBookingsUiState())
    val uiState: StateFlow<MyBookingsUiState> = _uiState.asStateFlow()

    val isLoggedIn: Boolean get() = authRepository.isLoggedIn()

    init {
        if (isLoggedIn) loadBookings()
    }

    fun loadBookings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            bookingRepository.getMyBookings()
                .onSuccess { bookings ->
                    _uiState.update { it.copy(isLoading = false, bookings = bookings) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, error = mapError(error))
                    }
                }
        }
    }

    fun showCancelDialog(bookingId: String) {
        _uiState.update { it.copy(cancelTargetId = bookingId) }
    }

    fun hideCancelDialog() {
        _uiState.update { it.copy(cancelTargetId = null) }
    }

    fun confirmCancel() {
        val id = _uiState.value.cancelTargetId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isCancelling = true) }
            bookingRepository.cancelBooking(id)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isCancelling = false,
                            cancelTargetId = null,
                            snackbarMessage = "Bron bekor qilindi"
                        )
                    }
                    loadBookings()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isCancelling = false,
                            cancelTargetId = null,
                            snackbarMessage = mapError(error)
                        )
                    }
                }
        }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    private fun mapError(error: Throwable): String = when (error) {
        is ApiException.Unauthorized -> "Kirish talab qilinadi"
        is ApiException.Network -> "Internet aloqasi yo'q"
        is ApiException.Client -> error.message ?: "Xatolik"
        is ApiException.Server -> "Server xatosi. Keyinroq urinib ko'ring."
        else -> error.message ?: "Xatolik yuz berdi"
    }
}
