package com.pan_de_casa.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pan_de_casa.domain.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardStats(
    val totalSales: Double = 0.0,
    val orderCount: Int = 0,
    val productStats: Map<String, Int> = emptyMap() // Product Name to Total Quantity
)

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _stats = MutableStateFlow(DashboardStats())
    val stats: StateFlow<DashboardStats> = _stats.asStateFlow()

    init {
        calculateStats()
    }

    private fun calculateStats() {
        viewModelScope.launch {
            orderRepository.getOrders().collect { orders ->
                val totalSales = orders.sumOf { it.total }
                val orderCount = orders.size
                
                val productStats = mutableMapOf<String, Int>()
                orders.forEach { order ->
                    order.items.forEach { item ->
                        val currentQty = productStats.getOrDefault(item.product.name, 0)
                        productStats[item.product.name] = currentQty + item.quantity
                    }
                }

                _stats.value = DashboardStats(
                    totalSales = totalSales,
                    orderCount = orderCount,
                    productStats = productStats
                )
            }
        }
    }
}
