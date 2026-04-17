package com.privacyguard.ui.screens.triggermap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.privacyguard.data.db.entities.TriggerPair
import com.privacyguard.data.repository.TriggerPairRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TriggerMapViewModel @Inject constructor(
    private val triggerRepo: TriggerPairRepository
) : ViewModel() {

    val pairs: StateFlow<List<TriggerPair>> = triggerRepo
        .getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun refresh() {
        viewModelScope.launch { triggerRepo.scanAndStore() }
    }
}
