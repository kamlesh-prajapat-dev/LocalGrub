package com.example.roti999.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roti999.data.database.local.LocalDatabase
import com.example.roti999.domain.model.User
import com.example.roti999.domain.model.DishesResult
import com.example.roti999.domain.model.FoodItem
import com.example.roti999.domain.repository.DishesRepository
import com.example.roti999.domain.repository.UserRepository
import com.example.roti999.util.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dishesRepository: DishesRepository,
    private val localDatabase: LocalDatabase,
    private val userRepository: UserRepository,
    private val networkUtils: NetworkUtils
) : ViewModel() {

    private val _fetchResult = MutableStateFlow<DishesResult>(DishesResult.Idle)
    val fetchResult: StateFlow<DishesResult> get() = _fetchResult.asStateFlow()

    private val _foodItems = MutableStateFlow<List<FoodItem>>(emptyList())
    val foodItems: StateFlow<List<FoodItem>> get() = _foodItems.asStateFlow()

    fun onChangeFoodItems(items: List<FoodItem>) {
        _foodItems.value = items
    }

    private val _isCartVisible = MutableStateFlow(false)
    val isCartVisible: StateFlow<Boolean> get() = _isCartVisible.asStateFlow()

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> get() = _user.asStateFlow()

    private val _isNetworkAvailable = MutableStateFlow(true)
    val isNetworkAvailable: StateFlow<Boolean> get() = _isNetworkAvailable.asStateFlow()

    fun onSetIsNetworkAvailable() {
        _isNetworkAvailable.value = true
    }

    fun loadUser() {
        viewModelScope.launch(Dispatchers.IO) {
            val user = localDatabase.getUser()
            if (user != null) {
                _user.update {
                    user
                }
                return@launch
            }

            val isNetworkAvailable = isNetworkAvailable.value
            if (isNetworkAvailable) {
                userRepository.getUserByPhoneNumber { user ->
                    if (user != null) {
                        _user.update {
                            user
                        }
                        localDatabase.setUser(user)
                    } else {
                        _user.update { null }
                    }
                }
            }
        }
    }

    fun fetchFoodItems() {
        viewModelScope.launch(Dispatchers.IO) {
            if (networkUtils.isInternetAvailable()) {
                _fetchResult.update { DishesResult.Loading }
                _isNetworkAvailable.value = true
                val fetchResult = dishesRepository.getDishes()
                when (fetchResult) {
                    is DishesResult.Success -> {
                        _fetchResult.update { fetchResult }
                    }
                    is DishesResult.Error -> {
                        _fetchResult.update { fetchResult }
                    }
                    else -> {}
                }
            } else {
                _isNetworkAvailable.value = false
                _fetchResult.update { DishesResult.Idle }
            }
        }
    }

    fun onIncreaseQuantity(item: FoodItem) {
        val currentList = _foodItems.value.toMutableList()
        val index: Int = currentList.indexOf(item)
        if (index != -1) {
            val newQty = if (item.quantity >= 9) 1 else item.quantity + 1
            val updatedItem = item.copy(quantity = newQty)
            currentList[index] = updatedItem
            _foodItems.value = currentList
        }
    }

    fun onDecreaseQuantity(item: FoodItem) {
        if (item.quantity > 1) {
            val currentList = _foodItems.value.toMutableList()
            val index: Int = currentList.indexOf(item)
            if (index != -1) {
                val newQty = if (item.quantity <= 1) 9 else item.quantity - 1
                val updatedItem = item.copy(quantity = newQty)
                currentList[index] = updatedItem
                _foodItems.value = currentList
            }
        }
    }

    fun onSelectItem(item: FoodItem, isSelected: Boolean) {
        val currentList = _foodItems.value.toMutableList()
        val index = currentList.indexOf(item)
        if (index != -1) {
            val updatedItem = item.copy(isSelected = isSelected, quantity = 1)
            currentList[index] = updatedItem
            _foodItems.value = currentList
            updateCartVisibility()
        }
    }

    private fun updateCartVisibility() {
        _isCartVisible.value = _foodItems.value.any { it.isSelected }
    }

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            localDatabase.setUser(null)
            userRepository.logout()
        }
    }
}
