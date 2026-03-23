package com.pan_de_casa.ui.admin

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pan_de_casa.domain.model.Product
import com.pan_de_casa.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

sealed class ProductFormState {
    object Idle : ProductFormState()
    object Loading : ProductFormState()
    object Success : ProductFormState()
    data class Error(val message: String) : ProductFormState()
}

@HiltViewModel
class ProductFormViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _state = MutableStateFlow<ProductFormState>(ProductFormState.Idle)
    val state: StateFlow<ProductFormState> = _state.asStateFlow()

    var name = mutableStateOf("")
    var description = mutableStateOf("")
    var price = mutableStateOf("")
    var category = mutableStateOf("General")
    var imageUri = mutableStateOf<Uri?>(null)
    var currentImageUrl = mutableStateOf("")

    fun loadProduct(productId: String) {
        if (productId == "{productId}") return
        viewModelScope.launch {
            productRepository.getProductById(productId)?.let { product ->
                name.value = product.name
                description.value = product.description
                price.value = product.price.toString()
                category.value = product.category
                currentImageUrl.value = product.imageUrl
            }
        }
    }

    private suspend fun uploadImage(uri: Uri): String {
        // Simulación: En la demo local devolvemos el URI como string
        return uri.toString()
    }

    fun saveProduct(productId: String?) {
        viewModelScope.launch {
            _state.value = ProductFormState.Loading
            try {
                val finalImageUrl = imageUri.value?.let { uploadImage(it) } ?: currentImageUrl.value
                
                val product = Product(
                    id = if (productId != null && productId != "{productId}") productId else "p" + System.currentTimeMillis(),
                    name = name.value,
                    description = description.value,
                    price = price.value.toDoubleOrNull() ?: 0.0,
                    imageUrl = finalImageUrl,
                    category = category.value
                )

                if (productId == null || productId == "{productId}") {
                    productRepository.addProduct(product)
                } else {
                    productRepository.updateProduct(product)
                }
                _state.value = ProductFormState.Success
            } catch (e: Exception) {
                _state.value = ProductFormState.Error(e.message ?: "Error al guardar")
            }
        }
    }
}
