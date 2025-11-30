package com.example.roti999.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.roti999.data.model.Order
import com.example.roti999.domain.model.FoodItem
import com.example.roti999.domain.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class EachOrderStatusViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _selectItemList = MutableStateFlow<List<FoodItem>>(emptyList())
    val selectItemList: StateFlow<List<FoodItem>> get() = _selectItemList

    fun onSetSelectItemList(selectItemList: List<FoodItem>) {
        _selectItemList.value = selectItemList
    }
    private var order: Order? = null

    fun onSetOrder(order: Order) {
        this.order = order
    }
}