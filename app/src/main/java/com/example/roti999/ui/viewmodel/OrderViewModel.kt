package com.example.roti999.ui.viewmodel

import androidx.activity.result.launch
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roti999.data.model.Order
import com.example.roti999.data.model.User
import com.example.roti999.domain.model.FoodItem
import com.example.roti999.domain.repository.OrderRepository
import com.example.roti999.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    // Represents the current state of the order placement process
    sealed class OrderUIState {
        object Idle : OrderUIState()
        object Loading : OrderUIState()
        object Success : OrderUIState()
        data class Error(val message: String) : OrderUIState()
    }

    private val _orderUIState = MutableLiveData<OrderUIState>(OrderUIState.Idle)
    val orderUIState: LiveData<OrderUIState> = _orderUIState

    private val _totalPrice = MutableLiveData(0.0)
    val totalPrice: LiveData<Double> = _totalPrice

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
            val itemTotal = item.price * item.quantity
            val addOnsTotal = item.addOns.filter { it.isSelected }.sumOf { it.price }
            itemTotal + addOnsTotal
        }
        _totalPrice.value = total.toDouble()
    }

    fun editUser(name: String, address: String) {
        viewModelScope.launch {
            _orderUIState.value = OrderUIState.Loading
            if (name.isNotEmpty() && address.isNotEmpty()) {
                val user = currentUser
                if (user == null) {
                    _orderUIState.value = OrderUIState.Error("User details not found.")
                    return@launch
                }
                if (user.name == name && user.address == address) {
                    _orderUIState.value = OrderUIState.Error("Please make changes to save.")
                }else {
                    val updatedUser = user.copy(name = name, address = address)
                    userRepository.createUser(updatedUser) { success ->
                        if (success) {
                            _orderUIState.value = OrderUIState.Success
                            updateUserData(updatedUser)
                        } else {
                            _orderUIState.value = OrderUIState.Error("Failed to save profile")
                        }
                    }
                }
            } else {
                _orderUIState.value = OrderUIState.Error("Please fill in all fields")
            }
        }
    }

    fun createOrder() {
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

            val order = Order(
                userId = user.uid,
                userName = user.name,
                userAddress = user.address,
                userPhoneNumber = user.phoneNumber,
                items = currentItems.map {
                    it.id
                },
                totalPrice = _totalPrice.value ?: 0.0,
                orderDate = LocalDate.now(),
                status = "Pending"
            )

            orderRepository.placeOrder(order) {
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
