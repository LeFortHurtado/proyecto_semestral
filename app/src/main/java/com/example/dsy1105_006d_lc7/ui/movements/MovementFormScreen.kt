package com.example.dsy1105_006d_lc7.ui.movements

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

/**
 * Pantalla de formulario para registrar un nuevo movimiento de inventario.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovementFormScreen(
    navController: NavController,
    vm: MovementsViewModel = viewModel(
        factory = MovementsViewModel.Factory(TallerApp.instance.repository)
    )
) {
    val form = vm.formState
    val state = vm.uiState
    var productExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val movementTypes = listOf(
        "ingreso" to "Ingreso",
        "venta" to "Venta",
        "consumo" to "Consumo en Taller",
        "ajuste" to "Ajuste de Inventario",
        "devolucion" to "Devolución"
    )

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
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Error
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
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Text(form.error ?: "", color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }

            // ── Tipo de movimiento ──
            item {
                Text("Tipo de Movimiento", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    movementTypes.forEach { (value, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { vm.onTipoChange(value) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = form.tipo == value,
                                onClick = { vm.onTipoChange(value) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label, style = MaterialTheme.typography.bodyMedium)
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
                                            "${product.codigoInterno} · Stock: ${product.cantidadDisponible}",
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
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Inventory2, contentDescription = null)
                            Text("Stock actual: ${form.stockActual} unidades", fontWeight = FontWeight.SemiBold)
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
                    placeholder = { Text("Cantidad del movimiento") },
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
                    placeholder = { Text("Descripción opcional del movimiento") },
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
