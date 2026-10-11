package com.pan_de_casa.data.repository

import com.pan_de_casa.data.local.dao.OrderDao
import com.pan_de_casa.data.local.entity.OrderEntity
import com.pan_de_casa.data.remote.dto.OrderDto
import com.pan_de_casa.data.remote.dto.OrderItemDto
import com.pan_de_casa.domain.model.CartItem
import com.pan_de_casa.domain.model.Order
import com.pan_de_casa.domain.model.OrderStatus
import com.pan_de_casa.domain.repository.OrderRepository
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OrderRepositoryImpl @Inject constructor(
    private val orderDao: OrderDao,
    private val postgrest: Postgrest
) : OrderRepository {

    override fun getOrders(): Flow<List<Order>> = flow {
        try {
            val remoteOrders = postgrest.from("orders")
                .select()
                .decodeList<OrderDto>()
            
            val domainOrders = remoteOrders.map { it.toDomain(emptyList()) }
            emit(domainOrders)
        } catch (e: Exception) {
            emitAll(orderDao.getAllOrders().map { entities ->
                entities.map { it.toDomain() }
            })
        }
    }

    override fun getOrdersByCustomer(customerId: String): Flow<List<Order>> = flow {
        try {
            val remoteOrders = postgrest.from("orders")
                .select {
                    filter {
                        eq("customer_id", customerId)
                    }
                }
                .decodeList<OrderDto>()
            
            emit(remoteOrders.map { it.toDomain(emptyList()) })
        } catch (e: Exception) {
            emitAll(orderDao.getOrdersByCustomer(customerId).map { entities ->
                entities.map { it.toDomain() }
            })
        }
    }

    override fun getOrderById(orderId: String): Flow<Order?> = flow {
        try {
            val dto = postgrest.from("orders")
                .select {
                    filter {
                        eq("id", orderId)
                    }
                }
                .decodeSingleOrNull<OrderDto>()
            
            emit(dto?.toDomain(emptyList()))
        } catch (e: Exception) {
            emitAll(orderDao.getOrderById(orderId).map { it?.toDomain() })
        }
    }

    override suspend fun placeOrder(order: Order): Result<String> {
        return try {
            val orderId = "ORD-" + System.currentTimeMillis().toString().takeLast(6)
            
            val orderDto = OrderDto(
                id = orderId,
                customer_id = order.customerId,
                total = order.total,
                status = order.status.name,
                created_at = order.timestamp,
                customer_name = order.customerName,
                customer_address = order.customerAddress,
                customer_phone = order.customerPhone
            )
            
            postgrest.from("orders").insert(orderDto)
            
            val itemsDto = order.items.mapNotNull { item ->
                val productId = item.product.id.toIntOrNull() ?: return@mapNotNull null
                OrderItemDto(
                    order_id = orderId,
                    product_id = productId,
                    quantity = item.quantity,
                    price_at_time = item.product.price
                )
            }
            
            if (itemsDto.isNotEmpty()) {
                postgrest.from("order_items").insert(itemsDto)
            }
            
            val orderWithId = order.copy(id = orderId)
            orderDao.insertOrder(OrderEntity.fromDomain(orderWithId))
            
            Result.success(orderId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateOrderStatus(orderId: String, status: OrderStatus): Result<Unit> {
        return try {
            postgrest.from("orders").update({
                set("status", status.name)
            }) {
                filter {
                    eq("id", orderId)
                }
            }
            orderDao.updateOrderStatus(orderId, status)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun OrderDto.toDomain(items: List<CartItem>): Order {
        val domainStatus = try {
            OrderStatus.valueOf(status)
        } catch (e: Exception) {
            OrderStatus.RECIBIDO
        }
        
        return Order(
            id = id ?: "",
            customerId = customer_id,
            total = total,
            status = domainStatus,
            timestamp = created_at,
            items = items,
            customerName = customer_name,
            customerAddress = customer_address,
            customerPhone = customer_phone
        )
    }
}
