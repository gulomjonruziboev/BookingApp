package uz.buron.data.repository

import com.squareup.moshi.Moshi
import uz.buron.data.api.BuronApiService
import uz.buron.data.api.safeApiCall
import uz.buron.data.dto.CreateReviewRequestDto
import uz.buron.data.dto.toDomain
import uz.buron.domain.model.Review
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewRepository @Inject constructor(
    private val api: BuronApiService,
    private val moshi: Moshi
) {
    suspend fun getVenueReviews(venueId: String): Result<List<Review>> {
        return safeApiCall(moshi) {
            api.getVenueReviews(venueId).map { it.toDomain() }
        }
    }

    suspend fun createReview(
        venueId: String,
        authorName: String,
        rating: Int,
        comment: String
    ): Result<Review> {
        return safeApiCall(moshi) {
            api.createReview(
                CreateReviewRequestDto(
                    venueId = venueId,
                    authorName = authorName,
                    rating = rating,
                    comment = comment
                )
            ).toDomain()
        }
    }
}
