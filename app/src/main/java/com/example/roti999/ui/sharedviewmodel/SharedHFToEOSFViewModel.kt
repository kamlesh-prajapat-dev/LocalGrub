package com.example.roti999.ui.sharedviewmodel

import androidx.lifecycle.ViewModel
import com.example.roti999.data.model.FetchedOrder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SharedHFToEOSFViewModel: ViewModel() {
    private val _order = MutableStateFlow<FetchedOrder?>(null)
    val order: StateFlow<FetchedOrder?> get() = _order.asStateFlow()

    fun onSetOrder(order: FetchedOrder?) {
        _order.value = order
    }
}