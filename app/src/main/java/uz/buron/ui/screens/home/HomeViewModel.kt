package uz.buron.ui.screens.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uz.buron.data.api.ApiException
import uz.buron.data.repository.VenueRepository
import uz.buron.domain.model.Venue
import uz.buron.util.RegionDetector
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val venues: List<Venue> = emptyList(),
    val detectedRegion: String? = null,
    val error: String? = null,
    val searchQuery: String = ""
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val venueRepository: VenueRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadPopularVenues()
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun loadPopularVenues() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val region = detectRegionIfPermitted()
            venueRepository.getPopularVenues(region = region, limit = 6)
                .onSuccess { venues ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            venues = venues,
                            detectedRegion = region
                        )
                    }
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

    private var locationPermissionPromptDeclined = false

    fun onLocationPermissionDeclined() {
        locationPermissionPromptDeclined = true
    }

    fun shouldShowLocationPermissionPrompt(context: Context): Boolean {
        if (locationPermissionPromptDeclined || hasLocationPermission(context)) {
            return false
        }
        locationPermissionPromptDeclined = true
        return true
    }

    fun hasLocationPermission(context: Context): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineGranted || coarseGranted
    }

    private suspend fun detectRegionIfPermitted(): String? {
        return if (hasLocationPermission(context)) {
            RegionDetector.detectRegion(context)
        } else {
            null
        }
    }

    private fun mapError(error: Throwable): String = when (error) {
        is ApiException.Network -> "Internet aloqasi yo'q"
        is ApiException.RateLimited -> "Juda ko'p so'rov. Keyinroq urinib ko'ring."
        is ApiException.Server -> "Server xatosi. Keyinroq urinib ko'ring."
        is ApiException.Client -> error.message ?: "Xatolik"
        else -> error.message ?: "Xatolik yuz berdi"
    }
}
