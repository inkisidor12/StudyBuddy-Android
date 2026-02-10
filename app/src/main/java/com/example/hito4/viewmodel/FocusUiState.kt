package com.example.hito4.viewmodel


import com.example.hito4.data.entity.SubjectEntity

data class FocusUiState(
    val subjects: List<SubjectEntity> = emptyList(),
    val selectedSubject: SubjectEntity? = null,
    val plannedMinutes: Int = 25,
    val totalSeconds: Int = 25 * 60,
    val remainingSeconds: Int = 25 * 60,
    val isRunning: Boolean = false,
    val isFinished: Boolean = false,
    val startMillis: Long? = null
)
