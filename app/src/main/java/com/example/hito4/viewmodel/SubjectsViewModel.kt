package com.example.hito4.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hito4.data.entity.SubjectEntity
import com.example.hito4.data.repo.SubjectRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SubjectsViewModel(private val repo: SubjectRepository) : ViewModel() {

    val subjects = repo.observeSubjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun add(name: String) {
        viewModelScope.launch { repo.addSubject(name) }
    }

    fun delete(subject: SubjectEntity) {
        viewModelScope.launch { repo.deleteSubject(subject) }
    }
}
