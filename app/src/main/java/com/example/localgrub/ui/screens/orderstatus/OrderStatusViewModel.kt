package com.example.localgrub.ui.screens.orderstatus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localgrub.data.model.FetchedOrder
import com.example.localgrub.data.model.GetUser
import com.example.localgrub.data.model.SelectedDish
import com.example.localgrub.domain.usecase.OrderUseCase
import com.example.localgrub.domain.usecase.UserUseCase
import com.example.localgrub.util.NetworkUtils
import com.example.localgrub.util.OrderStatus
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
class OrderStatusViewModel @Inject constructor(
    private val orderUseCase: OrderUseCase,
    private val networkUtils: NetworkUtils,
    private val userUseCase: UserUseCase
) : ViewModel() {

    private val _order = MutableStateFlow<FetchedOrder?>(null)
    val order: StateFlow<FetchedOrder?> get() = _order.asStateFlow()

    fun onSetOrder(order: FetchedOrder?) {
        _order.value = order
        loadUser()
    }

    private val _user = MutableStateFlow<GetUser?>(null)
    val user: StateFlow<GetUser?> get() = _user.asStateFlow()

    fun loadUser() {
        val user = userUseCase.getLocalUser()
        if (user != null) {
            _user.value = user
        }
    }

    private val _selectItemList = MutableStateFlow<List<SelectedDish>>(emptyList())

    private val _uiState = MutableStateFlow<OrderStatusUIState>(OrderStatusUIState.Idle)
    val uiState: StateFlow<OrderStatusUIState> get() = _uiState.asStateFlow()

    fun onSetSelectItemList(selectItemList: List<SelectedDish>) {
        _selectItemList.value = selectItemList
    }

    fun observeOrderById(orderId: String) {
        if (!networkUtils.isInternetAvailable()) {
            _uiState.value = OrderStatusUIState.NoInternet
            return
        }

        orderUseCase.observeOrderById(orderId)
            .onStart {
                _uiState.value = OrderStatusUIState.Loading
            }
            .onEach { state ->
                _uiState.value = state
            }
            .launchIn(viewModelScope)
    }

    fun cancelOrder() {
        _uiState.value = OrderStatusUIState.Loading

        if (!networkUtils.isInternetAvailable()) {
            _uiState.value = OrderStatusUIState.NoInternet
            return
        }

        viewModelScope.launch {
            val orderId = order.value?.id ?: return@launch
            val status = order.value?.status ?: return@launch
            _uiState.value = orderUseCase.cancelOrder(orderId, OrderStatus.CANCELLED, status)
        }
    }
}