package com.example.dsy1105_006d_lc7.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.dsy1105_006d_lc7.model.UserRole
import com.example.dsy1105_006d_lc7.model.data.local.ProductoConDetalles
import com.example.dsy1105_006d_lc7.viewmodel.ProductoViewModel

/**
 * Pantalla de inventario de repuestos y productos.
 * Control de acceso:
 * - ADMIN: Gestión completa (crear, editar, eliminar, ajustar stock mínimo y ver precios de compra).
 * - SELLER: Consulta de repuestos, stock disponible, ubicación física y precio de venta.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaProductos(
    navController: NavController,
    role: UserRole = UserRole.ADMIN,
    vm: ProductoViewModel = viewModel()
) {
    val state = vm.uiState
    var showDeleteDialog by remember { mutableStateOf<ProductoConDetalles?>(null) }
    var selectedProductForDetail by remember { mutableStateOf<ProductoConDetalles?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Inventario de Productos", fontWeight = FontWeight.Bold)
                        Text(
                            text = if (role == UserRole.ADMIN) "Modo Administrador (Total)" else "Modo Vendedor (Solo Consulta)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
        floatingActionButton = {
            if (role == UserRole.ADMIN) {
                FloatingActionButton(
                    onClick = {
                        vm.initFormForCreate()
                        navController.navigate("productForm")
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar producto")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = vm::onSearchQueryChange,
                placeholder = { Text("Buscar por código o nombre de repuesto...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (state.searchQuery.isNotBlank()) {
                        IconButton(onClick = { vm.onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = state.selectedCategory == null,
                        onClick = { vm.onCategoryFilter(null) },
                        label = { Text("Todas") },
                        leadingIcon = if (state.selectedCategory == null) {
                            { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(18.dp)) }
                        } else null
                    )
                }
                items(state.categories, key = { it }) { category ->
                    FilterChip(
                        selected = state.selectedCategory == category,
                        onClick = { vm.onCategoryFilter(category) },
                        label = { Text(category) },
                        leadingIcon = if (state.selectedCategory == category) {
                            { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(18.dp)) }
                        } else null
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.products.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Inventory2,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "No se encontraron productos",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.products, key = { it.product.id }) { productWithDetails ->
                        ItemProducto(
                            productWithDetails = productWithDetails,
                            role = role,
                            onOpenDetail = { selectedProductForDetail = productWithDetails },
                            onEdit = {
                                vm.initFormForEdit(productWithDetails.product)
                                navController.navigate("productForm?productId=${productWithDetails.product.id}")
                            },
                            onDelete = {
                                showDeleteDialog = productWithDetails
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }

        selectedProductForDetail?.let { prodWithDetails ->
            DialogoDetalleProducto(
                productWithDetails = prodWithDetails,
                role = role,
                onDismiss = { selectedProductForDetail = null },
                onEdit = {
                    val toEdit = prodWithDetails.product
                    selectedProductForDetail = null
                    vm.initFormForEdit(toEdit)
                    navController.navigate("productForm?productId=${toEdit.id}")
                },
                onUpdateMinStock = { newMinStock ->
                    vm.updateMinStock(prodWithDetails.product, newMinStock)
                    selectedProductForDetail = null
                },
                onDelete = {
                    val toDelete = prodWithDetails
                    selectedProductForDetail = null
                    showDeleteDialog = toDelete
                }
            )
        }

        if (role == UserRole.ADMIN) {
            showDeleteDialog?.let { prodWithDetails ->
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = null },
                    title = { Text("Eliminar Producto") },
                    text = { Text("¿Está seguro de eliminar permanentemente \"${prodWithDetails.product.nombre}\"? Esta acción no se puede deshacer.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                vm.deleteProduct(prodWithDetails.product)
                                showDeleteDialog = null
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Eliminar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = null }) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ItemProducto(
    productWithDetails: ProductoConDetalles,
    role: UserRole,
    onOpenDetail: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val product = productWithDetails.product
    val location = productWithDetails.location

    val stockColor = when {
        product.cantidadDisponible <= 0 -> Color(0xFFf5576c)
        product.cantidadDisponible <= product.existenciaMinima -> Color(0xFFffa726)
        else -> Color(0xFF43e97b)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenDetail),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        product.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        product.codigoInterno,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(stockColor.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        "${product.cantidadDisponible} uds",
                        color = stockColor,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column {
                FilaDetalle(Icons.Default.Category, "Categoría", product.categoria)
                if (location != null) {
                    FilaDetalle(
                        Icons.Default.Place,
                        "Ubicación",
                        "Zona ${location.zona} · Estante ${location.estante} · Repisa ${location.repisa}"
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (role == UserRole.ADMIN) {
                    Text(
                        "Compra Ref: \$${String.format("%,.0f", product.precioCompra)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Spacer(modifier = Modifier.width(4.dp))
                }

                Text(
                    "Venta: \$${String.format("%,.0f", product.precioVenta)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (product.estado != "activo") {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Estado: Descontinuado",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (product.cantidadDisponible <= product.existenciaMinima) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "⚠ Stock bajo mínimo (mín: ${product.existenciaMinima})",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFf5576c),
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onOpenDetail) {
                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ver Detalle")
                }

                if (role == UserRole.ADMIN) {
                    TextButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Editar")
                    }

                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Eliminar")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoDetalleProducto(
    productWithDetails: ProductoConDetalles,
    role: UserRole,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onUpdateMinStock: (Int) -> Unit,
    onDelete: () -> Unit
) {
    val product = productWithDetails.product
    val location = productWithDetails.location
    val supplier = productWithDetails.supplier

    var minStockInput by remember(product.id, product.existenciaMinima) {
        mutableStateOf(product.existenciaMinima.toString())
    }
    var minStockError by remember(product.id, product.existenciaMinima) {
        mutableStateOf(false)
    }

    val stockBadgeColor = when {
        product.cantidadDisponible <= 0 -> Color(0xFFf5576c)
        product.cantidadDisponible <= product.existenciaMinima -> Color(0xFFffa726)
        else -> Color(0xFF43e97b)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.nombre,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Código: ${product.codigoInterno}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Surface(
                    color = stockBadgeColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${product.cantidadDisponible} uds",
                        color = stockBadgeColor,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (product.descripcion.isNotBlank()) {
                    Text(
                        text = product.descripcion,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                FilaDetalle(Icons.Default.Category, "Categoría", product.categoria)
                FilaDetalle(
                    Icons.Default.Info,
                    "Estado",
                    if (product.estado == "descontinuado") "Descontinuado" else "Activo"
                )

                val ubicacionTexto = location?.let {
                    "Zona ${it.zona} · Estante ${it.estante} · Repisa ${it.repisa}"
                } ?: "Sin ubicación asignada"
                FilaDetalle(Icons.Default.Place, "Ubicación en Bodega", ubicacionTexto)

                if (role == UserRole.ADMIN && supplier != null) {
                    FilaDetalle(Icons.Default.LocalShipping, "Proveedor", supplier.nombreFicticio)
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                Text(
                    text = "Precios",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Precio Venta al Público:", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "$${String.format("%,.0f", product.precioVenta)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (role == UserRole.ADMIN) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = "Sensible",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Precio Compra Referencial:",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Text(
                                "$${String.format("%,.0f", product.precioCompra)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                if (role == UserRole.ADMIN) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Modificar Existencia Mínima",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = minStockInput,
                                onValueChange = {
                                    minStockInput = it
                                    minStockError = false
                                },
                                label = { Text("Mínimo") },
                                isError = minStockError,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            Button(
                                onClick = {
                                    val newMin = minStockInput.trim().toIntOrNull()
                                    if (newMin != null && newMin >= 0) {
                                        onUpdateMinStock(newMin)
                                    } else {
                                        minStockError = true
                                    }
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Guardar")
                            }
                        }
                        if (minStockError) {
                            Text(
                                text = "Ingresa un número entero mayor o igual a 0",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Existencias disponibles: ${product.cantidadDisponible} unidades",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (role == UserRole.ADMIN) {
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = onEdit,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Editar Producto")
                    }
                    OutlinedButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Eliminar Producto")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
fun FilaDetalle(icon: ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            "$label: $value",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
