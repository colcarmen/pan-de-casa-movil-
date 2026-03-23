package com.pan_de_casa.domain.repository

import com.pan_de_casa.domain.model.CartItem
import com.pan_de_casa.domain.model.Product
import kotlinx.coroutines.flow.StateFlow

interface CartRepository {
    val cartItems: StateFlow<List<CartItem>>
    fun addToCart(product: Product, quantity: Int = 1)
    fun removeFromCart(productId: String)
    fun updateQuantity(productId: String, quantity: Int)
    fun clearCart()
    fun getTotal(): Double
}
