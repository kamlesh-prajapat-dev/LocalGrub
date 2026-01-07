package com.example.roti999.ui.screens.eachorderstatus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roti999.data.model.FetchedOrder
import com.example.roti999.data.model.SelectedDish
import com.example.roti999.domain.repository.OrderRepository
import com.example.roti999.domain.usecase.OrderUseCase
import com.example.roti999.ui.screens.history.HistoryUIState
import com.example.roti999.util.NetworkUtils
import com.example.roti999.util.OrderStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EachOrderStatusViewModel @Inject constructor(
    private val orderUseCase: OrderUseCase,
    private val networkUtils: NetworkUtils,
) : ViewModel() {

    private val _order = MutableStateFlow<FetchedOrder?>(null)
    val order: StateFlow<FetchedOrder?> get() = _order.asStateFlow()

    fun onSetOrder(order: FetchedOrder?) {
        _order.value = order
    }

    private val _selectItemList = MutableStateFlow<List<SelectedDish>>(emptyList())

    private val _uiState = MutableStateFlow<EachOrderUIState>(EachOrderUIState.Idle)
    val uiState: StateFlow<EachOrderUIState> get() = _uiState.asStateFlow()

    fun onSetSelectItemList(selectItemList: List<SelectedDish>) {
        _selectItemList.value = selectItemList
    }

    fun observeOrderById(orderId: String) {
        if (!networkUtils.isInternetAvailable()) {
            _uiState.value = EachOrderUIState.NoInternet
            return
        }

        orderUseCase.observeOrderById(orderId)
            .onStart {
                _uiState.value = EachOrderUIState.Loading
            }
            .onEach { state ->
                _uiState.value = state
            }
            .launchIn(viewModelScope)
    }

    fun cancelOrder() {
        _uiState.value = EachOrderUIState.Loading

        if (!networkUtils.isInternetAvailable()) {
            _uiState.value = EachOrderUIState.NoInternet
            return
        }

        viewModelScope.launch {
            val orderId = order.value?.id ?: return@launch
            val status = order.value?.status ?: return@launch
            _uiState.value = orderUseCase.cancelOrder(orderId, OrderStatus.CANCELLED, status)
        }
    }
}