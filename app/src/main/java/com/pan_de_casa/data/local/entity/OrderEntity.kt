package com.pan_de_casa.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pan_de_casa.domain.model.CartItem
import com.pan_de_casa.domain.model.Order
import com.pan_de_casa.domain.model.OrderStatus
import com.pan_de_casa.domain.model.Product

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: String,
    val customerId: String,
    val customerName: String,
    val customerAddress: String,
    val customerPhone: String,
    val total: Double,
    val status: OrderStatus,
    val timestamp: Long,
    val itemsSummary: String // Guardaremos "Nombre:Cantidad,Nombre:Cantidad" para la demo
) {
    fun toDomain(): Order {
        // Reconstruimos items básicos para que el Dashboard pueda contar las estadísticas
        val items = itemsSummary.split(",").filter { it.isNotEmpty() }.map {
            val parts = it.split(":")
            CartItem(
                product = Product(id = "", name = parts[0], description = "", price = 0.0, imageUrl = "", category = ""),
                quantity = parts.getOrNull(1)?.toIntOrNull() ?: 1
            )
        }
        
        return Order(
            id = id,
            customerId = customerId,
            customerName = customerName,
            customerAddress = customerAddress,
            customerPhone = customerPhone,
            total = total,
            status = status,
            timestamp = timestamp,
            items = items
        )
    }

    companion object {
        fun fromDomain(order: Order): OrderEntity {
            val summary = order.items.joinToString(",") { "${it.product.name}:${it.quantity}" }
            return OrderEntity(
                id = order.id,
                customerId = order.customerId,
                customerName = order.customerName,
                customerAddress = order.customerAddress,
                customerPhone = order.customerPhone,
                total = order.total,
                status = order.status,
                timestamp = order.timestamp,
                itemsSummary = summary
            )
        }
    }
}
