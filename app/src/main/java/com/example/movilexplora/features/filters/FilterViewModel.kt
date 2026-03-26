package com.example.movilexplora.features.filters

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class FilterState(
    val distance: Float = 15f,
    val selectedCategory: String? = null,
    val selectedPriceRange: Int = 2,
    val filterCount: Int = 0
)

@HiltViewModel
class FilterViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow(FilterState())
    val state: StateFlow<FilterState> = _state.asStateFlow()

    fun updateDistance(distance: Float) {
        _state.update { it.copy(distance = distance) }
        updateFilterCount()
    }

    fun selectCategory(category: String) {
        _state.update { it.copy(selectedCategory = if (it.selectedCategory == category) null else category) }
        updateFilterCount()
    }

    fun selectPriceRange(range: Int) {
        _state.update { it.copy(selectedPriceRange = range) }
        updateFilterCount()
    }

    fun clearFilters() {
        _state.value = FilterState()
    }

    private fun updateFilterCount() {
        var count = 0
        if (_state.value.distance != 15f) count++
        if (_state.value.selectedCategory != null) count++
        if (_state.value.selectedPriceRange != 2) count++
        _state.update { it.copy(filterCount = count) }
    }
}
