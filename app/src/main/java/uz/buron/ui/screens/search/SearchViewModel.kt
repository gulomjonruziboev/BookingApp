package uz.buron.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uz.buron.data.api.ApiException
import uz.buron.data.repository.VenueRepository
import uz.buron.domain.model.Pagination
import uz.buron.domain.model.Venue
import uz.buron.ui.components.SearchFilters
import javax.inject.Inject

data class SearchUiState(
    val isLoading: Boolean = true,
    val query: String = "",
    val filters: SearchFilters = SearchFilters(),
    val venues: List<Venue> = emptyList(),
    val pagination: Pagination? = null,
    val error: String? = null,
    val showFilters: Boolean = false
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val venueRepository: VenueRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()
    private var searchJob: Job? = null

    fun initialize(query: String) {
        if (_uiState.value.query != query) {
            _uiState.update { it.copy(query = query) }
            search(page = 1)
        } else if (_uiState.value.venues.isEmpty()) {
            search(page = 1)
        }
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400)
            search(page = 1)
        }
    }

    fun onSearch() {
        search(page = 1)
    }

    fun onFiltersChange(filters: SearchFilters) {
        _uiState.update { it.copy(filters = filters) }
    }

    fun applyFilters() {
        _uiState.update { it.copy(showFilters = false) }
        search(page = 1)
    }

    fun resetFilters() {
        _uiState.update { it.copy(filters = SearchFilters()) }
        search(page = 1)
    }

    fun toggleFilters() {
        _uiState.update { it.copy(showFilters = !it.showFilters) }
    }

    fun nextPage() {
        val current = _uiState.value.pagination ?: return
        if (current.page < current.pages) {
            search(page = current.page + 1)
        }
    }

    fun prevPage() {
        val current = _uiState.value.pagination ?: return
        if (current.page > 1) {
            search(page = current.page - 1)
        }
    }

    private fun search(page: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val state = _uiState.value
            venueRepository.getVenues(state.filters.toParams(state.query, page))
                .onSuccess { response ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            venues = response.venues,
                            pagination = response.pagination
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, error = mapError(error))
                    }
                }
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
