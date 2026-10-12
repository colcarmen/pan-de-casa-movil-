package com.pan_de_casa.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class OrderDto(
    val id: String? = null,
    val customer_id: String,
    val total: Double,
    val status: String,
    val created_at: Long,
    val customer_name: String,
    val customer_address: String,
    val customer_phone: String
)

@Serializable
data class OrderItemDto(
    val id: Long? = null,
    val order_id: String,
    val product_id: Long,
    val quantity: Int,
    val price_at_time: Double
)
