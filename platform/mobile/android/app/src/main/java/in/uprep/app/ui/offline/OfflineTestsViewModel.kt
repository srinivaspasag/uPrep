package `in`.uprep.app.ui.offline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import `in`.uprep.app.data.offline.CachedTest
import `in`.uprep.app.data.offline.OfflineTestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class OfflineTestsUiState(
    val tests: List<CachedTest> = emptyList(),
    val pendingSyncCount: Int = 0
)

// Lists tests already downloaded for offline use (download itself happens
// from the course content list — see FolderBrowseScreen's download action —
// not from here) and surfaces how many finished attempts are still waiting
// to sync back to the server.
class OfflineTestsViewModel(
    private val repository: OfflineTestRepository
) : ViewModel() {
    private val _state = MutableStateFlow(OfflineTestsUiState())
    val state: StateFlow<OfflineTestsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(repository.observeDownloadedTests(), repository.observePendingSyncCount()) { tests, pending ->
                OfflineTestsUiState(tests = tests, pendingSyncCount = pending)
            }.collect { _state.value = it }
        }
    }

    class Factory(private val repository: OfflineTestRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            OfflineTestsViewModel(repository) as T
    }
}
