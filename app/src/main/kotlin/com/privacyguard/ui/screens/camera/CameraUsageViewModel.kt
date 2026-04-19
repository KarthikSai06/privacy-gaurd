package com.privacyguard.ui.screens.camera

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.privacyguard.data.db.entities.AppCameraUsage
import com.privacyguard.data.repository.CameraUsageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CameraUsageViewModel @Inject constructor(
    private val repository: CameraUsageRepository
) : ViewModel() {

    val usageList: StateFlow<List<AppCameraUsage>> = repository.getAllUsage()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun refresh() {
        viewModelScope.launch {
            repository.scanAndStore()
        }
    }
}
