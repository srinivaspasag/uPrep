package `in`.uprep.app.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import `in`.uprep.app.data.api.AnalyticsResponse
import `in`.uprep.app.data.api.LearnApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AnalyticsUiState(
    val data: AnalyticsResponse? = null,
    val loading: Boolean = true,
    val error: String? = null
)

class AnalyticsViewModel(private val learnApi: LearnApi) : ViewModel() {
    private val _state = MutableStateFlow(AnalyticsUiState())
    val state: StateFlow<AnalyticsUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            try {
                val resp = learnApi.analytics()
                if (resp.isSuccessful) {
                    _state.value = _state.value.copy(data = resp.body(), loading = false)
                } else {
                    _state.value = _state.value.copy(loading = false, error = "Couldn't load analytics")
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(loading = false, error = "Couldn't reach the server")
            }
        }
    }

    class Factory(private val learnApi: LearnApi) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = AnalyticsViewModel(learnApi) as T
    }
}
