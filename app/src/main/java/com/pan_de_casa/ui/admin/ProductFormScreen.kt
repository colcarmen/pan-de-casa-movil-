package com.pan_de_casa.ui.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.pan_de_casa.ui.components.PanButton
import com.pan_de_casa.ui.components.PanCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormScreen(
    productId: String?,
    onNavigateBack: () -> Unit,
    viewModel: ProductFormViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.imageUri.value = uri
    }

    val isEditMode = productId != null && productId != "{productId}"

    LaunchedEffect(productId) {
        if (isEditMode) {
            productId?.let { viewModel.loadProduct(it) }
        }
    }

    LaunchedEffect(state) {
        if (state is ProductFormState.Success) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Editar Pan" else "Nuevo Pan") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (state !is ProductFormState.Loading) {
                        IconButton(onClick = { viewModel.saveProduct(productId) }) {
                            Icon(Icons.Default.Save, contentDescription = "Guardar")
                        }
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PanCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                onClick = { launcher.launch("image/*") }
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    val displayImage = viewModel.imageUri.value ?: viewModel.currentImageUrl.value
                    if (displayImage.toString().isNotEmpty()) {
                        AsyncImage(
                            model = displayImage,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddPhotoAlternate, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                            Text("Seleccionar Imagen", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }

            OutlinedTextField(
                value = viewModel.name.value,
                onValueChange = { viewModel.name.value = it },
                label = { Text("Nombre del Producto") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            )

            OutlinedTextField(
                value = viewModel.description.value,
                onValueChange = { viewModel.description.value = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = MaterialTheme.shapes.medium
            )

            OutlinedTextField(
                value = viewModel.price.value,
                onValueChange = { viewModel.price.value = it },
                label = { Text("Precio") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                prefix = { Text("$") },
                shape = MaterialTheme.shapes.medium
            )

            OutlinedTextField(
                value = viewModel.category.value,
                onValueChange = { viewModel.category.value = it },
                label = { Text("Categoría") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            )

            if (state is ProductFormState.Error) {
                Text(
                    text = (state as ProductFormState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (state is ProductFormState.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                PanButton(
                    onClick = { viewModel.saveProduct(productId) },
                    modifier = Modifier.fillMaxWidth(),
                    text = if (isEditMode) "Actualizar Producto" else "Crear Producto"
                )
            }
        }
    }
}
