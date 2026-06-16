package uz.buron.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.squareup.moshi.Moshi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import uz.buron.data.dto.UserDto
import uz.buron.data.dto.toDomain
import uz.buron.domain.model.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenStore @Inject constructor(
    @ApplicationContext context: Context,
    private val moshi: Moshi
) {
    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val _authState = MutableStateFlow(loadAuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun getUser(): User? {
        val json = prefs.getString(KEY_USER, null) ?: return null
        return try {
            moshi.adapter(UserDto::class.java).fromJson(json)?.toDomain()
        } catch (_: Exception) {
            null
        }
    }

    fun isLoggedIn(): Boolean = !getToken().isNullOrBlank()

    fun saveSession(token: String, user: User) {
        val userJson = moshi.adapter(UserDto::class.java).toJson(
            UserDto(
                id = user.id,
                role = user.role,
                firstName = user.firstName,
                lastName = user.lastName,
                phone = user.phone,
                telegram = user.telegram,
                isEnabled = user.isEnabled
            )
        )
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_USER, userJson)
            .apply()
        _authState.value = AuthState(user = user, isLoggedIn = true)
    }

    fun clear() {
        prefs.edit().clear().apply()
        _authState.value = AuthState(user = null, isLoggedIn = false)
    }

    private fun loadAuthState(): AuthState {
        val user = getUser()
        val loggedIn = !getToken().isNullOrBlank() && user != null
        return AuthState(user = user, isLoggedIn = loggedIn)
    }

    data class AuthState(
        val user: User?,
        val isLoggedIn: Boolean
    )

    companion object {
        private const val PREFS_NAME = "buron_secure_prefs"
        private const val KEY_TOKEN = "buron_token"
        private const val KEY_USER = "buron_user"
    }
}
