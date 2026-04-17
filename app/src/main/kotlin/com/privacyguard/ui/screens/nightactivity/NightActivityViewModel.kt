package com.privacyguard.ui.screens.nightactivity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.privacyguard.data.db.entities.NightActivity
import com.privacyguard.data.repository.NightActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class NightActivityViewModel @Inject constructor(
    private val nightRepo: NightActivityRepository
) : ViewModel() {

    private val _filterDays = MutableStateFlow(7)
    val filterDays: StateFlow<Int> = _filterDays

    val activities: StateFlow<List<NightActivity>> = _filterDays.flatMapLatest { days ->
        val from = System.currentTimeMillis() - days.toLong() * 24 * 60 * 60 * 1000
        nightRepo.getFrom(from)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFilterDays(days: Int) { _filterDays.value = days }

    fun refresh() {
        viewModelScope.launch { nightRepo.scanAndStore() }
    }
}
