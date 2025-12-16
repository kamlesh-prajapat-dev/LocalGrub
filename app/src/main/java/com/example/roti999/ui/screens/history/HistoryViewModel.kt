package com.example.roti999.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roti999.data.local.LocalDatabase
import com.example.roti999.data.dto.Order
import com.example.roti999.domain.repository.OrderRepository
import com.example.roti999.util.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val networkUtils: NetworkUtils,
    private val localDatabase: LocalDatabase
) : ViewModel() {

    private val _uiState = MutableStateFlow<HistoryUIState>(HistoryUIState.Idle)
    val uiState: StateFlow<HistoryUIState> get() = _uiState.asStateFlow()

    private val _historyOrders = MutableStateFlow<List<Order>>(emptyList())
    val historyOrders: StateFlow<List<Order>> = _historyOrders.asStateFlow()

    fun onSetHistoryOrder(historyOrders: List<Order>) {
        _historyOrders.value = historyOrders
    }

    init {
        loadOrderHistoryItems()
    }

    fun loadOrderHistoryItems() {
        viewModelScope.launch(Dispatchers.IO) {
            if (networkUtils.isInternetAvailable()) {
                _uiState.value = HistoryUIState.Loading
                val currentUser = localDatabase.getUser()
                if (currentUser != null) {
                    orderRepository.getOrders(userId = currentUser.uid) { fetchResult ->
                        _uiState.value = fetchResult
                    }

                } else {
                    _uiState.value = HistoryUIState.Error("User not found")
                }
            } else {
                _uiState.value = HistoryUIState.NoInternet
            }
        }
    }
}