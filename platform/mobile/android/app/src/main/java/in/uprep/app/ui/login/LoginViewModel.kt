package `in`.uprep.app.ui.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import `in`.uprep.app.data.api.ApiError
import `in`.uprep.app.data.api.AuthApi
import `in`.uprep.app.data.api.LoginRequest
import `in`.uprep.app.data.session.SessionStore
import `in`.uprep.app.data.session.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val identifier: String = "",
    val password: String = "",
    val loading: Boolean = false,
    val error: String? = null
)

class LoginViewModel(
    private val authApi: AuthApi,
    private val sessionStore: SessionStore
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    private val _loggedInSession = MutableStateFlow<UserSession?>(null)
    val loggedInSession: StateFlow<UserSession?> = _loggedInSession.asStateFlow()

    fun onIdentifierChange(v: String) {
        _state.value = _state.value.copy(identifier = v, error = null)
    }

    fun onPasswordChange(v: String) {
        _state.value = _state.value.copy(password = v, error = null)
    }

    fun login() {
        val identifier = _state.value.identifier.trim()
        val password = _state.value.password
        if (identifier.isEmpty() || password.isEmpty()) {
            _state.value = _state.value.copy(error = "Enter your ID and password")
            return
        }
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            try {
                val resp = authApi.login(LoginRequest(identifier, password))
                if (resp.isSuccessful) {
                    val result = resp.body()?.result
                    if (result == null) {
                        _state.value = _state.value.copy(loading = false, error = "Unexpected response")
                        return@launch
                    }
                    val session = UserSession(
                        id = result.id,
                        orgId = result.orgId,
                        memberId = result.memberId,
                        firstName = result.firstName,
                        lastName = result.lastName,
                        profile = result.profile,
                        isSuperAdmin = result.isSuperAdmin
                    )
                    sessionStore.save(session)
                    _state.value = _state.value.copy(loading = false, error = null)
                    _loggedInSession.value = session
                } else {
                    val errBody = resp.errorBody()?.string()
                    val message = try {
                        Gson().fromJson(errBody, ApiError::class.java)?.errorMessage
                    } catch (e: Exception) {
                        null
                    } ?: "Sign in failed"
                    _state.value = _state.value.copy(loading = false, error = message)
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Login failed", e)
                _state.value = _state.value.copy(
                    loading = false,
                    error = "Couldn't reach the server — check your connection (${e.javaClass.simpleName}: ${e.message})"
                )
            }
        }
    }

    class Factory(
        private val authApi: AuthApi,
        private val sessionStore: SessionStore
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            LoginViewModel(authApi, sessionStore) as T
    }
}
