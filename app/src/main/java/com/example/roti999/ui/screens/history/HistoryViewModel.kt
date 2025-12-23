package com.example.roti999.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roti999.data.local.LocalDatabase
import com.example.roti999.data.model.FetchedOrder
import com.example.roti999.domain.usecase.OrderUseCase
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
    private val orderUseCase: OrderUseCase,
    private val networkUtils: NetworkUtils,
    private val localDatabase: LocalDatabase
) : ViewModel() {
    private val _uiState = MutableStateFlow<HistoryUIState>(HistoryUIState.Idle)
    val uiState: StateFlow<HistoryUIState> get() = _uiState.asStateFlow()

    private val _historyOrders = MutableStateFlow<List<FetchedOrder>>(emptyList())
    val historyOrders: StateFlow<List<FetchedOrder>> = _historyOrders.asStateFlow()

    fun onSetHistoryOrder(historyOrders: List<FetchedOrder>) {
        _historyOrders.value = historyOrders
    }

    fun loadOrderHistoryItems() {
        viewModelScope.launch(Dispatchers.IO) {
            if (networkUtils.isInternetAvailable()) {
                _uiState.value = HistoryUIState.Loading
                val currentUser = localDatabase.getUser()
                if (currentUser != null) {
                    _uiState.value = orderUseCase.getOrders(userId = currentUser.uid)
                } else {
                    _uiState.value = HistoryUIState.NavigateToCreateProfile
                }
            } else {
                _uiState.value = HistoryUIState.NoInternet
            }
        }
    }

    fun reset() {
        _uiState.value = HistoryUIState.Idle
    }
}