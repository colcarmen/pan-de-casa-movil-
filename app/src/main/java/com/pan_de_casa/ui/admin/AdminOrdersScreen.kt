package com.pan_de_casa.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pan_de_casa.domain.model.Order
import com.pan_de_casa.domain.model.OrderStatus
import com.pan_de_casa.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrdersScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminOrdersViewModel = hiltViewModel()
) {
    val state by viewModel.ordersState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Pedidos") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (val ordersState = state) {
                is AdminOrdersState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.fillMaxSize().wrapContentSize())
                }
                is AdminOrdersState.Success -> {
                    if (ordersState.orders.isEmpty()) {
                        Text("No hay pedidos registrados", modifier = Modifier.fillMaxSize().wrapContentSize())
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(ordersState.orders) { order ->
                                AdminOrderCard(
                                    order = order,
                                    onStatusChange = { newStatus ->
                                        viewModel.updateStatus(order.id, newStatus)
                                    }
                                )
                            }
                        }
                    }
                }
                is AdminOrdersState.Error -> {
                    Text(ordersState.message, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun AdminOrderCard(
    order: Order,
    onStatusChange: (OrderStatus) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Orden #${order.id.takeLast(4)}", fontWeight = FontWeight.Bold)
                    Text(order.customerName, style = MaterialTheme.typography.bodyMedium)
                }
                
                StatusBadge(status = order.status)
                
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Cambiar Estado")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        OrderStatus.entries.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status.name) },
                                onClick = {
                                    onStatusChange(status)
                                    showMenu = false
                                }
                            )
                        }
                    }
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            order.items.forEach { item ->
                Text("${item.quantity}x ${item.product.name}", style = MaterialTheme.typography.bodySmall)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total:", fontWeight = FontWeight.Bold)
                Text("$${order.total}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun StatusBadge(status: OrderStatus) {
    val (color, bgColor) = when(status) {
        OrderStatus.RECIBIDO -> StatusPendingText to StatusPendingBg
        OrderStatus.PREPARANDO -> StatusPrepText to StatusPrepBg
        OrderStatus.LISTO -> StatusReadyText to StatusReadyBg
        OrderStatus.ENTREGADO -> StatusDeliveredText to StatusDeliveredBg
    }
    
    Surface(
        color = bgColor,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = status.name,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}
