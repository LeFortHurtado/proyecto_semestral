package com.example.dsy1105_006d_lc7.ui.movements

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.dsy1105_006d_lc7.TallerApp
import com.example.dsy1105_006d_lc7.data.model.UserRole

/**
 * Pantalla de formulario para registrar un nuevo movimiento de inventario.
 * Incorpora filtrado condicional de opciones por rol (SELLER solo Ingreso, Venta y Consumo en servicio).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovementFormScreen(
    navController: NavController,
    role: UserRole = UserRole.ADMIN,
    vm: MovementsViewModel = viewModel(
        factory = MovementsViewModel.Factory(TallerApp.instance.repository)
    )
) {
    val form = vm.formState
    val state = vm.uiState
    var typeExpanded by remember { mutableStateOf(false) }
    var productExpanded by remember { mutableStateOf(false) }

    // Filtrado condicional según el rol
    // Si es SELLER: solo Ingreso, Venta y Consumo en servicio (Ajuste y Devolución ocultos)
    val allowedMovementTypes = remember(role) {
        if (role == UserRole.SELLER) {
            listOf(
                "ingreso" to "Ingreso",
                "venta" to "Venta",
                "consumo" to "Consumo en servicio"
            )
        } else {
            listOf(
                "ingreso" to "Ingreso",
                "venta" to "Venta",
                "consumo" to "Consumo en servicio",
                "ajuste" to "Ajuste de Inventario",
                "devolucion" to "Devolución"
            )
        }
    }

    // Snackbar para retroalimentación inmediata de errores (p.ej. stock insuficiente)
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(form.error) {
        form.error?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Long
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar Movimiento", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cartel de error visual persistente
            if (form.error != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Text(form.error ?: "", color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            // ── DropdownMenu filtrado por Rol ──
            item {
                Text("Tipo de Movimiento", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }

            item {
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = !typeExpanded }
                ) {
                    val currentTypeLabel = allowedMovementTypes.find { it.first == form.tipo }?.second ?: "Seleccionar tipo"
                    OutlinedTextField(
                        value = currentTypeLabel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo de Movimiento *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        allowedMovementTypes.forEach { (value, label) ->
                            DropdownMenuItem(
                                text = { Text(label, fontWeight = if (form.tipo == value) FontWeight.Bold else FontWeight.Normal) },
                                onClick = {
                                    vm.onTipoChange(value)
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // ── Selección de producto ──
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Producto", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }

            item {
                ExposedDropdownMenuBox(
                    expanded = productExpanded,
                    onExpandedChange = { productExpanded = !productExpanded }
                ) {
                    OutlinedTextField(
                        value = form.selectedProductName.ifBlank { "Seleccionar producto" },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Producto *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = productExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = productExpanded,
                        onDismissRequest = { productExpanded = false }
                    ) {
                        state.products.forEach { product ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            product.nombre,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            "${product.codigoInterno} · Stock disponible: ${product.cantidadDisponible} uds",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    vm.onProductSelected(product)
                                    productExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Info del stock actual
            if (form.selectedProductId != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Inventory2, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                            Text("Stock disponible actual: ${form.stockActual} unidades", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // ── Cantidad ──
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Cantidad", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }

            item {
                OutlinedTextField(
                    value = form.cantidad,
                    onValueChange = vm::onCantidadChange,
                    label = { Text("Cantidad *") },
                    placeholder = { Text("Ej: 5") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // ── Motivo ──
            item {
                OutlinedTextField(
                    value = form.motivo,
                    onValueChange = vm::onMotivoChange,
                    label = { Text("Motivo / Observación") },
                    placeholder = { Text("Descripción u orden de trabajo") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // ── Botón registrar ──
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { vm.registerMovement { navController.popBackStack() } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Registrar Movimiento",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}
