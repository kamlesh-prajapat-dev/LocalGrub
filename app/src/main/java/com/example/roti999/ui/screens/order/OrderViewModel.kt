package com.example.roti999.ui.screens.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roti999.data.model.PlacedOrder
import com.example.roti999.data.model.SelectedDish
import com.example.roti999.data.model.User
import com.example.roti999.domain.model.FoodItem
import com.example.roti999.domain.usecase.OrderUseCase
import com.example.roti999.util.NetworkUtils
import com.example.roti999.util.OrderStatus
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
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> get() = _currentUser.asStateFlow()
    private val _currentItemList = MutableStateFlow<List<SelectedDish>>(emptyList())
    val currentItems: StateFlow<List<SelectedDish>> get() = _currentItemList.asStateFlow()
    fun updateUserData(user: User) {
        _currentUser.value = user
    }

    fun updateOrderDetails(items: List<SelectedDish>) {
        _currentItemList.value = items
        calculateTotalPrice()
    }

    private fun calculateTotalPrice() {
        val total = currentItems.value.sumOf { item ->
            item.price * item.quantity
        }
        _totalPrice.value = total.toDouble()
    }

    fun placeOrder() {
        _orderUIState.value = OrderUIState.Loading

        if (!networkUtils.isInternetAvailable()) {
            _orderUIState.value = OrderUIState.NoInternet
            return
        }

        val user = currentUser.value
        if (user == null) {
            _orderUIState.value = OrderUIState.ValidationError("User details not found.")
            return
        }

        val currentItems = currentItems.value
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
                previousStatus = OrderStatus.PLACED,
                token = user.fcmToken
            )

            val result = orderUseCase.placeOrder(orderPlaced)
            _orderUIState.value = result
        }
    }
}