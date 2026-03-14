package com.example.localgrub.ui.screens.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localgrub.data.model.firebase.PlacedOrder
import com.example.localgrub.data.model.firebase.SelectedDish
import com.example.localgrub.data.model.firebase.GetUser
import com.example.localgrub.domain.usecase.OrderUseCase
import com.example.localgrub.util.NetworkUtils
import com.example.localgrub.util.OrderStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val orderUseCase: OrderUseCase,
    private val networkUtils: NetworkUtils
) : ViewModel() {
    private val _orderUIState = MutableStateFlow<OrderUIState>(OrderUIState.Idle)
    val orderUIState: StateFlow<OrderUIState> get() = _orderUIState.asStateFlow()
    private val _totalPrice = MutableStateFlow(0.0)
    val totalPrice: StateFlow<Double> get() = _totalPrice.asStateFlow()
    private val _user = MutableStateFlow<GetUser?>(null)
    val user: StateFlow<GetUser?> get() = _user.asStateFlow()
    private val _selectedDishes = MutableStateFlow<List<SelectedDish>>(emptyList())
    val selectedDishes: StateFlow<List<SelectedDish>> get() = _selectedDishes.asStateFlow()
    fun updateUserData(user: GetUser) {
        _user.value = user
    }

    fun updateOrderDetails(items: List<SelectedDish>) {
        _selectedDishes.value = items
        calculateTotalPrice()
    }

    private fun calculateTotalPrice() {
        val total = selectedDishes.value.sumOf { item ->
            item.price * item.quantity
        }
        _totalPrice.value = total.toDouble()
    }

    fun placeOrder() {
        _orderUIState.value = OrderUIState.Loading

        if (!networkUtils.hasInternetAccess()) {
            _orderUIState.value = OrderUIState.NoInternet
            return
        }

        val user = user.value
        if (user == null) {
            _orderUIState.value = OrderUIState.ValidationError("User details not found.")
            return
        }

        val currentItems = selectedDishes.value
        if (currentItems.isEmpty()) {
            _orderUIState.value = OrderUIState.ValidationError("Your cart is empty.")
            return
        }

        viewModelScope.launch {
            val orderPlaced = PlacedOrder(
                userId = user.uid,
                userName = user.name,
                userAddress = user.address,
                userPhoneNumber = user.phoneNumber,
                items = currentItems,
                totalPrice = _totalPrice.value,
                placeAt = System.currentTimeMillis(),
                status = OrderStatus.PLACED,
                previousStatus = OrderStatus.PLACED
            )

            val result = orderUseCase.placeOrder(orderPlaced)
            _orderUIState.value = result
        }
    }
}