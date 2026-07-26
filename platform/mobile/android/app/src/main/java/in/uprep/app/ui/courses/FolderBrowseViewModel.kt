package `in`.uprep.app.ui.courses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import `in`.uprep.app.data.api.ContentItem
import `in`.uprep.app.data.api.FolderInfo
import `in`.uprep.app.data.api.LearnApi
import `in`.uprep.app.data.api.SubfolderInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FolderBrowseUiState(
    val folder: FolderInfo? = null,
    val subfolders: List<SubfolderInfo> = emptyList(),
    val items: List<ContentItem> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null
)

class FolderBrowseViewModel(
    private val learnApi: LearnApi,
    private val folderId: String
) : ViewModel() {
    private val _state = MutableStateFlow(FolderBrowseUiState())
    val state: StateFlow<FolderBrowseUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            try {
                val resp = learnApi.browseFolder(folderId)
                if (resp.isSuccessful) {
                    val body = resp.body()
                    _state.value = FolderBrowseUiState(
                        folder = body?.folder,
                        subfolders = body?.subfolders ?: emptyList(),
                        items = body?.items ?: emptyList(),
                        loading = false
                    )
                } else {
                    _state.value = _state.value.copy(loading = false, error = "Couldn't load this folder")
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(loading = false, error = "Couldn't reach the server")
            }
        }
    }

    class Factory(
        private val learnApi: LearnApi,
        private val folderId: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            FolderBrowseViewModel(learnApi, folderId) as T
    }
}
