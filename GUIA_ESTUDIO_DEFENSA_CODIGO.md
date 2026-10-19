# 📚 Guía de Estudio y Defensa Técnica del Código
## Proyecto: Sistema de Gestión de Inventario para Taller Mecánico (`DSY1105_006D_LC7`)

---

## 🎯 1. Resumen Ejecutivo y Ficha Técnica

* **Nombre del Proyecto:** Control de Inventario y Reposición para Taller Mecánico Automotriz.
* **Plataforma:** Android Nativo (minSdk 24, targetSdk 36, Java 11).
* **Lenguaje:** Kotlin 100%.
* **Interfaz Gráfica:** **Jetpack Compose** con **Material Design 3** (Declarativa y reactiva).
* **Persistencia Local:** **Room Database (SQLite)** con soporte asíncrono y reactivo mediante **Kotlin Coroutines & Flow**.
* **Arquitectura de Software:** **MVVM (Model-View-ViewModel)** + **Offline-First** + **Repository Pattern**.
* **Capa de Red / API:** **Retrofit 2** con conversor **Gson** (preparado para integración REST Spring Boot).
* **Control de Acceso:** **RBAC (Role-Based Access Control)** con 2 roles diferenciados: `ADMIN` y `SELLER`.

---

## 🏗️ 2. Arquitectura del Software (Flujo de Datos)

El proyecto sigue una arquitectura **Unidirectional Data Flow (UDF)** y separación estricta de responsabilidades:

```
┌────────────────────────────────────────────────────────┐
│                   VISTA (UI)                           │
│   - Jetpack Compose Screens (Screen, Cards, Dialogs)   │
│   - Observa UIState reactivamente                      │
└───────────────────────────▲────────────────────────────┘
                            │ Eventos de usuario / StateFlow
┌───────────────────────────▼────────────────────────────┐
│                  VIEWMODEL                             │
│   - Manejo de UIState (StateFlow / mutableStateOf)     │
│   - Validación de formularios y reglas de negocio      │
│   - viewModelScope.launch (Corrutinas en hilo IO)      │
└───────────────────────────▲────────────────────────────┘
                            │ Llama funciones del repositorio
┌───────────────────────────▼────────────────────────────┐
│                 REPOSITORY                             │
│   - InventoryRepository / AuthRepository               │
│   - Única fuente de verdad (Single Source of Truth)    │
│   - Coordina transacciones y reglas críticas           │
└─────────────▲─────────────────────────────▲────────────┘
              │                             │
    ┌─────────▼─────────┐         ┌─────────▼──────────┐
    │     DATA LOCAL    │         │    DATA REMOTE     │
    │  Room Database    │         │  Retrofit Service  │
    │  DAOs + Entities  │         │  (Spring Boot API) │
    └───────────────────┘         └────────────────────┘
```

---

## 📂 3. Mapa de Paquetes (`packages`) y Responsabilidad

A continuación se detalla qué almacena cada paquete del proyecto (`com.example.dsy1105_006d_lc7`):

