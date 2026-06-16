package uz.buron.data.repository

import com.squareup.moshi.Moshi
import uz.buron.data.api.BuronApiService
import uz.buron.data.api.safeApiCall
import uz.buron.data.dto.CreateBookingRequestDto
import uz.buron.data.dto.toDomain
import uz.buron.domain.model.Booking
import uz.buron.domain.model.CalendarResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookingRepository @Inject constructor(
    private val api: BuronApiService,
    private val moshi: Moshi
) {
    suspend fun getCalendar(venueId: String, month: String): Result<CalendarResponse> {
        return safeApiCall(moshi) {
            api.getCalendar(venueId, month).toDomain()
        }
    }

    suspend fun createBooking(
        venueId: String,
        date: String,
        sessions: List<String>,
        clientName: String? = null,
        clientPhone: String? = null
    ): Result<Booking> {
        return safeApiCall(moshi) {
            api.createBooking(
                CreateBookingRequestDto(
                    venueId = venueId,
                    clientName = clientName,
                    clientPhone = clientPhone,
                    date = date,
                    sessions = sessions
                )
            ).toDomain()
        }
    }

    suspend fun getMyBookings(): Result<List<Booking>> {
        return safeApiCall(moshi) {
            api.getMyBookings().map { it.toDomain() }
        }
    }

    suspend fun cancelBooking(id: String): Result<Booking> {
        return safeApiCall(moshi) {
            api.cancelBooking(id).toDomain()
        }
    }

    suspend fun hasConfirmedBookingForVenue(venueId: String): Result<Boolean> {
        return getMyBookings().map { bookings ->
            bookings.any { booking ->
                booking.venue.id == venueId && booking.status == CONFIRMED_STATUS
            }
        }
    }

    companion object {
        private const val CONFIRMED_STATUS = "confirmed"
    }
}
