package com.pan_de_casa.domain.model

enum class OrderStatus {
    RECIBIDO,
    PREPARANDO,
    LISTO,
    ENTREGADO
}

data class Order(
    val id: String = "",
    val customerId: String = "",
    val items: List<CartItem> = emptyList(),
    val total: Double = 0.0,
    val status: OrderStatus = OrderStatus.RECIBIDO,
    val timestamp: Long = System.currentTimeMillis(),
    val customerName: String = "",
    val customerAddress: String = "",
    val customerPhone: String = ""
)

data class CartItem(
    val product: Product,
    val quantity: Int
)
