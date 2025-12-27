package com.example.roti999.ui.screens.eachorderstatus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roti999.data.model.FetchedOrder
import com.example.roti999.data.model.SelectedDish
import com.example.roti999.domain.repository.OrderRepository
import com.example.roti999.domain.usecase.OrderUseCase
import com.example.roti999.util.OrderStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EachOrderStatusViewModel @Inject constructor(
    private val orderUseCase: OrderUseCase
) : ViewModel() {

    private val _selectItemList = MutableStateFlow<List<SelectedDish>>(emptyList())

    private val _uiState = MutableStateFlow<EachOrderUIState>(EachOrderUIState.Idle)
    val uiState: StateFlow<EachOrderUIState> get() = _uiState.asStateFlow()

    fun onSetSelectItemList(selectItemList: List<SelectedDish>) {
        _selectItemList.value = selectItemList
    }

    fun cancelOrder(orderId: String, status: String) {
        _uiState.value = EachOrderUIState.Loading

        viewModelScope.launch {
            _uiState.value = orderUseCase.cancelOrder(orderId, OrderStatus.CANCELLED, status)
        }
    }
}