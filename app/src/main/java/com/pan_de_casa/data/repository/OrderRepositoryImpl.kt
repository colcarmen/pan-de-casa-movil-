package com.pan_de_casa.data.repository

import com.pan_de_casa.data.local.dao.OrderDao
import com.pan_de_casa.data.local.entity.OrderEntity
import com.pan_de_casa.domain.model.Order
import com.pan_de_casa.domain.model.OrderStatus
import com.pan_de_casa.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OrderRepositoryImpl @Inject constructor(
    private val orderDao: OrderDao
) : OrderRepository {

    override fun getOrders(): Flow<List<Order>> {
        return orderDao.getAllOrders().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getOrdersByCustomer(customerId: String): Flow<List<Order>> {
        return orderDao.getOrdersByCustomer(customerId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getOrderById(orderId: String): Flow<Order?> {
        return orderDao.getOrderById(orderId).map { it?.toDomain() }
    }

    override suspend fun placeOrder(order: Order): Result<String> {
        return try {
            val orderId = "ORD-" + (1000..9999).random()
            val orderWithId = order.copy(id = orderId)
            orderDao.insertOrder(OrderEntity.fromDomain(orderWithId))
            Result.success(orderId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateOrderStatus(orderId: String, status: OrderStatus): Result<Unit> {
        return try {
            orderDao.updateOrderStatus(orderId, status)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
