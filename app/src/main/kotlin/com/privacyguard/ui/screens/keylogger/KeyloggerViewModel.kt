package com.privacyguard.ui.screens.keylogger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.privacyguard.data.db.entities.AccessibilityRecord
import com.privacyguard.data.repository.AccessibilityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KeyloggerViewModel @Inject constructor(
    private val accessibilityRepo: AccessibilityRepository
) : ViewModel() {

    val records: StateFlow<List<AccessibilityRecord>> = accessibilityRepo
        .getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val suspiciousCount: StateFlow<Int> = accessibilityRepo
        .countSuspicious()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun refresh() {
        viewModelScope.launch { accessibilityRepo.scanAndStore() }
    }
}