| Paquete | ¿Qué guarda? | Propósito Técnico |
| :--- | :--- | :--- |
| **`raiz`** | `MainActivity.kt`, `TallerApp.kt` | Punto de entrada de la app y clase `Application` global que inicializa la DB como singleton. |
| **`data.local`** | `AppDatabase.kt` | Definición de la base de datos Room, migraciones, versiones y callback de precarga inicial con datos ficticios. |
| **`data.local.entity`** | `ProductEntity`, `MovementEntity`, `LocationEntity`, `SupplierEntity`, `Relations.kt` | Modelos de tablas SQLite con `@Entity`, llaves primarias, foráneas, índices y clases POJO con `@Embedded` y `@Relation`. |
| **`data.local.dao`** | `ProductDao`, `MovementDao`, `LocationDao`, `SupplierDao` | Interfaces con anotaciones `@Query`, `@Insert`, `@Update`, `@Delete` que devuelven `Flow<List<T>>` para datos reactivos. |
| **`data.model`** | `UserRole`, `Credential`, `AuthRepository` | Modelos de autenticación, enumeración de roles (`ADMIN`, `SELLER`) y credenciales demo hardcodeadas. |
| **`data.remote`** | `InventoryApiService`, `RetrofitClient` | Cliente HTTP Retrofit apuntando a `http://10.0.2.2:8080/` para conectar a microservicios/Spring Boot. |
| **`data.repository`**| `InventoryRepository.kt` | Orquestador de negocio. Encapsula DAOs y centraliza operaciones críticas como el registro de movimientos y control de stock. |
| **`navigation`** | `AppNav.kt` | Grafo de navegación (`NavHost`). Contiene los interceptores de seguridad que bloquean pantallas a usuarios no autorizados. |
| **`ui.login`** | `HomeScreen.kt`, `AuthViewModel.kt`, `LoginUiState.kt` | Pantalla de inicio de sesión con teclado responsivo, selección rápida de perfiles de prueba y validaciones. |
| **`ui.dashboard`** | `DashboardScreen.kt`, `DashboardViewModel.kt` | Pantalla principal con métricas resumidas (stock bajo, productos, categorías) y accesos directos por perfil. |
| **`ui.products`** | `ProductsScreen.kt`, `ProductFormScreen.kt`, `ProductsViewModel.kt` | Catálogo, búsqueda, filtrado por categoría, detalle con control de precios y formulario de alta/edición. |
| **`ui.movements`** | `MovementsScreen.kt`, `MovementFormScreen.kt`, `MovementsViewModel.kt` | Historial de kardex y registro de entradas/salidas con validación anti-stock negativo. |
| **`ui.reposition`** | `RepositionScreen.kt`, `RepositionViewModel.kt` | Módulo de compras/reposición. Agrupa productos bajo stock mínimo por proveedor y genera borradores de correo. |
| **`ui.suppliers`** | `SuppliersScreen.kt`, `SuppliersViewModel.kt` | CRUD de proveedores ficticios con contacto comercial. |
| **`ui.locations`** | `LocationsScreen.kt`, `LocationsViewModel.kt` | CRUD de ubicaciones físicas de bodega (Zonas, Estantes, Repisas, Posiciones). |
| **`ui.theme`** | `Color.kt`, `Theme.kt`, `Type.kt` | Paleta de colores Material 3, tipografías y tema global. |

---

## 🔍 4. Análisis Detallado de Partes Críticas y Definiciones de Funciones

### 4.1. Inicialización y Ciclo de Vida: `TallerApp.kt` y `MainActivity.kt`

#### [TallerApp.kt](file:///c:/Users/Kevin/OneDrive/Desktop/proyecto_semestral/app/src/main/java/com/example/dsy1105_006d_lc7/TallerApp.kt)
* **¿Qué es?**: Hereda de `android.app.Application`. Es el primer componente que se ejecuta al abrir la app antes de cualquier pantalla.
* **Propósito**: Implementa un contenedor de dependencias manual tipo **Singleton**.
* **Variables críticas**:
  * `val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }`: Crea la conexión SQLite en Room solo cuando se requiere por primera vez (`by lazy`).
  * `val repository: InventoryRepository by lazy { InventoryRepository(database) }`: Expone el repositorio en memoria para compartir el mismo contexto entre ViewModels.
  * `companion object { lateinit var instance: TallerApp }`: Permite que cualquier ViewModel acceda a la app global mediante `TallerApp.instance`.

#### [MainActivity.kt](file:///c:/Users/Kevin/OneDrive/Desktop/proyecto_semestral/app/src/main/java/com/example/dsy1105_006d_lc7/MainActivity.kt)
* **¿Qué es?**: La única Activity del proyecto (Single-Activity Architecture).
* **Función**: `setContent { DSY1105006DLC7Theme { Surface { AppNav() } } }`: Inyecta el grafo de Jetpack Compose en la ventana.

---

### 4.2. Persistencia y Base de Datos Local: `AppDatabase.kt` y Entidades

