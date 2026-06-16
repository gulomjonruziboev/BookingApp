package uz.buron.data.repository

import com.squareup.moshi.Moshi
import uz.buron.data.api.BuronApiService
import uz.buron.data.api.safeApiCall
import uz.buron.data.dto.toDomain
import uz.buron.domain.model.Venue
import uz.buron.domain.model.VenuesResponse
import javax.inject.Inject
import javax.inject.Singleton

data class VenueSearchParams(
    val query: String? = null,
    val region: String? = null,
    val minPrice: Long? = null,
    val maxPrice: Long? = null,
    val minCapacity: Int? = null,
    val minRating: Double? = null,
    val sort: String? = null,
    val order: String? = null,
    val page: Int = 1,
    val limit: Int = 12
)

@Singleton
class VenueRepository @Inject constructor(
    private val api: BuronApiService,
    private val moshi: Moshi
) {
    suspend fun getVenues(params: VenueSearchParams): Result<VenuesResponse> {
        return safeApiCall(moshi) {
            api.getVenues(
                query = params.query?.takeIf { it.isNotBlank() },
                region = params.region?.takeIf { it.isNotBlank() },
                minPrice = params.minPrice,
                maxPrice = params.maxPrice,
                minCapacity = params.minCapacity,
                minRating = params.minRating,
                sort = params.sort,
                order = params.order,
                page = params.page,
                limit = params.limit
            ).toDomain()
        }
    }

    suspend fun getVenue(id: String): Result<Venue> {
        return safeApiCall(moshi) {
            api.getVenue(id).toDomain()
        }
    }

    suspend fun getPopularVenues(region: String? = null, limit: Int = 6): Result<List<Venue>> {
        return getVenues(
            VenueSearchParams(
                region = region,
                sort = "rating",
                order = "desc",
                page = 1,
                limit = limit
            )
        ).map { it.venues }
    }
}
