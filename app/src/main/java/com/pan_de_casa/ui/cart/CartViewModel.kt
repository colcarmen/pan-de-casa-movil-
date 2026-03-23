package com.pan_de_casa.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pan_de_casa.domain.model.CartItem
import com.pan_de_casa.domain.model.Product
import com.pan_de_casa.domain.repository.CartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository
) : ViewModel() {

    val cartItems: StateFlow<List<CartItem>> = cartRepository.cartItems

    fun addToCart(product: Product, quantity: Int = 1) {
        cartRepository.addToCart(product, quantity)
    }

    fun removeFromCart(productId: String) {
        cartRepository.removeFromCart(productId)
    }

    fun updateQuantity(productId: String, quantity: Int) {
        cartRepository.updateQuantity(productId, quantity)
    }

    fun clearCart() {
        cartRepository.clearCart()
    }

    fun getTotal(): Double {
        return cartRepository.getTotal()
    }
}
