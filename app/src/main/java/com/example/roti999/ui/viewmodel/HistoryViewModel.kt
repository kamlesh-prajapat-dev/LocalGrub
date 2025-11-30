package com.example.roti999.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roti999.data.database.local.LocalDatabase
import com.example.roti999.data.model.Order
import com.example.roti999.domain.model.OrderHistoryResult
import com.example.roti999.domain.repository.OrderRepository
import com.example.roti999.util.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val networkUtils: NetworkUtils,
    private val localDatabase: LocalDatabase
) : ViewModel() {

    private val _isNetworkAvailable = MutableStateFlow(true)
    val isNetworkAvailable: StateFlow<Boolean> = _isNetworkAvailable

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _historyOrders = MutableStateFlow<List<Order>>(emptyList())
    val historyOrders: StateFlow<List<Order>> = _historyOrders

    init {
        loadOrderHistoryItems()
    }

    fun loadOrderHistoryItems() {
        viewModelScope.launch(Dispatchers.IO) {
            if (networkUtils.isInternetAvailable()) {
                _isNetworkAvailable.value = true
                val currentUser = localDatabase.getUser()
                if (currentUser != null) {
                    orderRepository.getOrders(userId = currentUser.uid) {fetchResult ->
                        when (fetchResult) {
                            is OrderHistoryResult.Success -> {
                                _historyOrders.value = fetchResult.orders
                            }
                            is OrderHistoryResult.Error -> {
                                _historyOrders.value = listOf(Order())
                            }
                            else -> {}
                        }
                    }

                } else {
                    _errorMessage.value = ""
                }
            } else {
                _isNetworkAvailable.value = false
            }
        }
    }
}