package com.example.roti999.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roti999.data.model.DishItem
import com.example.roti999.data.model.OrderPlaced
import com.example.roti999.data.model.SelectedDishItem
import com.example.roti999.data.model.User
import com.example.roti999.domain.model.FoodItem
import com.example.roti999.domain.repository.OrderRepository
import com.example.roti999.util.Constant
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    // Represents the current state of the order placement process
    sealed class OrderUIState {
        object Idle: OrderUIState()
        object Loading: OrderUIState()
        object Success: OrderUIState()
        data class Error(val message: String): OrderUIState()
    }

    private val _orderUIState = MutableStateFlow<OrderUIState>(OrderUIState.Idle)
    val orderUIState: StateFlow<OrderUIState> = _orderUIState

    private val _totalPrice = MutableStateFlow(0.0)
    val totalPrice: StateFlow<Double> = _totalPrice

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
                _orderUIState.value = OrderUIState.Error("User details not found.")
                return@launch
            }

            if (currentItems.isEmpty()) {
                _orderUIState.value = OrderUIState.Error("Your cart is empty.")
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
                        description = it.description,
                        price = it.price,
                        thumbnail = it.imageUrl,
                        isAvailable = true,
                        quantity = it.quantity
                    )
                },
                totalPrice = _totalPrice.value,
                placeAt = Timestamp.now(),
                status = Constant.PLACED.name
            )

            orderRepository.placeOrder(orderPlaced) {
                if (it) {
                    _orderUIState.value = OrderUIState.Success
                } else {
                    _orderUIState.value =
                        OrderUIState.Error("Failed to place order. Please try again.")
                }
            }
        }
    }
}