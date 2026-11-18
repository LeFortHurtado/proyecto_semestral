package com.example.dsy1105_006d_lc7.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.dsy1105_006d_lc7.model.UserRole
import com.example.dsy1105_006d_lc7.model.data.local.MovimientoConProducto
import com.example.dsy1105_006d_lc7.viewmodel.MenuPrincipalViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pantalla del Menú Principal (Dashboard) tras iniciar sesión.
 * Muestra el resumen del taller y los accesos a los módulos con restricción por rol.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaMenuPrincipal(
    username: String,
    role: UserRole = UserRole.ADMIN,
    navController: NavController,
    dashboardVm: MenuPrincipalViewModel = viewModel()
) {
    val state = dashboardVm.uiState

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Taller Mecánico",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Bienvenido, $username · ${if (role == UserRole.ADMIN) "Perfil Administrador" else "Perfil Vendedor"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate("login") {
                            popUpTo("login") { inclusive = true }
                            launchSingleTop = true
                        }
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar sesión")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "Resumen de Inventario",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TarjetaResumen(
                        modifier = Modifier.weight(1f),
                        title = "Productos",
                        value = state.totalProducts.toString(),
                        icon = Icons.Default.Inventory2,
                        gradientColors = listOf(Color(0xFF667eea), Color(0xFF764ba2))
                    )
                    TarjetaResumen(
                        modifier = Modifier.weight(1f),
                        title = "Bajo Stock",
                        value = state.lowStockCount.toString(),
                        icon = Icons.Default.Warning,
                        gradientColors = listOf(Color(0xFFf093fb), Color(0xFFf5576c))
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TarjetaResumen(
                        modifier = Modifier.weight(1f),
                        title = "Categorías",
                        value = state.totalCategories.toString(),
                        icon = Icons.Default.Category,
                        gradientColors = listOf(Color(0xFF4facfe), Color(0xFF00f2fe))
                    )
                    if (role == UserRole.ADMIN) {
                        TarjetaResumen(
                            modifier = Modifier.weight(1f),
                            title = "Proveedores",
                            value = state.totalSuppliers.toString(),
                            icon = Icons.Default.LocalShipping,
                            gradientColors = listOf(Color(0xFF43e97b), Color(0xFF38f9d7))
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Módulos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TarjetaModulo(
                        title = "Productos",
                        description = "Gestión y consulta del inventario",
                        icon = Icons.Default.Inventory2,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        onClick = { navController.navigate("products") }
                    )
                    TarjetaModulo(
                        title = "Movimientos",
                        description = "Registrar ingresos, ventas y consumos",
                        icon = Icons.Default.SwapVert,
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        onClick = { navController.navigate("movements") }
                    )
                    if (role == UserRole.ADMIN) {
                        TarjetaModulo(
                            title = "Reposición",
                            description = "Productos bajo stock y cotizaciones",
                            icon = Icons.Default.ShoppingCart,
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            onClick = { navController.navigate("reposition") }
                        )
                        TarjetaModulo(
                            title = "Proveedores",
                            description = "Gestión de proveedores ficticios",
                            icon = Icons.Default.LocalShipping,
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            onClick = { navController.navigate("suppliers") }
                        )
                    }
                    TarjetaModulo(
                        title = "Ubicaciones",
                        description = "Zonas, estantes y repisas de bodega",
                        icon = Icons.Default.Place,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        onClick = { navController.navigate("locations") }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Últimos Movimientos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (state.recentMovements.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            "No hay movimientos registrados aún",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            items(state.recentMovements.take(5)) { movWithProduct ->
                ItemMovimientoReciente(movWithProduct)
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun TarjetaResumen(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    gradientColors: List<Color>
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(gradientColors))
                .padding(16.dp)
        ) {
            Column {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    title,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@Composable
fun TarjetaModulo(
    title: String,
    description: String,
    icon: ImageVector,
    containerColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(36.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Ir",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun ItemMovimientoReciente(movWithProduct: MovimientoConProducto) {
    val mov = movWithProduct.movement
    val prod = movWithProduct.product
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val date = dateFormat.format(Date(mov.fechaHora))

    val (typeColor, typeIcon) = when (mov.tipo) {
        "ingreso" -> Color(0xFF43e97b) to Icons.Default.ArrowDownward
        "venta" -> Color(0xFF667eea) to Icons.Default.AttachMoney
        "consumo" -> Color(0xFFf5576c) to Icons.Default.Build
        "ajuste" -> Color(0xFFffa726) to Icons.Default.Tune
        "devolucion" -> Color(0xFF4facfe) to Icons.Default.Undo
        else -> Color.Gray to Icons.Default.SwapVert
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(typeColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(typeIcon, contentDescription = null, tint = typeColor, modifier = Modifier.size(22.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    prod.nombre,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "${mov.tipo.replaceFirstChar { it.uppercase() }} · ${mov.cantidad} uds · $date",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${mov.existenciaResultante}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (mov.existenciaResultante <= 0) Color(0xFFf5576c) else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "stock",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
