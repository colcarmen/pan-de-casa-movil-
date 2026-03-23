package com.pan_de_casa.ui.cart

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.pan_de_casa.domain.model.CartItem
import com.pan_de_casa.ui.components.PanButton
import com.pan_de_casa.ui.components.PanCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onNavigateToCheckout: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: CartViewModel = hiltViewModel()
) {
    val cartItems by viewModel.cartItems.collectAsState()
    val total = viewModel.getTotal()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Carrito", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (cartItems.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearCart() }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Vaciar Carrito")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (cartItems.isEmpty()) {
            EmptyCartView(onNavigateBack, modifier = Modifier.padding(padding))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(cartItems) { item ->
                        CartItemRow(
                            item = item,
                            onIncrease = { viewModel.addToCart(item.product, 1) },
                            onDecrease = { viewModel.updateQuantity(item.product.id, item.quantity - 1) },
                            onRemove = { viewModel.removeFromCart(item.product.id) }
                        )
                    }
                }
                
                CartSummary(total = total, onCheckout = onNavigateToCheckout)
            }
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit
) {
    PanCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .height(100.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = item.product.imageUrl,
                contentDescription = item.product.name,
                modifier = Modifier
                    .size(80.dp),
                contentScale = ContentScale.Crop
            )
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(item.product.name, style = MaterialTheme.typography.titleMedium)
                Text("$${item.product.price}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDecrease) { 
                        Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Menos", tint = MaterialTheme.colorScheme.primary) 
                    }
                    Text("${item.quantity}", style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = onIncrease) { 
                        Icon(Icons.Default.AddCircleOutline, contentDescription = "Más", tint = MaterialTheme.colorScheme.primary) 
                    }
                }
            }
            
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun CartSummary(total: Double, onCheckout: () -> Unit) {
    Surface(
        shadowElevation = 16.dp,
        tonalElevation = 4.dp,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total estimado:", style = MaterialTheme.typography.titleMedium)
                Text("$${total}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(20.dp))
            PanButton(
                onClick = onCheckout,
                modifier = Modifier.fillMaxWidth(),
                text = "Proceder al Pago"
            )
        }
    }
}

@Composable
fun EmptyCartView(onGoShopping: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.ShoppingBasket, contentDescription = null, modifier = Modifier.size(120.dp), tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        Spacer(modifier = Modifier.height(24.dp))
        Text("Tu carrito está vacío", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("¡Añade unos panes deliciosos!", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(32.dp))
        PanButton(onClick = onGoShopping, text = "Ir al Catálogo")
    }
}
