package `in`.uprep.app.ui.offline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import `in`.uprep.app.data.offline.CachedQuestion
import `in`.uprep.app.data.offline.CachedTest
import `in`.uprep.app.data.offline.OfflineTestRepository
import `in`.uprep.app.data.offline.SyncResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NativeTestUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val test: CachedTest? = null,
    val questions: List<CachedQuestion> = emptyList(),
    val currentIndex: Int = 0,
    val answers: Map<String, List<String>> = emptyMap(),
    val marked: Set<String> = emptySet(),
    val visited: Set<String> = emptySet(),
    val remainingSeconds: Int = 0,
    val submitting: Boolean = false,
    val submitted: Boolean = false,
    val submitError: String? = null
)

// Native (no WebView — WebView can't load anything with no connectivity)
// test-taking for a downloaded test. Essential subset of app/test/[id]/
// page.tsx's behavior only: palette, timer, single/multi-select answering,
// mark-for-review, submit. Deliberately NOT in scope: server-side autosave
// (meaningless offline — local Room rows are the autosave), rendering
// richness beyond plain question text.
//
// Per-question time tracking ports page.tsx's qTimeRef/activeRef/
// flushActive pattern directly: `qTime` accumulates seconds per question,
// `active` tracks which question is currently open and since when,
// `flushActive()` credits elapsed time to the question being left and is
// called on every navigation plus once more before submit.
class NativeTestViewModel(
    private val testId: String,
    private val repository: OfflineTestRepository
) : ViewModel() {
    private val _state = MutableStateFlow(NativeTestUiState())
    val state: StateFlow<NativeTestUiState> = _state.asStateFlow()

    private var attemptLocalId: Long = -1
    private val qTime = mutableMapOf<String, Int>()
    private var activeQId: String? = null
    private var activeSinceMillis: Long = 0L
    private var deadlineMillis: Long = 0L

    init {
        viewModelScope.launch {
            val cached = repository.getTestWithQuestions(testId)
            if (cached == null) {
                _state.value = _state.value.copy(loading = false, error = "This test isn't downloaded.")
                return@launch
            }
            attemptLocalId = repository.startOrResumeAttempt(testId)
            val existingAnswers = repository.getAnswers(attemptLocalId)
            val answersMap = mutableMapOf<String, List<String>>()
            val markedSet = mutableSetOf<String>()
            val visitedSet = mutableSetOf<String>()
            for (a in existingAnswers) {
                qTime[a.qId] = a.timeTakenSeconds
                visitedSet.add(a.qId)
                if (a.marked) markedSet.add(a.qId)
                val given = runCatching {
                    com.google.gson.Gson().fromJson(a.answerGivenJson, Array<String>::class.java).toList()
                }.getOrDefault(emptyList())
                if (given.isNotEmpty()) answersMap[a.qId] = given
            }

            val durationSeconds = cached.test.durationMin * 60
            // Resuming an attempt already in progress consumes the elapsed
            // wall-clock time against the original deadline, same as the
            // web client's resumed countdown — a killed app doesn't grant
            // extra time.
            val attempt = repository.getPendingSyncAttempts().firstOrNull { it.localId == attemptLocalId }
            val startedAt = attempt?.startedAt ?: System.currentTimeMillis()
            deadlineMillis = startedAt + durationSeconds * 1000L
            val remaining = ((deadlineMillis - System.currentTimeMillis()) / 1000L).toInt().coerceAtLeast(0)

            _state.value = NativeTestUiState(
                loading = false,
                test = cached.test,
                questions = cached.questions,
                answers = answersMap,
                marked = markedSet,
                visited = visitedSet,
                remainingSeconds = remaining
            )

            if (cached.questions.isNotEmpty()) {
                activeQId = cached.questions.first().id
                activeSinceMillis = System.currentTimeMillis()
            }

            runCountdown()
        }
    }

    private suspend fun runCountdown() {
        while (_state.value.remainingSeconds > 0 && !_state.value.submitted) {
            delay(1000)
            val remaining = ((deadlineMillis - System.currentTimeMillis()) / 1000L).toInt().coerceAtLeast(0)
            _state.value = _state.value.copy(remainingSeconds = remaining)
            if (remaining <= 0) {
                submit()
                return
            }
        }
    }

    private fun flushActive() {
        val qId = activeQId ?: return
        val elapsed = ((System.currentTimeMillis() - activeSinceMillis) / 1000L).toInt().coerceAtLeast(0)
        qTime[qId] = (qTime[qId] ?: 0) + elapsed
        activeSinceMillis = System.currentTimeMillis()
        persistAnswer(qId)
    }

    private fun persistAnswer(qId: String) {
        if (attemptLocalId < 0) return
        val answer = _state.value.answers[qId] ?: emptyList()
        val marked = _state.value.marked.contains(qId)
        val time = qTime[qId] ?: 0
        viewModelScope.launch {
            repository.recordAnswer(attemptLocalId, qId, answer, time, marked)
        }
    }

    fun goTo(index: Int) {
        val questions = _state.value.questions
        if (index !in questions.indices) return
        flushActive()
        activeQId = questions[index].id
        activeSinceMillis = System.currentTimeMillis()
        _state.value = _state.value.copy(
            currentIndex = index,
            visited = _state.value.visited + questions[index].id
        )
    }

    fun next() = goTo(_state.value.currentIndex + 1)
    fun previous() = goTo(_state.value.currentIndex - 1)

    // Single-choice ("SCQ") replaces the selection outright; anything else
    // is treated as multi-select and toggles membership — same two shapes
    // app/test/[id]/page.tsx's answerGiven handling supports.
    fun selectOption(qId: String, optionIndex: String) {
        val question = _state.value.questions.find { it.id == qId } ?: return
        val current = _state.value.answers[qId] ?: emptyList()
        val updated = if (question.type.equals("SCQ", ignoreCase = true)) {
            listOf(optionIndex)
        } else if (current.contains(optionIndex)) {
            current - optionIndex
        } else {
            current + optionIndex
        }
        _state.value = _state.value.copy(answers = _state.value.answers + (qId to updated))
        persistAnswer(qId)
    }

    fun toggleMark(qId: String) {
        val marked = _state.value.marked
        _state.value = _state.value.copy(
            marked = if (marked.contains(qId)) marked - qId else marked + qId
        )
        persistAnswer(qId)
    }

    fun submit() {
        if (_state.value.submitting || _state.value.submitted) return
        flushActive()
        _state.value = _state.value.copy(submitting = true, submitError = null)
        viewModelScope.launch {
            when (val result = repository.finishAndSync(attemptLocalId)) {
                is SyncResult.Success -> _state.value = _state.value.copy(submitting = false, submitted = true)
                is SyncResult.Failed -> _state.value = _state.value.copy(
                    submitting = false,
                    // Still "submitted" from the student's point of view — it's
                    // saved locally and will sync automatically; this is not a
                    // failure they need to act on (see SyncAttemptWorker).
                    submitted = true,
                    submitError = result.message
                )
            }
        }
    }

    class Factory(
        private val testId: String,
        private val repository: OfflineTestRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            NativeTestViewModel(testId, repository) as T
    }
}
