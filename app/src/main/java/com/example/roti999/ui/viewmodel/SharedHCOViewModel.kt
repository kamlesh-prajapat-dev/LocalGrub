package com.example.roti999.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.roti999.data.model.User
import com.example.roti999.domain.model.FoodItem

class SharedHCOViewModel: ViewModel() {

    private val _selectItemList = MutableLiveData<List<FoodItem>>()
    val selectItemList: LiveData<List<FoodItem>> = _selectItemList

    private val _user = MutableLiveData<User?>(null)
    val user: LiveData<User?> = _user

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