package com.pan_de_casa.data.repository

import com.pan_de_casa.domain.model.CartItem
import com.pan_de_casa.domain.model.Product
import com.pan_de_casa.domain.repository.CartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepositoryImpl @Inject constructor() : CartRepository {

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    override val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    override fun addToCart(product: Product, quantity: Int) {
        val currentList = _cartItems.value.toMutableList()
        val existingItemIndex = currentList.indexOfFirst { it.product.id == product.id }

        if (existingItemIndex != -1) {
            val existingItem = currentList[existingItemIndex]
            currentList[existingItemIndex] = existingItem.copy(quantity = existingItem.quantity + quantity)
        } else {
            currentList.add(CartItem(product, quantity))
        }
        _cartItems.value = currentList
    }

    override fun removeFromCart(productId: String) {
        _cartItems.value = _cartItems.value.filter { it.product.id != productId }
    }

    override fun updateQuantity(productId: String, quantity: Int) {
        if (quantity <= 0) {
            removeFromCart(productId)
            return
        }
        
        val currentList = _cartItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == productId }
        if (index != -1) {
            currentList[index] = currentList[index].copy(quantity = quantity)
            _cartItems.value = currentList
        }
    }

    override fun clearCart() {
        _cartItems.value = emptyList()
    }

    override fun getTotal(): Double {
        return _cartItems.value.sumOf { it.product.price * it.quantity }
    }
}
