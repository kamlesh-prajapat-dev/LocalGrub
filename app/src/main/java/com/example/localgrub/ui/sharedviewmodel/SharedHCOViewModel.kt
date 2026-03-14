package com.example.localgrub.ui.sharedviewmodel

import androidx.lifecycle.ViewModel
import com.example.localgrub.data.model.firebase.GetUser
import com.example.localgrub.data.model.firebase.FoodItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SharedHCOViewModel: ViewModel() {

    private val _selectItemList = MutableStateFlow<List<FoodItem>>(emptyList())
    val selectItemList: StateFlow<List<FoodItem>> get() = _selectItemList.asStateFlow()

    private val _user = MutableStateFlow<GetUser?>(null)
    val user: StateFlow<GetUser?> get() = _user.asStateFlow()

    fun onSetUser(user: GetUser?) {
        _user.value = user
    }

    fun onSetSelectItemList(selectItemList: List<FoodItem>) {
        _selectItemList.value = selectItemList
    }

    fun clearSelectItemList() {
        _selectItemList.value = emptyList()
    }

    fun reset() {
        _user.value = null
    }
}