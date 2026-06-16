package uz.buron.ui.screens.auth

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
import uz.buron.util.PhoneUtils
import uz.buron.util.Validation
import javax.inject.Inject

data class LoginUiState(
    val phoneDigits: String = "",
    val password: String = "",
    val phoneError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onPhoneChange(digits: String) {
        _uiState.update { it.copy(phoneDigits = digits, phoneError = null, error = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null, error = null) }
    }

    fun login() {
        val state = _uiState.value
        val phone = PhoneUtils.normalizePhoneUz("+998${state.phoneDigits}")
        val phoneError = PhoneUtils.validatePhoneUz(phone)
        val passwordError = if (state.password.isBlank()) "Parol talab qilinadi" else null

        if (phoneError != null || passwordError != null) {
            _uiState.update { it.copy(phoneError = phoneError, passwordError = passwordError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.login(phone, state.password)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, success = true) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = mapError(error)
                        )
                    }
                }
        }
    }

    private fun mapError(error: Throwable): String = when (error) {
        is ApiException.Unauthorized -> "Invalid credentials"
        is ApiException.Client -> error.message ?: "Xatolik"
        is ApiException.Network -> "Internet aloqasi yo'q"
        is ApiException.RateLimited -> "Juda ko'p so'rov. Keyinroq urinib ko'ring."
        is ApiException.Server -> "Server xatosi. Keyinroq urinib ko'ring."
        else -> error.message ?: "Xatolik yuz berdi"
    }
}
