package com.example.localgrub.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localgrub.data.model.FetchedOrder
import com.example.localgrub.domain.usecase.OrderUseCase
import com.example.localgrub.util.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val orderUseCase: OrderUseCase,
    private val networkUtils: NetworkUtils
) : ViewModel() {
    private val _uiState = MutableStateFlow<HistoryUIState>(HistoryUIState.Idle)
    val uiState: StateFlow<HistoryUIState> get() = _uiState.asStateFlow()

    private val _historyOrders = MutableStateFlow<List<FetchedOrder>>(emptyList())
    val historyOrders: StateFlow<List<FetchedOrder>> = _historyOrders.asStateFlow()

    fun onSetHistoryOrder(historyOrders: List<FetchedOrder>) {
        _historyOrders.value = historyOrders
    }

    fun loadOrderHistoryItems(uid: String) {
        if (!networkUtils.isInternetAvailable()) {
            _uiState.value = HistoryUIState.NoInternet
        }

        orderUseCase.observeOrders(uid)
            .onStart {
                _uiState.value = HistoryUIState.Loading
            }
            .onEach { state ->
                _uiState.value = state
            }
            .launchIn(viewModelScope)
    }

    fun reset() {
        _uiState.value = HistoryUIState.Idle
    }
}