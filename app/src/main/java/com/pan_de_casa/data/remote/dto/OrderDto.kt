package com.pan_de_casa.data.remote.dto

import com.google.firebase.firestore.DocumentId

data class OrderItemDto(
    val id: String? = null,
    val orderId: String = "",
    val productId: String = "",
    val productName: String = "",
    val productPrice: Double = 0.0,
    val productImageUrl: String = "",
    val productDescription: String = "",
    val quantity: Int = 1,
    val priceAtTime: Double = 0.0
)

data class OrderDto(
    @DocumentId
    val id: String? = null,
    val customerId: String = "",
    val total: Double = 0.0,
    val status: String = "RECIBIDO",
    val createdAt: Long = 0L,
    val customerName: String = "",
    val customerAddress: String = "",
    val customerPhone: String = "",
    val items: List<OrderItemDto> = emptyList()
)
