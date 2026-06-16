package uz.buron.data.api

import okhttp3.Interceptor
import okhttp3.Response
import uz.buron.data.local.TokenStore
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenStore: TokenStore
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val token = tokenStore.getToken()
        val newRequest = if (!token.isNullOrBlank()) {
            request.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            request
        }
        return chain.proceed(newRequest)
    }
}
