package `in`.uprep.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import `in`.uprep.app.data.api.ChangePasswordBody
import `in`.uprep.app.data.api.ChangePasswordResponse
import `in`.uprep.app.data.api.LearnApi
import `in`.uprep.app.data.api.StudentProfile
import `in`.uprep.app.data.session.SessionStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val profile: StudentProfile? = null,
    val loading: Boolean = true,
    val loadError: String? = null,
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val saving: Boolean = false,
    val passwordError: String? = null,
    val passwordChanged: Boolean = false
)

class ProfileViewModel(
    private val learnApi: LearnApi,
    private val sessionStore: SessionStore
) : ViewModel() {
    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.value = _state.value.copy(loading = true, loadError = null)
        viewModelScope.launch {
            // Name/ID are always available from the saved session, so the page
            // still shows who is signed in when the profile call fails offline.
            val saved = sessionStore.current()
            val fallback = saved?.let {
                StudentProfile(id = it.id, memberId = it.memberId ?: "", firstName = it.firstName, lastName = it.lastName)
            }
            try {
                val resp = learnApi.myProfile()
                val p = if (resp.isSuccessful) resp.body()?.profile else null
                _state.value = _state.value.copy(
                    profile = p ?: fallback,
                    loading = false,
                    loadError = if (p == null) "Couldn't load all your details" else null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(profile = fallback, loading = false, loadError = "You're offline — showing saved details")
            }
        }
    }

    fun onCurrent(v: String) { _state.value = _state.value.copy(currentPassword = v, passwordError = null, passwordChanged = false) }
    fun onNew(v: String) { _state.value = _state.value.copy(newPassword = v, passwordError = null, passwordChanged = false) }
    fun onConfirm(v: String) { _state.value = _state.value.copy(confirmPassword = v, passwordError = null, passwordChanged = false) }

    fun changePassword() {
        val s = _state.value
        if (s.saving) return
        // Same rules the server enforces (app/api/learn/password), checked
        // here first so the student gets an answer without a round-trip.
        val problem = when {
            s.currentPassword.isEmpty() -> "Enter your current password"
            s.newPassword.length < 6 -> "New password must be at least 6 characters"
            s.newPassword != s.confirmPassword -> "New passwords don't match"
            s.newPassword == s.currentPassword -> "New password must be different from the current one"
            else -> null
        }
        if (problem != null) {
            _state.value = s.copy(passwordError = problem)
            return
        }
        _state.value = s.copy(saving = true, passwordError = null, passwordChanged = false)
        viewModelScope.launch {
            try {
                val resp = learnApi.changePassword(ChangePasswordBody(s.currentPassword, s.newPassword))
                if (resp.isSuccessful && resp.body()?.ok == true) {
                    sessionStore.updateOfflinePassword(s.newPassword)
                    _state.value = _state.value.copy(
                        saving = false, passwordChanged = true,
                        currentPassword = "", newPassword = "", confirmPassword = ""
                    )
                } else {
                    val msg = resp.body()?.error ?: runCatching {
                        Gson().fromJson(resp.errorBody()?.string(), ChangePasswordResponse::class.java)?.error
                    }.getOrNull()
                    _state.value = _state.value.copy(saving = false, passwordError = msg ?: "Couldn't change your password. Try again.")
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(saving = false, passwordError = "No internet connection. Connect and try again.")
            }
        }
    }

    class Factory(private val learnApi: LearnApi, private val sessionStore: SessionStore) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ProfileViewModel(learnApi, sessionStore) as T
    }
}
