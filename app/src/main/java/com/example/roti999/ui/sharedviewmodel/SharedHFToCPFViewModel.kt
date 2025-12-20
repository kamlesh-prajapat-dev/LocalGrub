package com.example.roti999.ui.sharedviewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SharedHFToCPFViewModel: ViewModel() {
    private val _isNavigate = MutableStateFlow(false)
    val isNavigate: StateFlow<Boolean> get() = _isNavigate.asStateFlow()

    fun onSetIsNavigate(flag: Boolean) {
        _isNavigate.value = flag
    }

    fun reset() {
        _isNavigate.value = false
    }
}