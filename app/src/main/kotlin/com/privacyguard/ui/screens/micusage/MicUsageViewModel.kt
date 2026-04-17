package com.privacyguard.ui.screens.micusage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.privacyguard.data.db.entities.AppMicUsage
import com.privacyguard.data.repository.MicUsageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MicUsageViewModel @Inject constructor(
    private val micRepo: MicUsageRepository
) : ViewModel() {

    val usageList: StateFlow<List<AppMicUsage>> = micRepo
        .getUsageSince(System.currentTimeMillis() - 24L * 60 * 60 * 1000)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val alertApps: StateFlow<List<AppMicUsage>> = usageList.map { list ->
        list.filter { it.durationMs > 5 * 60 * 1000L } // > 5 min
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun refresh() {
        viewModelScope.launch { micRepo.scanAndStore() }
    }
}
