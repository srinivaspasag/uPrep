package `in`.uprep.app.ui.doubts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import `in`.uprep.app.data.api.DoubtAnswer
import `in`.uprep.app.data.api.DoubtDetail
import `in`.uprep.app.data.api.LearnApi
import `in`.uprep.app.data.api.PostAnswerBody
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DoubtDetailUiState(
    val doubt: DoubtDetail? = null,
    val answers: List<DoubtAnswer> = emptyList(),
    val aiPending: Boolean = false,
    val loading: Boolean = true,
    val error: String? = null,
    val sending: Boolean = false,
    val askingAira: Boolean = false
)

class DoubtDetailViewModel(private val learnApi: LearnApi, private val doubtId: String) : ViewModel() {
    private val _state = MutableStateFlow(DoubtDetailUiState())
    val state: StateFlow<DoubtDetailUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            try {
                val resp = learnApi.doubtDetail(doubtId)
                val body = resp.body()
                if (resp.isSuccessful && body?.doubt != null) {
                    _state.value = _state.value.copy(
                        doubt = body.doubt, answers = body.answers, aiPending = body.aiPending, loading = false
                    )
                } else {
                    _state.value = _state.value.copy(loading = false, error = body?.error ?: "Couldn't load this doubt")
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(loading = false, error = "Couldn't reach the server")
            }
        }
    }

    fun sendAnswer(content: String) {
        if (content.isBlank()) return
        _state.value = _state.value.copy(sending = true)
        viewModelScope.launch {
            try {
                learnApi.postAnswer(doubtId, PostAnswerBody(content))
            } catch (_: Exception) {
                // Ignored — load() below re-syncs from the server either way.
            } finally {
                _state.value = _state.value.copy(sending = false)
                load()
            }
        }
    }

    fun askAira() {
        _state.value = _state.value.copy(askingAira = true)
        viewModelScope.launch {
            try {
                learnApi.askAira(doubtId)
            } catch (_: Exception) {
            } finally {
                _state.value = _state.value.copy(askingAira = false)
                load()
            }
        }
    }

    class Factory(private val learnApi: LearnApi, private val doubtId: String) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = DoubtDetailViewModel(learnApi, doubtId) as T
    }
}
