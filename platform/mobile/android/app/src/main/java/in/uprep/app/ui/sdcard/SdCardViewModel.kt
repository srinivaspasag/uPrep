package `in`.uprep.app.ui.sdcard

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import `in`.uprep.app.data.api.SellerApi
import `in`.uprep.app.data.sdcard.ActivateResult
import `in`.uprep.app.data.sdcard.DeviceIdStore
import `in`.uprep.app.data.sdcard.SdCardManifest
import `in`.uprep.app.data.sdcard.SdCardRepository
import `in`.uprep.app.data.session.SessionStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SdCardUiState(
    val loading: Boolean = true,
    val folderPicked: Boolean = false,
    val manifest: SdCardManifest? = null,
    val activated: Boolean = false,
    val activating: Boolean = false,
    val activateError: String? = null
)

// Drives the whole card lifecycle: pick a folder -> (if a manifest is
// there but this device hasn't redeemed a code for it yet) activate once
// online -> browse/play, fully offline, from then on. Mirrors legacy's
// real sequence (see AccessCodeManager parity note) minus the desktop
// flashing tool, since content now arrives pre-packaged in the zip.
class SdCardViewModel(
    private val repository: SdCardRepository,
    private val sellerApi: SellerApi,
    private val deviceIdStore: DeviceIdStore,
    private val sessionStore: SessionStore
) : ViewModel() {
    private val _state = MutableStateFlow(SdCardUiState())
    val state: StateFlow<SdCardUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            val folderUri = repository.savedFolderUri()
            val manifest = if (folderUri != null) repository.readManifest() else null
            val activated = manifest?.groupId?.let { repository.hasKeyFor(it) } ?: false
            _state.value = SdCardUiState(
                loading = false,
                folderPicked = folderUri != null,
                manifest = manifest,
                activated = activated
            )
        }
    }

    fun onFolderPicked(uri: Uri) {
        viewModelScope.launch {
            repository.saveFolderUri(uri)
            refresh()
        }
    }

    fun activate(code: String, email: String) {
        val groupId = _state.value.manifest?.groupId ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(activating = true, activateError = null)
            val userId = sessionStore.current()?.id
            if (userId == null) {
                _state.value = _state.value.copy(activating = false, activateError = "Log in first, then activate this card.")
                return@launch
            }
            val deviceId = deviceIdStore.get()
            when (val result = repository.activate(sellerApi, groupId, code.trim(), email.trim(), deviceId, userId)) {
                is ActivateResult.Success -> {
                    _state.value = _state.value.copy(activating = false, activated = true)
                }
                is ActivateResult.Failed -> {
                    _state.value = _state.value.copy(activating = false, activateError = result.message)
                }
            }
        }
    }

    class Factory(
        private val repository: SdCardRepository,
        private val sellerApi: SellerApi,
        private val deviceIdStore: DeviceIdStore,
        private val sessionStore: SessionStore
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SdCardViewModel(repository, sellerApi, deviceIdStore, sessionStore) as T
    }
}
