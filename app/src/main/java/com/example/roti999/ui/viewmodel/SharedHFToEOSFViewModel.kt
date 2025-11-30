package com.example.roti999.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.roti999.data.model.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SharedHFToEOSFViewModel: ViewModel() {
    private val _order = MutableStateFlow<Order?>(null)
    val order: StateFlow<Order?> get() = _order.asStateFlow()

    fun onSetOrder(order: Order) {
        _order.value = order
    }
}