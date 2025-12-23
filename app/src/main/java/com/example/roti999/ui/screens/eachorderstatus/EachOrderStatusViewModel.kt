package com.example.roti999.ui.screens.eachorderstatus

import androidx.lifecycle.ViewModel
import com.example.roti999.data.model.FetchedOrder
import com.example.roti999.data.model.SelectedDish
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

    private val _selectItemList = MutableStateFlow<List<SelectedDish>>(emptyList())
    val selectItemList: StateFlow<List<SelectedDish>> get() = _selectItemList.asStateFlow()

    fun onSetSelectItemList(selectItemList: List<SelectedDish>) {
        _selectItemList.value = selectItemList
    }
    private var order: FetchedOrder? = null

    fun onSetOrder(order: FetchedOrder) {
        this.order = order
    }
}