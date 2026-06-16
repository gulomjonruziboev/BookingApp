package uz.buron.data.dto

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import uz.buron.domain.model.AuthResult
import uz.buron.domain.model.Booking
import uz.buron.domain.model.BookingVenueSummary
import uz.buron.domain.model.CalendarDay
import uz.buron.domain.model.CalendarResponse
import uz.buron.domain.model.Location
import uz.buron.domain.model.Pagination
import uz.buron.domain.model.Review
import uz.buron.domain.model.User
import uz.buron.domain.model.Venue
import uz.buron.domain.model.VenueOwner
import uz.buron.domain.model.VenuesResponse

fun UserDto.toDomain(): User = User(
    id = id,
    role = role,
    firstName = firstName,
    lastName = lastName,
    phone = phone,
    telegram = telegram,
    isEnabled = isEnabled
)

fun AuthResponseDto.toDomain(): AuthResult = AuthResult(
    token = token,
    user = user.toDomain()
)

fun VenueOwnerDto.toDomain(): VenueOwner = VenueOwner(
    id = id,
    firstName = firstName,
    lastName = lastName,
    phone = phone,
    telegram = telegram
)

fun LocationDto.toDomain(): Location = Location(lat = lat, lng = lng)

fun VenueDto.toDomain(): Venue = Venue(
    id = id,
    owner = owner.toDomain(),
    name = name,
    description = description,
    address = address,
    mapLink = mapLink,
    location = location?.toDomain(),
    region = region,
    district = district,
    phone = phone,
    images = images,
    pricePerSession = pricePerSession,
    capacity = capacity,
    rating = rating,
    totalBookings = totalBookings,
    status = status,
    isEnabled = isEnabled
)

fun PaginationDto.toDomain(): Pagination = Pagination(
    page = page,
    limit = limit,
    total = total,
    pages = pages
)

fun VenuesResponseDto.toDomain(): VenuesResponse = VenuesResponse(
    venues = venues.map { it.toDomain() },
    pagination = pagination.toDomain()
)

fun BookingVenueSummaryDto.toDomain(): BookingVenueSummary = BookingVenueSummary(
    id = id.orEmpty(),
    name = name,
    region = region,
    district = district
)

fun BookingDto.toDomain(): Booking {
    val venueSummary = venue?.toDomain()
        ?: BookingVenueSummary(id = "", name = "Noma'lum", region = null, district = null)

    return Booking(
        id = id,
        venue = venueSummary,
        clientName = clientName,
        clientPhone = clientPhone,
        date = date,
        sessions = sessions,
        status = status,
        createdAt = createdAt
    )
}

fun ReviewDto.toDomain(): Review = Review(
    id = id,
    venue = venue,
    authorName = authorName,
    rating = rating,
    comment = comment,
    createdAt = createdAt
)

fun CalendarDayDto.toDomain(): CalendarDay = CalendarDay(
    booked = booked,
    available = available,
    status = status
)

fun CalendarResponseDto.toDomain(): CalendarResponse = CalendarResponse(
    month = month,
    calendar = calendar.mapValues { it.value.toDomain() }
)

fun createMoshi(): Moshi = Moshi.Builder()
    .add(BookingVenueRefAdapter())
    .add(KotlinJsonAdapterFactory())
    .build()
