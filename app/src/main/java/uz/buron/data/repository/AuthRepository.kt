package uz.buron.data.repository

import com.squareup.moshi.Moshi
import uz.buron.data.api.ApiException
import uz.buron.data.api.BuronApiService
import uz.buron.data.api.safeApiCall
import uz.buron.data.dto.LoginRequestDto
import uz.buron.data.dto.RegisterRequestDto
import uz.buron.data.dto.toDomain
import uz.buron.data.local.TokenStore
import uz.buron.domain.model.AuthResult
import uz.buron.domain.model.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: BuronApiService,
    private val tokenStore: TokenStore,
    private val moshi: Moshi
) {
    val authState = tokenStore.authState

    suspend fun validateSession(): Result<User?> {
        if (!tokenStore.isLoggedIn()) return Result.success(null)
        return safeApiCall(moshi) {
            val user = api.me().toDomain()
            if (user.role != CLIENT_ROLE) {
                tokenStore.clear()
                throw ApiException.Client(CLIENT_ONLY_MESSAGE, 403)
            }
            tokenStore.saveSession(tokenStore.getToken()!!, user)
            user
        }
    }

    suspend fun login(phone: String, password: String): Result<User> {
        return safeApiCall(moshi) {
            val response = api.login(LoginRequestDto(phone = phone, password = password))
            if (response.user.role != CLIENT_ROLE) {
                throw ApiException.Client(CLIENT_ONLY_MESSAGE, 403)
            }
            val auth = response.toDomain()
            tokenStore.saveSession(auth.token, auth.user)
            auth.user
        }
    }

    suspend fun register(
        firstName: String,
        lastName: String,
        phone: String,
        password: String
    ): Result<User> {
        return safeApiCall(moshi) {
            val response = api.register(
                RegisterRequestDto(
                    role = CLIENT_ROLE,
                    firstName = firstName,
                    lastName = lastName,
                    phone = phone,
                    password = password
                )
            )
            if (response.user.role != CLIENT_ROLE) {
                throw ApiException.Client(CLIENT_ONLY_MESSAGE, 403)
            }
            val auth = response.toDomain()
            tokenStore.saveSession(auth.token, auth.user)
            auth.user
        }
    }

    fun logout() {
        tokenStore.clear()
    }

    fun getCurrentUser(): User? = tokenStore.getUser()

    fun isLoggedIn(): Boolean = tokenStore.isLoggedIn()

    companion object {
        const val CLIENT_ROLE = "client"
        const val CLIENT_ONLY_MESSAGE = "Bu ilova faqat mijozlar uchun"
    }
}
