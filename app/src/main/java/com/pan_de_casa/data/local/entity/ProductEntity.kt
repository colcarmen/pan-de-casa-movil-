package com.pan_de_casa.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pan_de_casa.domain.model.Product

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val category: String
) {
    fun toDomain() = Product(id, name, description, price, imageUrl, category)
    
    companion object {
        fun fromDomain(product: Product) = ProductEntity(
            product.id, product.name, product.description, product.price, product.imageUrl, product.category
        )
    }
}
