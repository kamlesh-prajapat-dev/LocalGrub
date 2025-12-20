package com.example.roti999.ui.screens.eachorderstatus

import androidx.lifecycle.ViewModel
import com.example.roti999.data.model.Order
import com.example.roti999.data.model.SelectedDishItem
import com.example.roti999.domain.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class EachOrderStatusViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _selectItemList = MutableStateFlow<List<SelectedDishItem>>(emptyList())
    val selectItemList: StateFlow<List<SelectedDishItem>> get() = _selectItemList.asStateFlow()

    fun onSetSelectItemList(selectItemList: List<SelectedDishItem>) {
        _selectItemList.value = selectItemList
    }
    private var order: Order? = null

    fun onSetOrder(order: Order) {
        this.order = order
    }
}