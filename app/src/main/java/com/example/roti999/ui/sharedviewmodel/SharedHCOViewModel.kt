package com.example.roti999.ui.sharedviewmodel

import androidx.lifecycle.ViewModel
import com.example.roti999.domain.model.User
import com.example.roti999.domain.model.FoodItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SharedHCOViewModel: ViewModel() {

    private val _selectItemList = MutableStateFlow<List<FoodItem>>(emptyList())
    val selectItemList: StateFlow<List<FoodItem>> get() = _selectItemList

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> get() = _user.asStateFlow()

    fun onSetUser(user: User?) {
        _user.value = user
    }

    fun onSetSelectItemList(selectItemList: List<FoodItem>) {
        _selectItemList.value = selectItemList
    }

    fun clearSelectItemList() {
        _selectItemList.value = emptyList()
    }
}