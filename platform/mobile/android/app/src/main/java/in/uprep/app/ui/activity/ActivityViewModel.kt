package `in`.uprep.app.ui.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import `in`.uprep.app.data.api.ActivityFeedItem
import `in`.uprep.app.data.api.LearnApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ActivityUiState(
    val feed: List<ActivityFeedItem> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null
)

class ActivityViewModel(private val learnApi: LearnApi) : ViewModel() {
    private val _state = MutableStateFlow(ActivityUiState())
    val state: StateFlow<ActivityUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            try {
                val resp = learnApi.activity()
                if (resp.isSuccessful) {
                    _state.value = _state.value.copy(feed = resp.body()?.feed ?: emptyList(), loading = false)
                } else {
                    _state.value = _state.value.copy(loading = false, error = "Couldn't load recent activity")
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(loading = false, error = "Couldn't reach the server")
            }
        }
    }

    class Factory(private val learnApi: LearnApi) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ActivityViewModel(learnApi) as T
    }
}
