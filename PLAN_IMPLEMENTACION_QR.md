# Plan de Implementación: Integración de Lector QR de `camara2026` en `proyecto_semestral`

## 1. Contexto y Objetivo
En el proyecto semestral (`proyecto_semestral`), el módulo de movimientos de inventario (`MovementFormScreen`) permite a los usuarios (vendedores y administradores) registrar movimientos de entrada, venta o consumo.
El objetivo es permitir que el vendedor pueda:
1. Seleccionar un producto desde la lista desplegable tradicional (`ExposedDropdownMenuBox`).
2. **Alternativa con Lector QR**: Escanear un código QR desde la cámara del dispositivo móvil que contenga el **ID** del producto (o su código interno).
3. Al detectar el código QR, identificar automáticamente el producto en la base de datos local (Room) y cargar de inmediato todos sus datos (nombre, código interno, categoría, stock disponible, precio) en el formulario de movimiento.

---

## 2. Origen del Código (`camara2026`)
En el proyecto de referencia `camara2026` se identificaron los siguientes componentes clave:
- **Librerías**:
  - `androidx.camera:camera-core`, `camera-camera2`, `camera-lifecycle`, `camera-view` (CameraX).
  - `com.google.mlkit:barcode-scanning` (Google ML Kit Barcode Scanning con formato QR).
- **Permisos en AndroidManifest.xml**:
  - `android.permission.CAMERA` y `android.hardware.camera` (requerido=false).
- **Componentes lógicos**:
  - `CameraPermissionHelper.kt`: Validador de permisos de cámara con `ContextCompat.checkSelfPermission`.
  - `QrScanner.kt`: Composable con `PreviewView`, `ImageAnalysis` y ML Kit `BarcodeScanning.getClient(options)` con `Barcode.FORMAT_QR_CODE`.

---

## 3. Plan de Cambios Detallado por Archivo

### Paso 1: Configurar Dependencias en Gradle
- **Archivo**: `proyecto_semestral/app/build.gradle.kts`
- **Cambio**: Añadir las dependencias de CameraX y ML Kit Barcode Scanning.
```kotlin
// CameraX
val cameraxVersion = "1.4.1"
implementation("androidx.camera:camera-core:$cameraxVersion")
implementation("androidx.camera:camera-camera2:$cameraxVersion")
implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
implementation("androidx.camera:camera-view:$cameraxVersion")

// Google ML Kit Barcode Scanning
implementation("com.google.mlkit:barcode-scanning:17.3.0")
```

### Paso 2: Declarar Permisos en el Manifiesto
- **Archivo**: `proyecto_semestral/app/src/main/AndroidManifest.xml`
- **Cambio**: Añadir el permiso de cámara y la característica de hardware:
```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-feature android:name="android.hardware.camera" android:required="false"/>
```

### Paso 3: Migrar y Adaptar Utilidades QR
- **Paquete de destino**: `com.example.dsy1105_006d_lc7.ui.qr`
- **Archivos a crear**:
  1. `CameraPermissionHelper.kt`:
     - Función `hasCameraPermission(context: Context): Boolean`.
  2. `QrScanner.kt`:
     - Composable adaptado de `camara2026` para vincular CameraX a `LocalLifecycleOwner` y analizar frames con ML Kit.
  3. `QrScannerDialog.kt`:
     - Diálogo flotante o modal que contiene:
       - Visor de cámara con marco/overlay guía.
       - Botón de cancelar / cerrar.
       - Manejo automático de solicitud de permisos si no han sido concedidos aún.

### Paso 4: Extender `MovementsViewModel.kt`
- **Archivo**: `proyecto_semestral/app/src/main/java/com/example/dsy1105_006d_lc7/ui/movements/MovementsViewModel.kt`
- **Cambio**:
  - Agregar la función `onQrCodeScanned(qrContent: String): Boolean`:
    - Interpreta el contenido del código QR.
    - Soporta ID numérico directo (ej: `"5"`), prefijos comunes (ej: `"ID: 5"`, `"id=5"`), o códigos internos de producto (ej: `"FILT-001"`).
    - Busca el producto en la lista en memoria `uiState.products`.
    - Si lo encuentra, ejecuta `onProductSelected(product)` y actualiza el formulario.
    - Retorna `true` si se encontró el producto o `false` con mensaje de error si no existe.

### Paso 5: Modificar Pantalla `MovementFormScreen.kt`
- **Archivo**: `proyecto_semestral/app/src/main/java/com/example/dsy1105_006d_lc7/ui/movements/MovementFormScreen.kt`
- **Cambio**:
  - Agregar estado `showQrDialog` (`remember { mutableStateOf(false) }`).
  - Añadir botón de acción con icono `Icons.Default.QrCodeScanner` al lado de la lista desplegable o como botón de acceso rápido `"Escanear QR del Producto"`.
  - Integrar `rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission())` para solicitar permisos de cámara en caliente de forma limpia y reactiva.
  - Al abrir el escáner y leer el QR, invocar `vm.onQrCodeScanned(qrContent)`.
  - Mejorar la tarjeta de visualización de datos del producto para que muestre claramente:
    - Código interno y Nombre.
    - Categoría.
    - Stock actual en bodega.
    - Precio de venta unitario.
    - Indicador de selección rápida por QR.

---

## 4. Estrategia de Pruebas y Validación
1. **Compilación de Gradle**: Ejecutar `./gradlew compileDebugKotlin` para asegurar ausencia de errores de tipado o dependencias.
2. **Flujo de Escaneo QR**:
   - Escaneo con ID directo (`"1"`, `"2"`, etc.): Se debe cargar el producto correspondiente.
   - Escaneo con código interno (`"ACE-001"`, etc.): Se debe cargar el producto correspondiente.
   - Escaneo con código inválido: Debe mostrar un mensaje de error sin romper el flujo.
3. **Flujo Tradicional**: La lista desplegable debe seguir funcionando exactamente igual que antes.
4. **Validación de Reglas de Negocio**: Comprobar que tras seleccionar por QR, las validaciones de stock mínimo, stock negativo para ventas y tipos de movimiento por rol (vendedor vs admin) continúen activas y funcionales.
