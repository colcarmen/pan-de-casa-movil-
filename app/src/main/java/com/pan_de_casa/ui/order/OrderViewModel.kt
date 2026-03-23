package com.pan_de_casa.ui.order

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pan_de_casa.domain.model.Order
import com.pan_de_casa.domain.model.OrderStatus
import com.pan_de_casa.domain.repository.AuthRepository
import com.pan_de_casa.domain.repository.CartRepository
import com.pan_de_casa.domain.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CheckoutState {
    object Idle : CheckoutState()
    object Loading : CheckoutState()
    data class Success(val orderId: String) : CheckoutState()
    data class Error(val message: String) : CheckoutState()
}

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val cartRepository: CartRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _checkoutState = MutableStateFlow<CheckoutState>(CheckoutState.Idle)
    val checkoutState: StateFlow<CheckoutState> = _checkoutState.asStateFlow()

    // Form State
    var name = mutableStateOf("")
    var address = mutableStateOf("")
    var phone = mutableStateOf("")

    init {
        // Pre-fill user data if logged in
        viewModelScope.launch {
            authRepository.currentUser.firstOrNull()?.let { user ->
                name.value = user.name
                address.value = user.address
                phone.value = user.phone
            }
        }
    }

    fun placeOrder() {
        val currentItems = cartRepository.cartItems.value
        if (currentItems.isEmpty()) {
            _checkoutState.value = CheckoutState.Error("El carrito está vacío")
            return
        }

        viewModelScope.launch {
            _checkoutState.value = CheckoutState.Loading
            
            val user = authRepository.currentUser.firstOrNull()
            
            val order = Order(
                customerId = user?.uid ?: "ANONYMOUS",
                customerName = name.value,
                customerAddress = address.value,
                customerPhone = phone.value,
                items = currentItems,
                total = cartRepository.getTotal(),
                status = OrderStatus.RECIBIDO,
                timestamp = System.currentTimeMillis()
            )

            orderRepository.placeOrder(order)
                .onSuccess { orderId ->
                    cartRepository.clearCart()
                    _checkoutState.value = CheckoutState.Success(orderId)
                }
                .onFailure { error ->
                    _checkoutState.value = CheckoutState.Error(error.message ?: "Error al procesar el pedido")
                }
        }
    }
    
    fun resetCheckoutState() {
        _checkoutState.value = CheckoutState.Idle
    }
}
