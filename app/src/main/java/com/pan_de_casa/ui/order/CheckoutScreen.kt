package com.pan_de_casa.ui.order

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pan_de_casa.ui.cart.CartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onOrderConfirmed: (String) -> Unit,
    onNavigateBack: () -> Unit,
    orderViewModel: OrderViewModel = hiltViewModel(),
    cartViewModel: CartViewModel = hiltViewModel()
) {
    val checkoutState by orderViewModel.checkoutState.collectAsState()
    val cartItems by cartViewModel.cartItems.collectAsState()
    val total = cartViewModel.getTotal()

    LaunchedEffect(checkoutState) {
        if (checkoutState is CheckoutState.Success) {
            onOrderConfirmed((checkoutState as CheckoutState.Success).orderId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Finalizar Compra") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Sección Datos de Envío
            Text("Datos de Entrega", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = orderViewModel.name.value,
                onValueChange = { orderViewModel.name.value = it },
                label = { Text("Nombre Completo") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = orderViewModel.address.value,
                onValueChange = { orderViewModel.address.value = it },
                label = { Text("Dirección Exacta") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = orderViewModel.phone.value,
                onValueChange = { orderViewModel.phone.value = it },
                label = { Text("Teléfono de Contacto") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Resumen de Orden
            Text("Resumen del Pedido", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            cartItems.forEach { item ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${item.quantity}x ${item.product.name}")
                    Text("$${item.product.price * item.quantity}")
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("TOTAL", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("$${total}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (checkoutState is CheckoutState.Loading) {
                CircularProgressIndicator(modifier = Modifier.fillMaxWidth().wrapContentSize())
            } else {
                Button(
                    onClick = { orderViewModel.placeOrder() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = orderViewModel.name.value.isNotBlank() && 
                             orderViewModel.address.value.isNotBlank() && 
                             orderViewModel.phone.value.isNotBlank()
                ) {
                    Text("Confirmar Pedido")
                }
            }

            if (checkoutState is CheckoutState.Error) {
                Text(
                    text = (checkoutState as CheckoutState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
