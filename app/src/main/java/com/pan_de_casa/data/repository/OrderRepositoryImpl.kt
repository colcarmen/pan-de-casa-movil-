package com.pan_de_casa.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.pan_de_casa.data.local.dao.OrderDao
import com.pan_de_casa.data.local.entity.OrderEntity
import com.pan_de_casa.data.remote.dto.OrderDto
import com.pan_de_casa.data.remote.dto.OrderItemDto
import com.pan_de_casa.domain.model.CartItem
import com.pan_de_casa.domain.model.Order
import com.pan_de_casa.domain.model.OrderStatus
import com.pan_de_casa.domain.model.Product
import com.pan_de_casa.domain.repository.OrderRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class OrderRepositoryImpl @Inject constructor(
    private val orderDao: OrderDao,
    private val firestore: FirebaseFirestore
) : OrderRepository {

    override fun getOrders(): Flow<List<Order>> = flow {
        try {
            val snapshot = firestore.collection("orders").get().await()
            val domainOrders = snapshot.documents.mapNotNull { doc ->
                val dto = doc.toObject(OrderDto::class.java)
                dto?.copy(id = doc.id)?.toDomain()
            }
            emit(domainOrders)
        } catch (e: Exception) {
            android.util.Log.e("FirebaseError", "Error getOrders: ${e.message}", e)
            emitAll(orderDao.getAllOrders().map { entities ->
                entities.map { it.toDomain() }
            })
        }
    }

    override fun getOrdersByCustomer(customerId: String): Flow<List<Order>> = flow {
        try {
            val snapshot = firestore.collection("orders")
                .whereEqualTo("customerId", customerId)
                .get()
                .await()
            val domainOrders = snapshot.documents.mapNotNull { doc ->
                val dto = doc.toObject(OrderDto::class.java)
                dto?.copy(id = doc.id)?.toDomain()
            }
            emit(domainOrders)
        } catch (e: Exception) {
            android.util.Log.e("FirebaseError", "Error getOrdersByCustomer: ${e.message}", e)
            emitAll(orderDao.getOrdersByCustomer(customerId).map { entities ->
                entities.map { it.toDomain() }
            })
        }
    }

    override fun getOrderById(orderId: String): Flow<Order?> = callbackFlow {
        val listener = firestore.collection("orders").document(orderId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("FirebaseError", "Error en listener getOrderById: ${error.message}", error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val dto = snapshot.toObject(OrderDto::class.java)
                    trySend(dto?.copy(id = snapshot.id)?.toDomain())
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun placeOrder(order: Order): Result<String> {
        return try {
            val orderId = "ORD-" + System.currentTimeMillis().toString().takeLast(6)

            val itemsDto = order.items.map { item ->
                OrderItemDto(
                    productId = item.product.id,
                    productName = item.product.name,
                    productPrice = item.product.price,
                    productImageUrl = item.product.imageUrl,
                    productDescription = item.product.description,
                    quantity = item.quantity,
                    priceAtTime = item.product.price
                )
            }

            val orderDto = OrderDto(
                id = orderId,
                customerId = order.customerId,
                total = order.total,
                status = order.status.name,
                createdAt = order.timestamp,
                customerName = order.customerName,
                customerAddress = order.customerAddress,
                customerPhone = order.customerPhone,
                items = itemsDto
            )

            firestore.collection("orders").document(orderId).set(orderDto).await()

            val orderWithId = order.copy(id = orderId)
            orderDao.insertOrder(OrderEntity.fromDomain(orderWithId))

            Result.success(orderId)
        } catch (e: Exception) {
            android.util.Log.e("FirebaseError", "Error placeOrder: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun updateOrderStatus(orderId: String, status: OrderStatus): Result<Unit> {
        return try {
            firestore.collection("orders").document(orderId)
                .update("status", status.name)
                .await()
            orderDao.updateOrderStatus(orderId, status)
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("FirebaseError", "Error updateOrderStatus: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun OrderDto.toDomain(): Order {
        val domainStatus = try {
            OrderStatus.valueOf(status)
        } catch (e: Exception) {
            OrderStatus.RECIBIDO
        }

        val domainItems = items.map { itemDto ->
            CartItem(
                product = Product(
                    id = itemDto.productId,
                    name = itemDto.productName,
                    description = itemDto.productDescription,
                    price = itemDto.priceAtTime.takeIf { it > 0 } ?: itemDto.productPrice,
                    imageUrl = itemDto.productImageUrl
                ),
                quantity = itemDto.quantity
            )
        }

        return Order(
            id = id ?: "",
            customerId = customerId,
            total = total,
            status = domainStatus,
            timestamp = createdAt,
            items = domainItems,
            customerName = customerName,
            customerAddress = customerAddress,
            customerPhone = customerPhone
        )
    }
}
