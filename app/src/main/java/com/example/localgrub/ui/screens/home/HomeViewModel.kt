package com.example.localgrub.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localgrub.data.model.FoodItem
import com.example.localgrub.data.model.GetUser
import com.example.localgrub.domain.usecase.AuthUseCase
import com.example.localgrub.domain.usecase.DishesUseCase
import com.example.localgrub.domain.usecase.UserUseCase
import com.example.localgrub.util.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userUseCase: UserUseCase,
    private val authUseCase: AuthUseCase,
    private val dishesUseCase: DishesUseCase,
    private val networkUtils: NetworkUtils
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUIState>(HomeUIState.Idle)
    val uiState: StateFlow<HomeUIState> = _uiState.asStateFlow()

    private val _foodItems = MutableStateFlow<List<FoodItem>>(emptyList())
    val foodItems: StateFlow<List<FoodItem>> = _foodItems.asStateFlow()

    private val _isCartVisible = MutableStateFlow(false)
    val isCartVisible: StateFlow<Boolean> = _isCartVisible.asStateFlow()

    private val _user = MutableStateFlow<GetUser?>(null)
    val user: StateFlow<GetUser?> = _user.asStateFlow()

    fun onChangeFoodItems(items: List<FoodItem>) {
        _foodItems.value = items
        updateCartVisibility()
    }

    fun fetchFoodItems() {
        if (!networkUtils.isInternetAvailable()) {
            _uiState.value = HomeUIState.NoInternet
        }

        dishesUseCase.getDishes()
            .onStart { _uiState.value = HomeUIState.Loading }
            .onEach { state -> _uiState.value = state }
            .launchIn(viewModelScope)
    }

    fun loadCurrentUser() {
        val currentUser = authUseCase.getCurrentUser()
        if (currentUser != null) {
            _user.value = GetUser(
                uid = currentUser.uid,
                phoneNumber = currentUser.phoneNumber!!
            )
        } else {
            _uiState.value = HomeUIState.LoginState
        }
    }

    fun loadUser() {
        val currentUser = authUseCase.getCurrentUser() ?: run {
            _uiState.value = HomeUIState.LoginState
            return
        }

        val localUser = userUseCase.getLocalUser()
        if (localUser?.profileCompleted == true) {
            _uiState.value = HomeUIState.OrderState(localUser)
        } else if (localUser != null) {
            _uiState.value = HomeUIState.ProfileState(
                user = GetUser(
                    uid = currentUser.uid,
                    phoneNumber = currentUser.phoneNumber!!
                )
            )
        } else {
            viewModelScope.launch {
                _uiState.value = HomeUIState.Loading
                val fetchResult =
                    userUseCase.getUserByUid(currentUser.uid, currentUser.phoneNumber!!)
                _uiState.value = fetchResult
            }
        }
    }

    fun onIncreaseQuantity(item: FoodItem) {
        updateFoodItem(item.id) {
            it.copy(quantity = (it.quantity + 1).coerceAtMost(MAX_QUANTITY))
        }
    }

    fun onDecreaseQuantity(item: FoodItem) {
        updateFoodItem(item.id) {
            if (it.quantity > 1) {
                it.copy(quantity = it.quantity - 1)
            } else {
                it.copy(isSelected = false, quantity = 1)
            }
        }
    }

    fun onSelectItem(item: FoodItem, isSelected: Boolean) {
        updateFoodItem(item.id) {
            it.copy(isSelected = isSelected, quantity = 1)
        }
    }

    private fun updateFoodItem(itemId: String, transform: (FoodItem) -> FoodItem) {
        _foodItems.update { currentList ->
            currentList.map { item ->
                if (item.id == itemId) transform(item) else item
            }
        }
        updateCartVisibility()
    }

    private fun updateCartVisibility() {
        _isCartVisible.value = _foodItems.value.any { it.isSelected }
    }

    fun logout() {
        userUseCase.setLocalUser(null)
        authUseCase.logout()
    }

    fun reset() {
        _uiState.value = HomeUIState.Idle
    }
    
    companion object {
        private const val MAX_QUANTITY = 99
    }
}
