package uz.buron.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserDto(
    @Json(name = "_id") val id: String,
    val role: String,
    val firstName: String,
    val lastName: String,
    val phone: String,
    val telegram: String? = null,
    val isEnabled: Boolean = true,
    val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class AuthResponseDto(
    val token: String,
    val user: UserDto
)

@JsonClass(generateAdapter = true)
data class RegisterRequestDto(
    val role: String,
    val firstName: String,
    val lastName: String,
    val phone: String,
    val password: String
)

@JsonClass(generateAdapter = true)
data class LoginRequestDto(
    val phone: String,
    val password: String
)

@JsonClass(generateAdapter = true)
data class VenueOwnerDto(
    @Json(name = "_id") val id: String,
    val firstName: String,
    val lastName: String,
    val phone: String? = null,
    val telegram: String? = null
)

@JsonClass(generateAdapter = true)
data class LocationDto(
    val lat: Double,
    val lng: Double
)

@JsonClass(generateAdapter = true)
data class VenueDto(
    @Json(name = "_id") val id: String,
    val owner: VenueOwnerDto,
    val name: String,
    val description: String,
    val address: String,
    val mapLink: String? = null,
    val location: LocationDto? = null,
    val region: String,
    val district: String,
    val phone: String,
    val images: List<String> = emptyList(),
    val pricePerSession: Long,
    val capacity: Int,
    val rating: Double,
    val totalBookings: Int = 0,
    val status: String,
    val isEnabled: Boolean = true
)

@JsonClass(generateAdapter = true)
data class PaginationDto(
    val page: Int,
    val limit: Int,
    val total: Int,
    val pages: Int
)

@JsonClass(generateAdapter = true)
data class VenuesResponseDto(
    val venues: List<VenueDto>,
    val pagination: PaginationDto
)

@JsonClass(generateAdapter = true)
data class BookingVenueSummaryDto(
    @Json(name = "_id") val id: String? = null,
    val name: String,
    val region: String? = null,
    val district: String? = null
)

@JsonClass(generateAdapter = true)
data class BookingDto(
    @Json(name = "_id") val id: String,
    val venue: BookingVenueSummaryDto?,
    val clientName: String,
    val clientPhone: String,
    val date: String,
    val sessions: List<String>,
    val status: String,
    val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class CreateBookingRequestDto(
    val venueId: String,
    val clientName: String? = null,
    val clientPhone: String? = null,
    val date: String,
    val sessions: List<String>
)

@JsonClass(generateAdapter = true)
data class ReviewDto(
    @Json(name = "_id") val id: String,
    val venue: String,
    val authorName: String,
    val rating: Int,
    val comment: String,
    val createdAt: String
)

@JsonClass(generateAdapter = true)
data class CreateReviewRequestDto(
    val venueId: String,
    val authorName: String,
    val rating: Int,
    val comment: String
)

@JsonClass(generateAdapter = true)
data class CalendarDayDto(
    val booked: List<String> = emptyList(),
    val available: List<String> = emptyList(),
    val status: String
)

@JsonClass(generateAdapter = true)
data class CalendarResponseDto(
    val month: String,
    val calendar: Map<String, CalendarDayDto>
)

@JsonClass(generateAdapter = true)
data class ApiErrorDto(
    val message: String,
    val errors: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class HealthResponseDto(
    val status: String
)
