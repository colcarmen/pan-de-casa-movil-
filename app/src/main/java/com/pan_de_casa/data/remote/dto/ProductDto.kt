package com.pan_de_casa.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProductDto(
    val id: Int? = null,
    val name: String,
    val description: String,
    val price: Double,
    val image_url: String? = null,
    val category: String? = null,
    val stock: Int = 0
)
