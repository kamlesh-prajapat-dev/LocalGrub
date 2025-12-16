package com.example.roti999.ui.screens.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roti999.data.dto.Order
import com.example.roti999.data.dto.OrderPlaced
import com.example.roti999.data.dto.SelectedDishItem
import com.example.roti999.data.dto.User
import com.example.roti999.domain.model.FoodItem
import com.example.roti999.domain.repository.OrderRepository
import com.example.roti999.util.Constant
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {
    private val _orderUIState = MutableStateFlow<OrderUIState>(OrderUIState.Idle)
    val orderUIState: StateFlow<OrderUIState> get() = _orderUIState.asStateFlow()

    private val _totalPrice = MutableStateFlow(0.0)
    val totalPrice: StateFlow<Double> get() = _totalPrice.asStateFlow()
    private var currentUser: User? = null
    private var currentItems: List<FoodItem> = emptyList()

    fun updateUserData(user: User) {
        currentUser = user
    }

    fun updateOrderDetails(items: List<FoodItem>) {
        currentItems = items
        calculateTotalPrice()
    }

    private fun calculateTotalPrice() {
        val total = currentItems.sumOf { item ->
            item.price * item.quantity
        }
        _totalPrice.value = total.toDouble()
    }

    fun placeOrder() {
        viewModelScope.launch {
            _orderUIState.value = OrderUIState.Loading

            val user = currentUser
            if (user == null) {
                _orderUIState.value = OrderUIState.ValidationError("User details not found.")
                return@launch
            }

            if (currentItems.isEmpty()) {
                _orderUIState.value = OrderUIState.ValidationError("Your cart is empty.")
                return@launch
            }

            val orderPlaced = OrderPlaced(
                userId = user.uid,
                userName = user.name,
                userAddress = user.address,
                userPhoneNumber = user.phoneNumber,
                items = currentItems.map {
                    SelectedDishItem(
                        id = it.id,
                        name = it.name,
                        price = it.price,
                        quantity = it.quantity
                    )
                },
                totalPrice = _totalPrice.value,
                placeAt = Timestamp.now(),
                status = Constant.PLACED.name,
                token = user.fcmToken
            )

            orderRepository.placeOrder(orderPlaced) {
                _orderUIState.value = it
            }
        }
    }
}