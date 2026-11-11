package com.example.dsy1105_006d_lc7.ui.products

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.example.dsy1105_006d_lc7.data.model.UserRole

/**
 * Pantalla de formulario para crear o editar un producto.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormScreen(
    navController: NavController,
    role: UserRole = UserRole.ADMIN,
    productId: Long = -1L,
    vm: ProductsViewModel = viewModel(
        factory = ProductsViewModel.Factory(TallerApp.instance.repository)
    )
) {
    LaunchedEffect(productId) {
        vm.ensureFormLoaded(productId)
    }

    val form = vm.formState
    val state = vm.uiState

    var estadoExpanded by remember { mutableStateOf(false) }
    var supplierExpanded by remember { mutableStateOf(false) }
    var locationExpanded by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(form.error) {
        val error = form.error ?: return@LaunchedEffect
        listState.animateScrollToItem(0)
        snackbarHostState.showSnackbar(
            message = error,
            duration = SnackbarDuration.Short
        )
    }

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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
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
                    enabled = !form.isSaving
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
                    shape = RoundedCornerShape(12.dp),
                    enabled = !form.isSaving
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
                    shape = RoundedCornerShape(12.dp),
                    enabled = !form.isSaving
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
                    shape = RoundedCornerShape(12.dp),
                    enabled = !form.isSaving
                )
            }

            item {
                val estadoOptions = listOf(
                    "activo" to "Activo",
                    "descontinuado" to "Descontinuado"
                )
                val estadoLabel = estadoOptions.find { it.first == form.estado }?.second ?: form.estado
                ExposedDropdownMenuBox(
                    expanded = estadoExpanded,
                    onExpandedChange = { if (!form.isSaving) estadoExpanded = !estadoExpanded }
                ) {
                    OutlinedTextField(
                        value = estadoLabel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Estado") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = estadoExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !form.isSaving
                    )
                    ExposedDropdownMenu(
                        expanded = estadoExpanded,
                        onDismissRequest = { estadoExpanded = false }
                    ) {
                        estadoOptions.forEach { (value, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    vm.onEstadoSelected(value)
                                    estadoExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Inventario", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }

            item {
                if (role == UserRole.ADMIN) {
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
                            shape = RoundedCornerShape(12.dp),
                            enabled = !form.isSaving
                        )
                        OutlinedTextField(
                            value = form.existenciaMinima,
                            onValueChange = { vm.onFormFieldChange("existenciaMinima", it) },
                            label = { Text("Stock Mínimo") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !form.isSaving
                        )
                    }
                } else {
                    OutlinedTextField(
                        value = form.cantidadDisponible,
                        onValueChange = { vm.onFormFieldChange("cantidadDisponible", it) },
                        label = { Text("Stock Actual") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !form.isSaving
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Precios", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }

            item {
                if (role == UserRole.ADMIN) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = form.precioCompra,
                            onValueChange = { vm.onFormFieldChange("precioCompra", it) },
                            label = { Text("Precio Compra (Referencial)") },
                            leadingIcon = { Text("$") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !form.isSaving
                        )
                        OutlinedTextField(
                            value = form.precioVenta,
                            onValueChange = { vm.onFormFieldChange("precioVenta", it) },
                            label = { Text("Precio Venta") },
                            leadingIcon = { Text("$") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !form.isSaving
                        )
                    }
                } else {
                    OutlinedTextField(
                        value = form.precioVenta,
                        onValueChange = { vm.onFormFieldChange("precioVenta", it) },
                        label = { Text("Precio Venta") },
                        leadingIcon = { Text("$") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !form.isSaving
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
                    onExpandedChange = { if (!form.isSaving) supplierExpanded = !supplierExpanded }
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
                        shape = RoundedCornerShape(12.dp),
                        enabled = !form.isSaving
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
                    onExpandedChange = { if (!form.isSaving) locationExpanded = !locationExpanded }
                ) {
                    val locationText = state.locations.find { it.id == form.ubicacionId }?.let { loc ->
                        if (loc.posicion.isNotBlank()) {
                            "${loc.zona} / ${loc.estante} / ${loc.repisa} (${loc.posicion})"
                        } else {
                            "${loc.zona} / ${loc.estante} / ${loc.repisa}"
                        }
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
                        shape = RoundedCornerShape(12.dp),
                        enabled = !form.isSaving
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
                            val itemLabel = if (location.posicion.isNotBlank()) {
                                "${location.zona} / ${location.estante} / ${location.repisa} (${location.posicion})"
                            } else {
                                "${location.zona} / ${location.estante} / ${location.repisa}"
                            }
                            DropdownMenuItem(
                                text = { Text(itemLabel) },
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
                    enabled = !form.isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (form.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(Icons.Default.Save, contentDescription = null)
                    }
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
