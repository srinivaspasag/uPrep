package `in`.uprep.app.ui.courses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import `in`.uprep.app.data.api.CourseSummary
import `in`.uprep.app.data.api.LearnApi
import `in`.uprep.app.data.api.ProgramGroup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CoursesUiState(
    val courses: List<CourseSummary> = emptyList(),
    val programGroups: List<ProgramGroup> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null
)

class CoursesViewModel(private val learnApi: LearnApi) : ViewModel() {
    private val _state = MutableStateFlow(CoursesUiState())
    val state: StateFlow<CoursesUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            try {
                val resp = learnApi.myCourses()
                if (resp.isSuccessful) {
                    _state.value = CoursesUiState(
                        courses = resp.body()?.courses ?: emptyList(),
                        programGroups = resp.body()?.programGroups ?: emptyList(),
                        loading = false
                    )
                } else {
                    _state.value = _state.value.copy(loading = false, error = "Couldn't load your courses")
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(loading = false, error = "Couldn't reach the server")
            }
        }
    }

    class Factory(private val learnApi: LearnApi) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = CoursesViewModel(learnApi) as T
    }
}
