package com.pan_de_casa.domain.repository

import com.pan_de_casa.domain.model.Order
import com.pan_de_casa.domain.model.OrderStatus
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    fun getOrders(): Flow<List<Order>>
    fun getOrdersByCustomer(customerId: String): Flow<List<Order>>
    fun getOrderById(orderId: String): Flow<Order?>
    suspend fun placeOrder(order: Order): Result<String>
    suspend fun updateOrderStatus(orderId: String, status: OrderStatus): Result<Unit>
}