#### [AppDatabase.kt](file:///c:/Users/Kevin/OneDrive/Desktop/proyecto_semestral/app/src/main/java/com/example/dsy1105_006d_lc7/data/local/AppDatabase.kt)
* **Anotación:** `@Database(entities = [ProductEntity::class, MovementEntity::class, LocationEntity::class, SupplierEntity::class], version = 1)`
* **Patrón Singleton seguro (Double-Checked Locking):**
  ```kotlin
  fun getDatabase(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
          val instance = Room.databaseBuilder(...).addCallback(DatabaseCallback()).build()
          INSTANCE = instance
          instance
      }
  }
  ```
* **Precarga de Datos (Seed Data):**
  * Clase interna `DatabaseCallback : Callback()`:
  * Sobrescribe `onCreate(db: SupportSQLiteDatabase)`: Solo se dispara la **primera vez que el archivo SQLite físico se crea en el dispositivo**.
  * Ejecuta `CoroutineScope(Dispatchers.IO).launch`: Lanza en segundo plano la inserción de 6 ubicaciones (Zonas A, B, C), 4 proveedores de repuestos automotrices y 10 productos de prueba (filtros, aceites sintéticos, pastillas de freno, baterías, etc.).

#### Relaciones de Base de Datos y Modelado ([ProductEntity.kt](file:///c:/Users/Kevin/OneDrive/Desktop/proyecto_semestral/app/src/main/java/com/example/dsy1105_006d_lc7/data/local/entity/ProductEntity.kt))
* **Foreign Keys con `@ForeignKey`**:
  * `LocationEntity` ligada a `ubicacionId` con `onDelete = ForeignKey.SET_NULL`. Si se borra la ubicación, el producto no se elimina, su ubicación queda nula.
  * `SupplierEntity` ligada a `proveedorId` con `onDelete = ForeignKey.SET_NULL`.
* **Índices con `@Index`**:
  * `codigoInterno` tiene índice único (`unique = true`): Evita repuestos con códigos duplicados en SQLite.
  * Índices en `ubicacionId` y `proveedorId`: Aceleran enormemente los `JOIN` y consultas relacionales.

#### [MovementEntity.kt](file:///c:/Users/Kevin/OneDrive/Desktop/proyecto_semestral/app/src/main/java/com/example/dsy1105_006d_lc7/data/local/entity/MovementEntity.kt)
* Relación con `ProductEntity` con `onDelete = ForeignKey.CASCADE`. Si se elimina un repuesto, se eliminan sus movimientos históricos.
* Registra: `existenciaAnterior` y `existenciaResultante` además de `cantidad` y `fechaHora`. Esto proporciona **auditoría completa y trazabilidad inmutable**.

#### Clases Relacionales con `@Relation` ([Relations.kt](file:///c:/Users/Kevin/OneDrive/Desktop/proyecto_semestral/app/src/main/java/com/example/dsy1105_006d_lc7/data/local/entity/Relations.kt))
* `ProductWithDetails`:
  * `@Embedded val product: ProductEntity`
  * `@Relation(parentColumn = "ubicacionId", entityColumn = "id") val location: LocationEntity?`
  * `@Relation(parentColumn = "proveedorId", entityColumn = "id") val supplier: SupplierEntity?`
* En los DAOs, las funciones que consultan `ProductWithDetails` o `MovementWithProduct` están decoradas con **`@Transaction`**. Esto garantiza que Room realice ambas consultas de forma atómica y consistente.

---

### 4.3. Corazón de la Lógica de Negocio: `InventoryRepository.kt`

