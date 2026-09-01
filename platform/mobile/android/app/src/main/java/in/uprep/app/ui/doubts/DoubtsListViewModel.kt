package `in`.uprep.app.ui.doubts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import `in`.uprep.app.data.api.CreateDoubtBody
import `in`.uprep.app.data.api.DoubtSummary
import `in`.uprep.app.data.api.LearnApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DoubtsUiState(
    val doubts: List<DoubtSummary> = emptyList(),
    val filter: String = "open",
    val loading: Boolean = true,
    val error: String? = null,
    val creating: Boolean = false,
    val createError: String? = null
)

class DoubtsListViewModel(private val learnApi: LearnApi) : ViewModel() {
    private val _state = MutableStateFlow(DoubtsUiState())
    val state: StateFlow<DoubtsUiState> = _state.asStateFlow()

    init { load() }

    fun setFilter(filter: String) {
        _state.value = _state.value.copy(filter = filter)
        load()
    }

    fun load() {
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            try {
                val resp = learnApi.myDoubts(_state.value.filter)
                if (resp.isSuccessful) {
                    _state.value = _state.value.copy(doubts = resp.body()?.items ?: emptyList(), loading = false)
                } else {
                    _state.value = _state.value.copy(loading = false, error = "Couldn't load your doubts")
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(loading = false, error = "Couldn't reach the server")
            }
        }
    }

    fun createDoubt(title: String, content: String, onCreated: (String) -> Unit) {
        if (title.isBlank()) {
            _state.value = _state.value.copy(createError = "A question title is required")
            return
        }
        _state.value = _state.value.copy(creating = true, createError = null)
        viewModelScope.launch {
            try {
                val resp = learnApi.createDoubt(CreateDoubtBody(name = title, content = content))
                val id = resp.body()?.id
                if (resp.isSuccessful && id != null) {
                    _state.value = _state.value.copy(creating = false)
                    load()
                    onCreated(id)
                } else {
                    _state.value = _state.value.copy(creating = false, createError = resp.body()?.error ?: "Failed to post doubt")
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(creating = false, createError = "Couldn't reach the server")
            }
        }
    }

    fun clearCreateError() {
        _state.value = _state.value.copy(createError = null)
    }

    class Factory(private val learnApi: LearnApi) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = DoubtsListViewModel(learnApi) as T
    }
}
