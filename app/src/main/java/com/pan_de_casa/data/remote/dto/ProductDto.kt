package com.pan_de_casa.data.remote.dto

import com.google.firebase.firestore.DocumentId

data class ProductDto(
    @DocumentId
    val id: String? = null,
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val imageUrl: String? = null,
    val category: String? = "General",
    val stock: Int = 0,
    val createdAt: Long? = null
)
