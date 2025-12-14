package com.example.roti999.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roti999.data.local.LocalDatabase
import com.example.roti999.domain.model.User
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

    private val _uiState = MutableStateFlow<HomeUIState>(HomeUIState.Idle)
    val uiState: StateFlow<HomeUIState> get() = _uiState.asStateFlow()

    private val _foodItems = MutableStateFlow<List<FoodItem>>(emptyList())
    val foodItems: StateFlow<List<FoodItem>> get() = _foodItems.asStateFlow()

    fun onChangeFoodItems(items: List<FoodItem>) {
        _foodItems.value = items
    }

    private val _isCartVisible = MutableStateFlow(false)
    val isCartVisible: StateFlow<Boolean> get() = _isCartVisible.asStateFlow()

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> get() = _user.asStateFlow()

    fun loadInitialData() {
        if (networkUtils.isInternetAvailable()) {
            loadUser()
            fetchFoodItems()
        } else {
            _uiState.value = HomeUIState.IsInternetAvailable
        }
    }

    private fun loadUser() {
        viewModelScope.launch(Dispatchers.IO) {
            val user = localDatabase.getUser()
            if (user != null) {
                _user.update { user }
                return@launch
            }

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

    private fun fetchFoodItems() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { HomeUIState.Loading }
            val fetchResult = dishesRepository.getDishes()
            _uiState.update { fetchResult }
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
        val currentList = _foodItems.value.toMutableList()
        val index: Int = currentList.indexOf(item)
        if (index != -1) {
            val newQty = if (item.quantity <= 0) 9 else item.quantity - 1
            if (newQty == 0) {
                val updatedItem = item.copy(quantity = newQty, isSelected = false)
                currentList[index] = updatedItem
                _foodItems.value = currentList
                updateCartVisibility()
            } else {
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
