package uz.buron.data.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import uz.buron.data.dto.AuthResponseDto
import uz.buron.data.dto.BookingDto
import uz.buron.data.dto.CalendarResponseDto
import uz.buron.data.dto.CreateBookingRequestDto
import uz.buron.data.dto.CreateReviewRequestDto
import uz.buron.data.dto.HealthResponseDto
import uz.buron.data.dto.LoginRequestDto
import uz.buron.data.dto.RegisterRequestDto
import uz.buron.data.dto.ReviewDto
import uz.buron.data.dto.UserDto
import uz.buron.data.dto.VenueDto
import uz.buron.data.dto.VenuesResponseDto

interface BuronApiService {

    @GET("health")
    suspend fun health(): HealthResponseDto

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequestDto): AuthResponseDto

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequestDto): AuthResponseDto

    @GET("auth/me")
    suspend fun me(): UserDto

    @GET("venues")
    suspend fun getVenues(
        @Query("q") query: String? = null,
        @Query("region") region: String? = null,
        @Query("minPrice") minPrice: Long? = null,
        @Query("maxPrice") maxPrice: Long? = null,
        @Query("minCapacity") minCapacity: Int? = null,
        @Query("minRating") minRating: Double? = null,
        @Query("sort") sort: String? = null,
        @Query("order") order: String? = null,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null
    ): VenuesResponseDto

    @GET("venues/{id}")
    suspend fun getVenue(@Path("id") id: String): VenueDto

    @GET("bookings/venue/{venueId}/calendar")
    suspend fun getCalendar(
        @Path("venueId") venueId: String,
        @Query("month") month: String
    ): CalendarResponseDto

    @POST("bookings")
    suspend fun createBooking(@Body body: CreateBookingRequestDto): BookingDto

    @GET("bookings/my")
    suspend fun getMyBookings(): List<BookingDto>

    @PUT("bookings/{id}/cancel")
    suspend fun cancelBooking(@Path("id") id: String): BookingDto

    @GET("reviews/venue/{venueId}")
    suspend fun getVenueReviews(@Path("venueId") venueId: String): List<ReviewDto>

    @POST("reviews")
    suspend fun createReview(@Body body: CreateReviewRequestDto): ReviewDto
}
