package com.privacyguard.ui.screens.location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.privacyguard.data.db.entities.AppLocationUsage
import com.privacyguard.data.repository.LocationUsageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationUsageViewModel @Inject constructor(
    private val repository: LocationUsageRepository
) : ViewModel() {

    val usageList: StateFlow<List<AppLocationUsage>> = repository.getAllUsage()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun refresh() {
        viewModelScope.launch {
            repository.scanAndStore()
        }
    }
}
