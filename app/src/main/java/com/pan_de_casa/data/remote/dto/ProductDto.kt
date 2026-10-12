package com.pan_de_casa.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProductDto(
    val id: Long? = null,
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val image_url: String? = null,
    val category: String? = null,
    val stock: Int = 0,
    val created_at: String? = null
)
