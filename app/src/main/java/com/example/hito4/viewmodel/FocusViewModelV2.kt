package com.example.hito4.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hito4.data.entity.SubjectEntity
import com.example.hito4.data.repo.StudySessionRepository
import com.example.hito4.data.repo.SubjectRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.max

class FocusViewModelV2(
    private val subjectRepo: SubjectRepository,
    private val sessionRepo: StudySessionRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(FocusUiState())
    val ui: StateFlow<FocusUiState> = _ui.asStateFlow()

    private var timerJob: Job? = null

    init {
        // Observamos asignaturas desde Room y las metemos en el estado
        viewModelScope.launch {
            subjectRepo.observeSubjects().collect { list ->
                _ui.update { current ->
                    val selected = current.selectedSubject ?: list.firstOrNull()
                    current.copy(subjects = list, selectedSubject = selected)
                }
            }
        }
    }

    fun selectSubject(subject: SubjectEntity) {
        _ui.update { it.copy(selectedSubject = subject) }
    }

    fun changePlannedMinutes(delta: Int) {
        _ui.update { current ->
            if (current.isRunning) current
            else {
                val newMinutes = max(1, current.plannedMinutes + delta)
                current.copy(
                    plannedMinutes = newMinutes,
                    totalSeconds = newMinutes * 60,
                    remainingSeconds = newMinutes * 60,
                    isFinished = false,
                    startMillis = null
                )
            }
        }
    }

    fun setPlannedMinutes(minutes: Int) {
        _ui.update { current ->
            if (current.isRunning) current
            else {
                val newMinutes = max(1, minutes)
                current.copy(
                    plannedMinutes = newMinutes,
                    totalSeconds = newMinutes * 60,
                    remainingSeconds = newMinutes * 60,
                    isFinished = false,
                    startMillis = null
                )
            }
        }
    }

    fun start() {
        val current = _ui.value
        if (current.subjects.isEmpty()) return
        if (current.isRunning) return
        if (current.remainingSeconds <= 0) return

        // Si no hay asignatura seleccionada, seleccionamos la primera
        val selected = current.selectedSubject ?: current.subjects.first()

        _ui.update {
            it.copy(
                selectedSubject = selected,
                isRunning = true,
                isFinished = false,
                startMillis = it.startMillis ?: System.currentTimeMillis()
            )
        }

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_ui.value.isRunning && _ui.value.remainingSeconds > 0) {
                delay(1000)
                _ui.update { s -> s.copy(remainingSeconds = s.remainingSeconds - 1) }
            }

            // Si llegó a 0 mientras seguía en running -> finaliza y guarda
            val endState = _ui.value
            if (endState.isRunning && endState.remainingSeconds <= 0) {
                finishAndSave()
            }
        }
    }

    fun pause() {
        _ui.update { it.copy(isRunning = false) }
        timerJob?.cancel()
        timerJob = null
    }

    fun reset() {
        timerJob?.cancel()
        timerJob = null
        _ui.update { current ->
            current.copy(
                totalSeconds = current.plannedMinutes * 60,
                remainingSeconds = current.plannedMinutes * 60,
                isRunning = false,
                isFinished = false,
                startMillis = null
            )
        }
    }

    private fun finishAndSave() {
        timerJob?.cancel()
        timerJob = null

        val state = _ui.value
        val subject = state.selectedSubject
        val start = state.startMillis

        _ui.update { it.copy(isRunning = false, isFinished = true) }

        if (subject != null && start != null) {
            viewModelScope.launch {
                sessionRepo.addSession(
                    subjectId = subject.id,
                    startTimeMillis = start,
                    endTimeMillis = System.currentTimeMillis(),
                    plannedMinutes = state.plannedMinutes,
                    actualMinutes = state.plannedMinutes
                )
            }
        }
    }
}
