package com.pan_de_casa.ui.order

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pan_de_casa.domain.model.Order
import com.pan_de_casa.domain.model.OrderStatus
import com.pan_de_casa.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderStatusScreen(
    orderId: String,
    onNavigateBack: () -> Unit,
    viewModel: OrderViewModel = hiltViewModel()
) {
    // En una implementación real, el ViewModel tendría un Flow para una orden específica
    // Por ahora simularemos la obtención de la orden
    val order = remember { mutableStateOf<Order?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estado del Pedido") },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Pedido #$orderId",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Barra de Progreso de 4 pasos (según SRS)
            val currentStatus = OrderStatus.RECIBIDO // Esto vendría del Flow de Firebase
            OrderProgressBar(currentStatus)
            
            Spacer(modifier = Modifier.height(48.dp))
            
            StatusInfoCard(currentStatus)
        }
    }
}

@Composable
fun OrderProgressBar(status: OrderStatus) {
    val progress = when(status) {
        OrderStatus.RECIBIDO -> 0.25f
        OrderStatus.PREPARANDO -> 0.50f
        OrderStatus.LISTO -> 0.75f
        OrderStatus.ENTREGADO -> 1f
    }
    
    val animatedProgress by animateFloatAsState(targetValue = progress)

    Column(modifier = Modifier.fillMaxWidth()) {
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatusLabel("Recibido", status >= OrderStatus.RECIBIDO)
            StatusLabel("Preparando", status >= OrderStatus.PREPARANDO)
            StatusLabel("Listo", status >= OrderStatus.LISTO)
            StatusLabel("Entregado", status >= OrderStatus.ENTREGADO)
        }
    }
}

@Composable
fun StatusLabel(label: String, isActive: Boolean) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = if (isActive) MaterialTheme.colorScheme.primary else Color.Gray,
        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
    )
}

@Composable
fun StatusInfoCard(status: OrderStatus) {
    val (title, description, color, bgColor) = when(status) {
        OrderStatus.RECIBIDO -> Quad(
            "Pedido Recibido", 
            "Estamos validando tu orden. ¡Pronto empezaremos a hornear!",
            StatusPendingText, StatusPendingBg
        )
        OrderStatus.PREPARANDO -> Quad(
            "En Preparación", 
            "Tus panes están en el horno ahora mismo. El aroma es increíble.",
            StatusPrepText, StatusPrepBg
        )
        OrderStatus.LISTO -> Quad(
            "¡Pedido Listo!", 
            "Tu orden está lista para ser recogida o enviada.",
            StatusReadyText, StatusReadyBg
        )
        OrderStatus.ENTREGADO -> Quad(
            "Entregado", 
            "¡Gracias por preferir Pan de Casa! Disfruta tu pedido.",
            StatusDeliveredText, StatusDeliveredBg
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
            Spacer(modifier = Modifier.height(8.dp))
            Text(description, style = MaterialTheme.typography.bodyMedium, color = color)
        }
    }
}

data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
