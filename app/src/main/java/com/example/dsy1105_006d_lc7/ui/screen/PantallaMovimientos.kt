package com.example.dsy1105_006d_lc7.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.dsy1105_006d_lc7.model.UserRole
import com.example.dsy1105_006d_lc7.model.data.local.MovimientoConProducto
import com.example.dsy1105_006d_lc7.viewmodel.MovimientoViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pantalla de listado y gestión de movimientos de inventario (CRUD).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaMovimientos(
    navController: NavController,
    role: UserRole = UserRole.ADMIN,
    vm: MovimientoViewModel = viewModel()
) {
    val state = vm.uiState
    var selectedMovementForDetail by remember { mutableStateOf<MovimientoConProducto?>(null) }
    var showDeleteDialog by remember { mutableStateOf<MovimientoConProducto?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearMessage()
        }
    }

    val movementTypes = remember(role) {
        if (role == UserRole.SELLER) {
            listOf(
                null to "Todos",
                "ingreso" to "Ingresos",
                "venta" to "Ventas",
                "consumo" to "Consumos"
            )
        } else {
            listOf(
                null to "Todos",
                "ingreso" to "Ingresos",
                "venta" to "Ventas",
                "consumo" to "Consumos",
                "ajuste" to "Ajustes",
                "devolucion" to "Devoluciones"
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Movimientos de Inventario", fontWeight = FontWeight.Bold) },
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    vm.initFormForCreate()
                    navController.navigate("movementForm")
                },
                containerColor = MaterialTheme.colorScheme.tertiary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Registrar movimiento")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(movementTypes) { (type, label) ->
                    FilterChip(
                        selected = state.filterType == type,
                        onClick = { vm.onFilterType(type) },
                        label = { Text(label) },
                        leadingIcon = if (state.filterType == type) {
                            { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(18.dp)) }
                        } else null
                    )
                }
            }

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.movements.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.SwapVert,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "No hay movimientos registrados",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.movements, key = { it.movement.id }) { movWithProduct ->
                        ItemMovimiento(
                            movWithProduct = movWithProduct,
                            role = role,
                            onOpenDetail = { selectedMovementForDetail = movWithProduct },
                            onEdit = {
                                vm.initFormForEdit(movWithProduct)
                                navController.navigate("movementForm?movementId=${movWithProduct.movement.id}")
                            },
                            onDelete = {
                                showDeleteDialog = movWithProduct
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    selectedMovementForDetail?.let { movWithProduct ->
        DialogoDetalleMovimiento(
            movWithProduct = movWithProduct,
            onDismiss = { selectedMovementForDetail = null },
            onEdit = {
                val target = movWithProduct
                selectedMovementForDetail = null
                vm.initFormForEdit(target)
                navController.navigate("movementForm?movementId=${target.movement.id}")
            },
            onDelete = {
                val target = movWithProduct
                selectedMovementForDetail = null
                showDeleteDialog = target
            }
        )
    }

    showDeleteDialog?.let { movWithProduct ->
        val mov = movWithProduct.movement
        val prod = movWithProduct.product
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Eliminar Movimiento #${mov.id}") },
            text = {
                Text(
                    "¿Está seguro de eliminar permanentemente este movimiento de ${mov.tipo.uppercase()} (${mov.cantidad} uds) para \"${prod.nombre}\"? Esta acción revertirá automáticamente las existencias en bodega."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.deleteMovement(mov)
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

@Composable
fun ItemMovimiento(
    movWithProduct: MovimientoConProducto,
    role: UserRole,
    onOpenDetail: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val mov = movWithProduct.movement
    val prod = movWithProduct.product
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val date = dateFormat.format(Date(mov.fechaHora))

    val (typeColor, typeLabel, typeIcon) = when (mov.tipo) {
        "ingreso" -> Triple(Color(0xFF43e97b), "Ingreso", Icons.Default.ArrowDownward)
        "venta" -> Triple(Color(0xFF667eea), "Venta", Icons.Default.AttachMoney)
        "consumo" -> Triple(Color(0xFFf5576c), "Consumo Taller", Icons.Default.Build)
        "ajuste" -> Triple(Color(0xFFffa726), "Ajuste", Icons.Default.Tune)
        "devolucion" -> Triple(Color(0xFF4facfe), "Devolución", Icons.Default.Undo)
        else -> Triple(Color.Gray, mov.tipo, Icons.Default.SwapVert)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenDetail),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(typeColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(typeIcon, contentDescription = null, tint = typeColor, modifier = Modifier.size(24.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        prod.nombre,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        prod.codigoInterno,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(typeColor.copy(alpha = 0.1f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            typeLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = typeColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (mov.motivo.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            mov.motivo,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        date,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "${if (mov.tipo in listOf("ingreso", "devolucion")) "+" else "-"}${mov.cantidad}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = typeColor
                    )
                    Text(
                        "${mov.existenciaAnterior} → ${mov.existenciaResultante}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(6.dp))

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

@Composable
fun DialogoDetalleMovimiento(
    movWithProduct: MovimientoConProducto,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val mov = movWithProduct.movement
    val prod = movWithProduct.product
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
    val date = dateFormat.format(Date(mov.fechaHora))

    val (typeColor, typeLabel, typeIcon) = when (mov.tipo) {
        "ingreso" -> Triple(Color(0xFF43e97b), "Ingreso", Icons.Default.ArrowDownward)
        "venta" -> Triple(Color(0xFF667eea), "Venta", Icons.Default.AttachMoney)
        "consumo" -> Triple(Color(0xFFf5576c), "Consumo Taller", Icons.Default.Build)
        "ajuste" -> Triple(Color(0xFFffa726), "Ajuste", Icons.Default.Tune)
        "devolucion" -> Triple(Color(0xFF4facfe), "Devolución", Icons.Default.Undo)
        else -> Triple(Color.Gray, mov.tipo, Icons.Default.SwapVert)
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
                        text = "Movimiento #${mov.id}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = date,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    color = typeColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(typeIcon, contentDescription = null, tint = typeColor, modifier = Modifier.size(16.dp))
                        Text(
                            text = typeLabel,
                            color = typeColor,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
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
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Producto Asociado",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = prod.nombre,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Código: ${prod.codigoInterno}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Categoría: ${prod.categoria}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "Stock actual en bodega: ${prod.cantidadDisponible} uds",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Balance del Movimiento",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Cantidad procesada:", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "${if (mov.tipo in listOf("ingreso", "devolucion")) "+" else "-"}${mov.cantidad} uds",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = typeColor
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Existencia anterior:", style = MaterialTheme.typography.bodySmall)
                            Text("${mov.existenciaAnterior} uds", style = MaterialTheme.typography.bodySmall)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Existencia resultante:", style = MaterialTheme.typography.bodySmall)
                            Text("${mov.existenciaResultante} uds", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                if (mov.motivo.isNotBlank()) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Motivo / Observación",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = mov.motivo,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}
