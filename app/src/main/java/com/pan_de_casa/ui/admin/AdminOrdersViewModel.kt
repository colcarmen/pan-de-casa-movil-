package com.pan_de_casa.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pan_de_casa.domain.model.Order
import com.pan_de_casa.domain.model.OrderStatus
import com.pan_de_casa.domain.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AdminOrdersState {
    object Loading : AdminOrdersState()
    data class Success(val orders: List<Order>) : AdminOrdersState()
    data class Error(val message: String) : AdminOrdersState()
}

@HiltViewModel
class AdminOrdersViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _ordersState = MutableStateFlow<AdminOrdersState>(AdminOrdersState.Loading)
    val ordersState: StateFlow<AdminOrdersState> = _ordersState.asStateFlow()

    init {
        loadOrders()
    }

    private fun loadOrders() {
        viewModelScope.launch {
            orderRepository.getOrders().collect { orders ->
                _ordersState.value = AdminOrdersState.Success(orders)
            }
        }
    }

    fun updateStatus(orderId: String, newStatus: OrderStatus) {
        viewModelScope.launch {
            orderRepository.updateOrderStatus(orderId, newStatus)
        }
    }
}
