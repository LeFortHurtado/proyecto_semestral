package com.example.dsy1105_006d_lc7.ui.movements

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.dsy1105_006d_lc7.TallerApp
import com.example.dsy1105_006d_lc7.data.model.UserRole
import com.example.dsy1105_006d_lc7.ui.qr.CameraPermissionHelper
import com.example.dsy1105_006d_lc7.ui.qr.QrScannerDialog

/**
 * Pantalla de formulario para registrar un nuevo movimiento de inventario.
 * Incorpora filtrado condicional de opciones por rol (SELLER solo Ingreso, Venta y Consumo en servicio).
 * Permite seleccionar el producto mediante lista desplegable o escaneo de código QR (cámara).
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
    val context = LocalContext.current
    val form = vm.formState
    val state = vm.uiState
    var typeExpanded by remember { mutableStateOf(false) }
    var productExpanded by remember { mutableStateOf(false) }

    // Estado del escáner QR y permisos de cámara
    var hasCameraPermission by remember {
        mutableStateOf(CameraPermissionHelper.hasCameraPermission(context))
    }
    var showQrDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (isGranted) {
            showQrDialog = true
        } else {
            Toast.makeText(
                context,
                "Se requiere permiso de cámara para escanear el código QR",
                Toast.LENGTH_LONG
            ).show()
        }
    }

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

    // Snackbar para retroalimentación inmediata de errores y confirmación de escaneo QR
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(form.error) {
        form.error?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Long
            )
        }
    }

    LaunchedEffect(form.scanSuccessMessage) {
        form.scanSuccessMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            vm.clearScanSuccessMessage()
        }
    }

    // Modal para escáner QR de productos
    if (showQrDialog) {
        QrScannerDialog(
            hasCameraPermission = hasCameraPermission,
            onRequestPermission = {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            },
            onQrScanned = { rawQr ->
                vm.onQrCodeScanned(rawQr) { success, msg ->
                    if (success) {
                        showQrDialog = false
                    }
                }
            },
            onDismissRequest = { showQrDialog = false }
        )
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

            // ── Selección de producto (Lista Desplegable o Escáner QR) ──
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Producto",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    TextButton(
                        onClick = {
                            if (hasCameraPermission) {
                                showQrDialog = true
                            } else {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Escanear QR")
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        ExposedDropdownMenuBox(
                            expanded = productExpanded,
                            onExpandedChange = { productExpanded = !productExpanded }
                        ) {
                            OutlinedTextField(
                                value = form.selectedProductName.ifBlank { "Seleccionar producto..." },
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Producto (Lista o QR) *") },
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
                                                    "${product.codigoInterno} · Stock: ${product.cantidadDisponible} uds · $${product.precioVenta.toInt()}",
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

                    // Botón de acceso rápido al escáner de códigos QR
                    FilledTonalIconButton(
                        onClick = {
                            if (hasCameraPermission) {
                                showQrDialog = true
                            } else {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        modifier = Modifier.size(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            Icons.Default.QrCodeScanner,
                            contentDescription = "Escanear QR del producto"
                        )
                    }
                }
            }

            item {
                OutlinedButton(
                    onClick = {
                        if (hasCameraPermission) {
                            showQrDialog = true
                        } else {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Escanear Código QR del Producto",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // ── Ficha de datos del producto seleccionado / escaneado ──
            if (form.selectedProductId != null) {
                item {
                    val prod = form.selectedProduct ?: state.products.find { it.id == form.selectedProductId }
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = prod?.nombre ?: form.selectedProductName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "ID: ${form.selectedProductId}",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.15f),
                                thickness = 1.dp
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Código: ${prod?.codigoInterno ?: "N/A"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = "Categoría: ${prod?.categoria ?: "General"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Inventory2,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Stock disponible: ${form.stockActual} uds",
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }

                                if (prod != null && prod.precioVenta > 0) {
                                    Text(
                                        text = "Precio: $${prod.precioVenta.toInt()}",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
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