El método más crítico del proyecto es **`registerMovement`** ([InventoryRepository.kt: líneas 84-126](file:///c:/Users/Kevin/OneDrive/Desktop/proyecto_semestral/app/src/main/java/com/example/dsy1105_006d_lc7/data/repository/InventoryRepository.kt#L84-L126)):

```kotlin
suspend fun registerMovement(
    tipo: String,
    productId: Long,
    cantidad: Int,
    motivo: String = "",
    allowNegative: Boolean = false
): Result<Long>
```

#### ¿Qué hace esta función paso a paso?
1. **Busca el producto:** `val product = db.productDao().getProductById(productId)`. Si no existe, retorna `Result.failure(Exception("Producto no encontrado"))`.
2. **Obtiene el stock actual:** `val stockAnterior = product.cantidadDisponible`.
3. **Aplica cálculo según el tipo de movimiento:**
   * `"ingreso"` o `"devolucion"`: `nuevoStock = stockAnterior + cantidad`
   * `"venta"` o `"consumo"`: `nuevoStock = stockAnterior - cantidad`
   * `"ajuste"`: `nuevoStock = cantidad` (el ajuste manual sobrescribe existencias).
4. **Regla de Negocio Crítica (Bloqueo de Stock Negativo):**
   ```kotlin
   if (nuevoStock < 0 && !allowNegative) {
       return Result.failure(
           Exception("Stock insuficiente. Disponible: $stockAnterior, Solicitado: $cantidad")
       )
   }
   ```
5. **Genera el objeto `MovementEntity`**: Guarda `stockAnterior` y `existenciaResultante = nuevoStock` con timestamp actual del sistema (`System.currentTimeMillis()`).
6. **Ejecuta la persistencia**:
   * Inserta el movimiento: `db.movementDao().insert(movement)`
   * Actualiza el stock en la tabla de productos: `db.productDao().updateStock(productId, nuevoStock)`
7. **Retorna `Result.success(movementId)`**.

---

### 4.4. Control de Acceso por Roles (RBAC) y Seguridad

El sistema maneja dos niveles de usuario definidos en [UserRole.kt](file:///c:/Users/Kevin/OneDrive/Desktop/proyecto_semestral/app/src/main/java/com/example/dsy1105_006d_lc7/data/model/UserRole.kt):
* **`UserRole.ADMIN`**: Administrador / Propietario del taller.
* **`UserRole.SELLER`**: Vendedor / Operador de mesón o mecánico.

#### Mecanismos de Seguridad Implementados:

#### 1. Guardias de Navegación en [AppNav.kt](file:///c:/Users/Kevin/OneDrive/Desktop/proyecto_semestral/app/src/main/java/com/example/dsy1105_006d_lc7/navigation/AppNav.kt)
Si un usuario con rol `SELLER` intenta ingresar a la ruta `"reposition"` o `"suppliers"` (por ejemplo, mediante un deep link o manipulación de navegación), el código lo expulsa inmediatamente con:
```kotlin
composable("reposition") {
    if (currentRole == UserRole.ADMIN) {
        RepositionScreen(navController = navController)
    } else {
        LaunchedEffect(Unit) {
            navController.popBackStack() // Expulsión inmediata al Dashboard
        }
    }
}
```

#### 2. Restricción Visual en Pantalla de Productos ([ProductsScreen.kt](file:///c:/Users/Kevin/OneDrive/Desktop/proyecto_semestral/app/src/main/java/com/example/dsy1105_006d_lc7/ui/products/ProductsScreen.kt))
* **Precio de Compra Oculto**: `if (role == UserRole.ADMIN)` muestra `"Compra Ref: $X"`. Para el `SELLER`, el precio al que el taller compra a los distribuidores no se renderiza.
* **Botones de Edición y Eliminación**: Ocultos en el listado y en el diálogo de detalle si `role != UserRole.ADMIN`.
* **Modificación de Stock Mínimo**: El campo y botón para alterar `existenciaMinima` solo se dibuja para el `ADMIN`. El vendedor solo lee el stock actual.
* **Creación de Productos**: El botón flotante `FloatingActionButton` (+) solo aparece para el `ADMIN`.

#### 3. Restricción en Tipos de Movimientos ([MovementFormScreen.kt](file:///c:/Users/Kevin/OneDrive/Desktop/proyecto_semestral/app/src/main/java/com/example/dsy1105_006d_lc7/ui/movements/MovementFormScreen.kt))
* Si el rol es `SELLER`, el selector solo permite:
  1. `Ingreso`
  2. `Venta`
  3. `Consumo en servicio`
* Las opciones `Ajuste de Inventario` y `Devolución` son exclusivas del `ADMIN` para prevenir manipulaciones indebidas de stock.

---

### 4.5. Módulo de Reposición y Cotizaciones: `RepositionViewModel.kt`

#### Cálculo de la Cantidad Sugerida de Reposición:
En [RepositionViewModel.kt: líneas 58-65](file:///c:/Users/Kevin/OneDrive/Desktop/proyecto_semestral/app/src/main/java/com/example/dsy1105_006d_lc7/ui/reposition/RepositionViewModel.kt#L58-L65):
```kotlin
val sugerida = (prod.product.existenciaMinima * 2) - prod.product.cantidadDisponible
cantidadSugerida = if (sugerida > 0) sugerida else prod.product.existenciaMinima
```
* **Fórmula de reposición:** Objetivo de stock = el doble del stock mínimo.
* Se resta lo que aún queda en estantería para pedir exactamente lo necesario y no sobrestockear la bodega.

#### Generación de Borrador de Correo por Intent Implícito (`generateEmailDraft`):
* **Función:** No envía el correo en segundo plano (lo cual requeriría credenciales SMTP y violaría la interacción con el usuario).
* **Solución técnica:** Usa un **Android Intent Implícito** con acción `Intent.ACTION_SENDTO` y esquema `mailto:`.
* Abre la app de correo del dispositivo (Gmail, Outlook) precargando:
  * Destinatario: `supplier.correoFicticio`
  * Asunto: `"Solicitud de Cotización - Taller Mecánico Puente Alto"`
  * Cuerpo formateado: Listado detallado de piezas, códigos y unidades sugeridas.

---

### 4.6. Capa de Red y API REST: `RetrofitClient.kt` e `InventoryApiService.kt`

* **`RetrofitClient`**: Configura la instancia singleton con `baseUrl("http://10.0.2.2:8080/")`.
  * *Dato de examen*: `10.0.2.2` es la IP especial que usa el emulador de Android Studio para acceder al `localhost` de la computadora anfitriona donde corre Spring Boot.
* **`InventoryApiService`**: Expone endpoints como `@GET("api/productos")`, `@POST("api/movimientos")`, etc.
* **Estrategia Offline-First**: La app lee y escribe siempre en SQLite local (Room). Retrofit está preparado para sincronización asíncrona cuando el dispositivo detecte conexión activa a internet.

---

## 📊 5. Matriz Comparativa de Roles (ADMIN vs SELLER)

| Funcionalidad / Módulo | Perfil ADMIN (Administrador) | Perfil SELLER (Vendedor/Operador) |
| :--- | :---: | :---: |
| **Inicio de Sesión** | `admin / 123` | `vendedor / 123` |
| **Dashboard: Métricas generales** | Total productos, bajo stock, categorías, proveedores | Total productos, bajo stock, categorías (sin proveedores) |
| **Catálogo: Búsqueda y stock actual** | ✅ Permitido | ✅ Permitido |
| **Catálogo: Ubicación en bodega (zona/estante/repisa)**| ✅ Permitido | ✅ Permitido |
| **Catálogo: Precio de Venta al cliente** | ✅ Permitido | ✅ Permitido |
| **Catálogo: Precio de Compra referencial** | ✅ Visible | ❌ **Oculto y protegido** |
| **Catálogo: Crear nuevo repuesto** | ✅ Botón FAB (+) activo | ❌ Oculto |
| **Catálogo: Editar o Eliminar repuestos** | ✅ Permitido | ❌ Oculto |
| **Catálogo: Modificar existencia mínima** | ✅ Permitido en diálogo | ❌ Solo lectura |
| **Kardex: Ver histórico de movimientos** | ✅ Completo | ✅ Completo |
| **Movimientos: Tipos permitidos** | Ingreso, Venta, Consumo, Ajuste, Devolución | **Solo Ingreso, Venta y Consumo** |
| **Validación Anti-Stock Negativo** | ✅ Activa | ✅ Activa (Operación bloqueada) |
| **Módulo Reposición / Pedidos** | ✅ Acceso y envío de cotizaciones | ❌ **No ruteable (bloqueo en navegación)** |
| **Módulo Proveedores** | ✅ CRUD completo de proveedores | ❌ **No ruteable (bloqueo en navegación)** |
| **Módulo Ubicaciones de Bodega** | ✅ Gestión completa | ✅ Consulta y gestión de bodega |

---

## ❓ 6. Batería de Preguntas Difíciles del Profesor y Respuestas Modelo

### P1: ¿Por qué eligieron una arquitectura MVVM y qué ventajas ofrece sobre un código monolítico en la Activity?
> **Respuesta ideal:**
> *"Implementamos MVVM para desacoplar totalmente la lógica de negocio y estado de la representación visual. Los `ViewModel` sobreviven a los cambios de configuración del ciclo de vida de Android (como la rotación de pantalla o recreación del Activity). Además, al usar `StateFlow` y `mutableStateOf`, la UI es puramente declarativa y reactiva: cuando los datos en Room cambian, la UI se recompone automáticamente sin necesidad de llamar manualmente a métodos tipo `notifyDataSetChanged()` o buscar vistas con `findViewById()`."*

### P2: ¿Cómo funciona el patrón Offline-First en esta aplicación?
> **Respuesta ideal:**
> *"La aplicación toma a Room (SQLite local) como la 'Única Fuente de Verdad' (Single Source of Truth). Todas las pantallas leen directamente los `Flow` que emiten los DAOs locales. La capa de Retrofit (`InventoryApiService`) no bloquea la interfaz de usuario: la app funciona de forma 100% autónoma sin internet en el taller, permitiendo registrar ventas, ingresos y consultar ubicaciones. La arquitectura está lista para que un Worker o SyncAdapter envíe los datos a Spring Boot en segundo plano."*

### P3: ¿Por qué los DAOs retornan `Flow<List<T>>` en lugar de una simple `List<T>`?
> **Respuesta ideal:**
> *"Un método que retorna `List<T>` se ejecuta una sola vez y no se entera si la base de datos cambia. En cambio, `Flow` es un flujo asíncrono reactivo de Kotlin Coroutines. Room mantiene un observador sobre las tablas de SQLite y, cada vez que se produce un `INSERT`, `UPDATE` o `DELETE`, el DAO emite automáticamente la nueva lista de datos actualizada a los suscriptores (`collectAsState()` en Compose), manteniendo la pantalla sincronizada en tiempo real sin recargar."*

### P4: ¿Por qué usan `@Transaction` en las consultas de `ProductWithDetails` y `MovementWithProduct`?
> **Respuesta ideal:**
> *"Porque Room necesita ejecutar dos consultas SELECT independientes bajo el capó (una a la tabla principal y otra a la tabla relacionada mediante la clave foránea). La anotación `@Transaction` le indica a SQLite que ejecute ambas consultas dentro de una transacción atómica; esto previene inconsistencias temporales o condiciones de carrera si otra corrutina modifica los datos entre ambas consultas."*

### P5: ¿Cómo se garantiza que una venta o consumo no deje el stock en números negativos?
> **Respuesta ideal:**
> *"La validación se realiza en dos capas defensivas: primero en la UI/ViewModel ([MovementsViewModel.kt](file:///c:/Users/Kevin/OneDrive/Desktop/proyecto_semestral/app/src/main/java/com/example/dsy1105_006d_lc7/ui/movements/MovementsViewModel.kt)), donde se compara la cantidad ingresada contra el stock actual disponible antes de procesar. Y segundo, en el repositorio ([InventoryRepository.kt](file:///c:/Users/Kevin/OneDrive/Desktop/proyecto_semestral/app/src/main/java/com/example/dsy1105_006d_lc7/data/repository/InventoryRepository.kt)), donde se calcula `nuevoStock = stockAnterior - cantidad`. Si el resultado es menor que 0 y `allowNegative` es falso, se aborta la operación retornando un `Result.failure()` con un mensaje explícito, impidiendo tanto el INSERT en movimientos como el UPDATE en productos."*

### P6: Si un usuario con rol de Vendedor intenta entrar por URL o código a la ruta `"reposition"`, ¿cómo lo evitan?
> **Respuesta ideal:**
> *"En el archivo [AppNav.kt](file:///c:/Users/Kevin/OneDrive/Desktop/proyecto_semestral/app/src/main/java/com/example/dsy1105_006d_lc7/navigation/AppNav.kt), el `NavHost` observa el rol activo desde `AuthViewModel`. Las rutas sensibles como `reposition` y `suppliers` tienen un bloque condicional: si el rol no es `ADMIN`, no se compone la pantalla y se dispara un `LaunchedEffect(Unit) { navController.popBackStack() }` que expulsa inmediatamente la pila de navegación hacia el Dashboard. No basta con ocultar los botones visualmente; la ruta está protegida a nivel del enrutador."*

### P7: ¿Qué es `by lazy` y por qué se usó en `TallerApp.kt`?
> **Respuesta ideal:**
> *"`by lazy` es una propiedad delegada de Kotlin que asegura la inicialización perezosa. La base de datos y el repositorio no consumen memoria ni tiempo de CPU al arrancar el proceso del sistema operativo; se instancian únicamente la primera vez que un componente solicita acceder a ellos, y a partir de ese momento devuelven siempre la misma instancia en memoria (Singleton seguro)."*

### P8: ¿Por qué usaron un Intent Implícito para la cotización a proveedores en lugar de enviar el mail directamente con código?
> **Respuesta ideal:**
> *"Por tres razones: 1) Seguridad: evitamos almacenar contraseñas de cuentas de correo en el código de la app. 2) Privacidad y control del usuario: la regla del negocio estipula que el encargado del taller debe revisar el borrador antes de enviarlo. 3) Integración nativa: un Intent `ACTION_SENDTO` delega la tarea al cliente de correo preferido del usuario (Gmail, Outlook) aprovechando el sistema operativo Android."*

### P9: ¿Para qué sirve el archivo `Relations.kt` con `@Embedded`?
> **Respuesta ideal:**
> *"Room no permite almacenar directamente objetos anidados dentro de una entidad relacional. `@Embedded` le dice a Room que descomponga los campos de `ProductEntity` dentro de la clase contenedora, y `@Relation` define la vinculación con `LocationEntity` y `SupplierEntity` por ID. Esto nos permite tener un POJO limpio en Kotlin que une 3 tablas sin tener que escribir consultas SQL complejas a mano con cursores."*

### P10: ¿Cómo manejaron la compatibilidad de formularios con teclados móviles?
> **Respuesta ideal:**
> *"En `HomeScreen.kt` y los formularios usamos modificadores como `.imePadding()` y `.verticalScroll(rememberScrollState())`. Esto asegura que cuando el teclado virtual del dispositivo se despliega, el contenido se ajuste y permita scroll sin tapar los botones de acción ni los campos de texto, complementado con `ImeAction.Next` y `ImeAction.Done`."*

---

## 🎤 7. Guion Sugerido para la Exposición Oral (5 Minutos)

### Minuto 1: Introducción y Problema a Resolver
> *"Buenos días profesor(a). Nuestro proyecto consiste en un sistema móvil para la gestión integral de inventario y reposición de un taller mecánico automotriz. El desafío principal de este negocio es la alta rotación de repuestos, el riesgo de quiebres de stock y la necesidad de conocer con exactitud la ubicación física de piezas críticas en bodega (zona, estante y repisa)."*

### Minuto 2: Demostración de Roles y Seguridad (RBAC)
> *"Para garantizar la integridad y la confidencialidad, implementamos un control estricto de acceso basado en roles: Administrador y Vendedor. 
> Cuando iniciamos como **Vendedor (`vendedor/123`)**, la aplicación restringe el acceso a datos sensibles: el precio de compra a proveedores queda oculto, los botones de eliminar o modificar stock mínimo no existen, y los módulos de proveedores y reposición están completamente bloqueados tanto en la interfaz como en el grafo de navegación."*

### Minuto 3: Operatoria Diaria y Regla de Stock
> *"Al ingresar como **Administrador (`admin/123`)**, tenemos control total. En el módulo de **Movimientos**, cualquier salida por venta o consumo en servicio valida que haya stock suficiente. Si intentamos vender más piezas de las existentes, el sistema bloquea la transacción de inmediato para evitar inventario negativo, registrando en SQLite el stock anterior y el resultante para una trazabilidad fidedigna."*

### Minuto 4: Módulo de Reposición Inteligente
> *"Uno de los puntos más destacados es el módulo de **Reposición**. El sistema detecta en tiempo real los repuestos cuya existencia está bajo el stock mínimo, calcula automáticamente la cantidad sugerida de compra y los agrupa por proveedor. Con un solo clic, mediante un Intent implícito de Android, genera el borrador de cotización en el cliente de correo del usuario, listo para su envío."*

### Minuto 5: Arquitectura Técnica y Conclusión
> *"A nivel de arquitectura, usamos **Jetpack Compose** para una UI moderna y declarativa, **Room** con **Kotlin Flow** para un funcionamiento reactivo y Offline-First, y el patrón **MVVM con Repositorio**. La app está lista además para conectarse a microservicios REST mediante Retrofit. Con esto logramos una aplicación robusta, segura y alineada a las mejores prácticas de la industria."*

---

## 📌 8. Chuleta Rápida de Archivos para Abrir en la Demostración

Si el profesor pide: *"Muéstrame el código donde..."*

| Pregunta del Profesor | Archivo a Abrir | Líneas clave |
| :--- | :--- | :--- |
| *"...bloqueas que el stock quede en negativo"* | [InventoryRepository.kt](file:///c:/Users/Kevin/OneDrive/Desktop/proyecto_semestral/app/src/main/java/com/example/dsy1105_006d_lc7/data/repository/InventoryRepository.kt) | Líneas 96 - 108 |
| *"...ocultas el precio de compra al vendedor"* | [ProductsScreen.kt](file:///c:/Users/Kevin/OneDrive/Desktop/proyecto_semestral/app/src/main/java/com/example/dsy1105_006d_lc7/ui/products/ProductsScreen.kt) | Líneas 338 - 347 y 527 - 564 |
| *"...bloqueas las rutas de navegación para el rol SELLER"* | [AppNav.kt](file:///c:/Users/Kevin/OneDrive/Desktop/proyecto_semestral/app/src/main/java/com/example/dsy1105_006d_lc7/navigation/AppNav.kt) | Líneas 97 - 118 |
| *"...creas la base de datos y precargas los datos ficticios"* | [AppDatabase.kt](file:///c:/Users/Kevin/OneDrive/Desktop/proyecto_semestral/app/src/main/java/com/example/dsy1105_006d_lc7/data/local/AppDatabase.kt) | Líneas 65 - 102 |
| *"...haces el JOIN / Relación entre Producto, Ubicación y Proveedor"*| [Relations.kt](file:///c:/Users/Kevin/OneDrive/Desktop/proyecto_semestral/app/src/main/java/com/example/dsy1105_006d_lc7/data/local/entity/Relations.kt) y [ProductDao.kt](file:///c:/Users/Kevin/OneDrive/Desktop/proyecto_semestral/app/src/main/java/com/example/dsy1105_006d_lc7/data/local/dao/ProductDao.kt) | Relations.kt (Líneas 11-23), ProductDao.kt (Línea 53) |
| *"...calculas la cantidad a reponer y generas el mail"* | [RepositionViewModel.kt](file:///c:/Users/Kevin/OneDrive/Desktop/proyecto_semestral/app/src/main/java/com/example/dsy1105_006d_lc7/ui/reposition/RepositionViewModel.kt) | Líneas 58 - 65 y Líneas 86 - 126 |
| *"...defines los endpoints de Retrofit para Spring Boot"* | [InventoryApiService.kt](file:///c:/Users/Kevin/OneDrive/Desktop/proyecto_semestral/app/src/main/java/com/example/dsy1105_006d_lc7/data/remote/InventoryApiService.kt) | Líneas 15 - 57 |
