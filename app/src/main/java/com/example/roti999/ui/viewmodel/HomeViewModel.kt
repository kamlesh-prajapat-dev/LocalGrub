package com.example.roti999.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roti999.data.model.User
import com.example.roti999.domain.model.AddOn
import com.example.roti999.domain.model.DishesResult
import com.example.roti999.domain.model.FoodItem
import com.example.roti999.domain.repository.DishesRepository
import com.example.roti999.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dishesRepository: DishesRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _foodItems = MutableLiveData<List<FoodItem>>(emptyList())
    val foodItems: LiveData<List<FoodItem>> get()  = _foodItems

    private val _isCartVisible = MutableLiveData(false)
    val isCartVisible: LiveData<Boolean> get() = _isCartVisible

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> get() = _user


    init {
        fetchFoodItems()
        getUser()
    }

    private fun fetchFoodItems() {
        viewModelScope.launch {
            // You would replace this with your actual data fetching logic
            val fetchResult = dishesRepository.getDishes()
            when(fetchResult) {
                is DishesResult.Success -> {
                    _foodItems.postValue(fetchResult.dishes)
                }
                is DishesResult.Error -> {
                    _errorMessage.postValue(fetchResult.message)
                }
                else -> {}
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

    fun onAddOnSelected(item: FoodItem, addOn: AddOn, isSelected: Boolean) {
        val currentList = _foodItems.value!!.toMutableList()
        val itemIndex: Int = currentList.indexOf(item)
        if (itemIndex != -1) {
            val addOnIndex = item.addOns.indexOf(addOn)
            if (addOnIndex != -1) {
                val updatedAddOns = item.addOns.toMutableList()
                updatedAddOns[addOnIndex] = addOn.copy(isSelected = isSelected)
                val updatedItem = item.copy(addOns = updatedAddOns)
                currentList[itemIndex] = updatedItem
                _foodItems.value = currentList
            }
        }
    }

    private fun updateCartVisibility() {
        _isCartVisible.value = _foodItems.value?.any { it.isSelected }
    }

    private fun getUser() {
        viewModelScope.launch {
            userRepository.getUserByPhoneNumber { user ->
                _user.postValue(user)
            }
        }
    }
}
