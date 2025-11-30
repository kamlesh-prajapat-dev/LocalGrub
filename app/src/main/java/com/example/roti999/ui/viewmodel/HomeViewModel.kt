package com.example.roti999.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roti999.data.database.local.LocalDatabase
import com.example.roti999.data.model.User
import com.example.roti999.domain.model.DishesResult
import com.example.roti999.domain.model.FoodItem
import com.example.roti999.domain.repository.DishesRepository
import com.example.roti999.domain.repository.UserRepository
import com.example.roti999.util.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dishesRepository: DishesRepository,
    private val localDatabase: LocalDatabase,
    private val userRepository: UserRepository,
    private val networkUtils: NetworkUtils
) : ViewModel() {

    private val _foodItems = MutableLiveData<List<FoodItem>>(emptyList())
    val foodItems: LiveData<List<FoodItem>> get() = _foodItems

    private val _isCartVisible = MutableLiveData(false)
    val isCartVisible: LiveData<Boolean> get() = _isCartVisible

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> get() = _user

    private val _isNetworkAvailable = MutableStateFlow(true)
    val isNetworkAvailable: StateFlow<Boolean> get() = _isNetworkAvailable

    fun onSetIsNetworkAvailable() {
        _isNetworkAvailable.value = true
    }

    init {
        fetchFoodItems()
    }

    fun loadUser() {
        viewModelScope.launch(Dispatchers.IO) {
            val user = localDatabase.getUser()
            if (user != null) {
                _user.postValue(user)
                return@launch
            }

            val isNetworkAvailable = isNetworkAvailable.value
            if (isNetworkAvailable) {
                userRepository.getUserByPhoneNumber { user ->
                    if (user != null) {
                        _user.postValue(user)
                        localDatabase.setUser(user)
                    } else {
                        _user.postValue(null)
                    }
                }
            }
        }
    }

    fun fetchFoodItems() {
        viewModelScope.launch(Dispatchers.IO) {
            if (networkUtils.isInternetAvailable()) {
                _isNetworkAvailable.value = true
                val fetchResult = dishesRepository.getDishes()
                when (fetchResult) {
                    is DishesResult.Success -> {
                        _foodItems.postValue(fetchResult.dishes)
                    }
                    is DishesResult.Error -> {
                        _errorMessage.postValue(fetchResult.message)
                    }
                    else -> {}
                }
            } else {
                _isNetworkAvailable.value = false
            }
        }
    }

    fun onIncreaseQuantity(item: FoodItem) {
        val currentList = _foodItems.value!!.toMutableList()
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
            val currentList = _foodItems.value!!.toMutableList()
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
        val currentList = _foodItems.value!!.toMutableList()
        val index = currentList.indexOf(item)
        if (index != -1) {
            val updatedItem = item.copy(isSelected = isSelected)
            currentList[index] = updatedItem
            _foodItems.value = currentList
            updateCartVisibility()
        }
    }

    private fun updateCartVisibility() {
        _isCartVisible.value = _foodItems.value?.any { it.isSelected }
    }

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            localDatabase.setUser(null)
            userRepository.logout()
        }
    }
}
