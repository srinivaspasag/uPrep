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
    val activateError: String? = null,
    // True when this device WAS activated but its 1-year access window has
    // since passed (checked fully offline against the cached expiry) — a
    // distinct state from never-activated, so the UI can explain what
    // happened instead of just asking to activate again from scratch.
    val expired: Boolean = false,
    // Plain-mode cards have no access code to redeem at all, so an expired
    // one is a dead end, not a "re-enter your code" prompt — kept separate
    // from `expired` (which drives the encrypted-mode ActivateForm) so the
    // UI can show a plain blocking message instead of a form with nothing
    // to submit.
    val plainExpired: Boolean = false
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
            // "plain" packaging has no key to protect — there's nothing an
            // online activation step would be gating, so requiring one just
            // blocks offline-first use (a tablet provisioned once, never
            // online again) for no security benefit. Only "encrypted" cards
            // need the real access-code verify flow below.
            val isPlain = manifest?.packagingMode == "plain"
            val hasKey = manifest?.groupId?.let { repository.hasKeyFor(it) } ?: false
            val encryptedExpired = hasKey && manifest?.groupId?.let { repository.isExpired(it) } == true
            // Plain mode has no server/key to check against — just the
            // manifest's own expiryDate against the device's local clock.
            val plainExpired = isPlain && manifest?.expiryDate?.let { System.currentTimeMillis() > it } == true
            _state.value = SdCardUiState(
                loading = false,
                folderPicked = folderUri != null,
                manifest = manifest,
                activated = (isPlain && !plainExpired) || (hasKey && !encryptedExpired),
                expired = !isPlain && encryptedExpired,
                plainExpired = plainExpired
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
