package com.example.dsy1105_006d_lc7.ui.products

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.dsy1105_006d_lc7.TallerApp

/**
 * Pantalla de formulario para crear o editar un producto.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormScreen(
    navController: NavController,
    vm: ProductsViewModel = viewModel(
        factory = ProductsViewModel.Factory(TallerApp.instance.repository)
    )
) {
    val form = vm.formState
    val state = vm.uiState

    var supplierExpanded by remember { mutableStateOf(false) }
    var locationExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (form.isEditing) "Editar Producto" else "Nuevo Producto",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
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

            item {
                Text("Información Básica", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }

            item {
                OutlinedTextField(
                    value = form.codigoInterno,
                    onValueChange = { vm.onFormFieldChange("codigoInterno", it) },
                    label = { Text("Código Interno *") },
                    placeholder = { Text("Ej: FIL-001") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !form.isEditing // No editar código en modo edición
                )
            }

            item {
                OutlinedTextField(
                    value = form.nombre,
                    onValueChange = { vm.onFormFieldChange("nombre", it) },
                    label = { Text("Nombre *") },
                    placeholder = { Text("Ej: Filtro de aceite genérico") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = form.descripcion,
                    onValueChange = { vm.onFormFieldChange("descripcion", it) },
                    label = { Text("Descripción") },
                    placeholder = { Text("Descripción detallada del producto") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = form.categoria,
                    onValueChange = { vm.onFormFieldChange("categoria", it) },
                    label = { Text("Categoría *") },
                    placeholder = { Text("Ej: Filtros, Aceites, Frenos") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Inventario", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = form.cantidadDisponible,
                        onValueChange = { vm.onFormFieldChange("cantidadDisponible", it) },
                        label = { Text("Stock Actual") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = form.existenciaMinima,
                        onValueChange = { vm.onFormFieldChange("existenciaMinima", it) },
                        label = { Text("Stock Mínimo") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Precios", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = form.precioCompra,
                        onValueChange = { vm.onFormFieldChange("precioCompra", it) },
                        label = { Text("Precio Compra") },
                        leadingIcon = { Text("$") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = form.precioVenta,
                        onValueChange = { vm.onFormFieldChange("precioVenta", it) },
                        label = { Text("Precio Venta") },
                        leadingIcon = { Text("$") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Relaciones", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }

            // Selector de Proveedor
            item {
                ExposedDropdownMenuBox(
                    expanded = supplierExpanded,
                    onExpandedChange = { supplierExpanded = !supplierExpanded }
                ) {
                    OutlinedTextField(
                        value = state.suppliers.find { it.id == form.proveedorId }?.nombreFicticio ?: "Sin proveedor",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Proveedor") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = supplierExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = supplierExpanded,
                        onDismissRequest = { supplierExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Sin proveedor") },
                            onClick = {
                                vm.onSupplierSelected(null)
                                supplierExpanded = false
                            }
                        )
                        state.suppliers.forEach { supplier ->
                            DropdownMenuItem(
                                text = { Text(supplier.nombreFicticio) },
                                onClick = {
                                    vm.onSupplierSelected(supplier.id)
                                    supplierExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Selector de Ubicación
            item {
                ExposedDropdownMenuBox(
                    expanded = locationExpanded,
                    onExpandedChange = { locationExpanded = !locationExpanded }
                ) {
                    val locationText = state.locations.find { it.id == form.ubicacionId }?.let {
                        "${it.zona} / ${it.estante} / ${it.repisa}"
                    } ?: "Sin ubicación"

                    OutlinedTextField(
                        value = locationText,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Ubicación en Bodega") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = locationExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = locationExpanded,
                        onDismissRequest = { locationExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Sin ubicación") },
                            onClick = {
                                vm.onLocationSelected(null)
                                locationExpanded = false
                            }
                        )
                        state.locations.forEach { location ->
                            DropdownMenuItem(
                                text = { Text("${location.zona} / ${location.estante} / ${location.repisa}") },
                                onClick = {
                                    vm.onLocationSelected(location.id)
                                    locationExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Botón guardar
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { vm.saveProduct { navController.popBackStack() } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (form.isEditing) "Actualizar Producto" else "Crear Producto",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}
