# 🔧 Sistema de Gestión de Inventario — Taller Mecánico Automotriz

**Asignatura:** Desarrollo de Aplicaciones Móviles (`DSY1105_006D_LC7`)  
**Entrega:** Proyecto Semestral — Octubre 2026  
**Plataforma:** Android Nativo

---

## 👥 Integrantes

| Nombre | Rol |
| :--- | :--- |
| **Kevin Hurtado** | Desarrollador |
| **Renato Le Fort** | Desarrollador |

---

## 📋 Descripción del Proyecto

Aplicación móvil Android nativa desarrollada en **Kotlin** con **Jetpack Compose** para la gestión integral del inventario de un taller mecánico automotriz. La app permite administrar productos, proveedores, ubicaciones de bodega, registrar movimientos de stock (ingresos, ventas, consumos internos) y generar alertas de reposición automática cuando las existencias caen por debajo del mínimo configurado.

El sistema implementa **control de acceso basado en roles (RBAC)** con dos perfiles diferenciados:

- **Administrador:** Acceso total a todos los módulos, incluyendo gestión de proveedores y reposición.
- **Vendedor:** Acceso restringido a consulta de productos y registro de movimientos. No puede acceder a los módulos de proveedores ni reposición.

---

## 🛠️ Stack Tecnológico

| Capa | Tecnología |
| :--- | :--- |
| **Lenguaje** | Kotlin 100% |
| **Interfaz (UI)** | Jetpack Compose + Material Design 3 |
| **Arquitectura** | MVVM + Repository Pattern + UDF |
| **Persistencia Local** | Room Database (SQLite) |
| **Asincronía** | Kotlin Coroutines & Flow |
| **Navegación** | Jetpack Navigation Compose |
| **Cámara / QR** | CameraX + Google ML Kit Barcode Scanning |
| **Red (preparado)** | Retrofit 2 + Gson |
| **Min SDK** | API 24 (Android 7.0) |
| **Target SDK** | API 36 |

---

## ✅ Funcionalidades Implementadas

### Autenticación y Control de Acceso
- Login con validación de credenciales (`admin/123` y `vendedor/123`).
- Sistema de roles RBAC (Admin y Vendedor) con restricciones de navegación.
- Cierre de sesión con limpieza completa del BackStack.

### Dashboard Principal
- Resumen de inventario en tiempo real: total de productos, stock bajo, categorías y proveedores.
- Listado de los últimos 5 movimientos recientes.
- Accesos rápidos a cada módulo del sistema.

### Módulo de Productos
- Listado reactivo con `LazyColumn` y tarjetas Material 3.
- Formulario de creación y edición de productos con validaciones.
- Asignación de proveedor, categoría y ubicación de bodega mediante dropdowns.
- Diálogo de confirmación para eliminación segura.
- Vista de detalle expandible por producto.

### Módulo de Movimientos de Stock
- Registro de ingresos, ventas y consumos internos.
- Actualización automática del stock al confirmar un movimiento.
- Selección de producto mediante lectura de código QR con la cámara del dispositivo (CameraX + ML Kit).
- Historial de movimientos con información del producto asociado.

### Módulo de Reposición (Solo Admin)
- Detección automática de productos con stock por debajo de la existencia mínima.
- Listado de alertas de reposición con datos del proveedor asociado.

### Módulo de Proveedores (Solo Admin)
- CRUD completo de proveedores (crear, listar, editar, eliminar).
- Formulario con campos de nombre, contacto y teléfono.

### Módulo de Ubicaciones
- Gestión de ubicaciones de bodega (estantes, estanterías).
- CRUD completo con campos de zona, pasillo y nivel.

### Escaneo QR
- Lectura de códigos QR con cámara en tiempo real usando CameraX.
- Reconocimiento con Google ML Kit Barcode Scanning.
- Autocompletado del producto seleccionado en el formulario de movimientos.

### Persistencia Local (Offline-First)
- Base de datos Room con entidades, DAOs y relaciones.
- Precarga inicial de datos ficticios mediante callback de Room.
- Consultas reactivas mediante Kotlin Flow.

---

## 📂 Estructura del Proyecto

```
com.example.dsy1105_006d_lc7/
├── MainActivity.kt              # Punto de entrada, setContent + Tema
├── TallerApp.kt                 # Clase Application (singleton DB y Repository)
├── data/
│   ├── local/                   # Room Database, DAOs, Entidades
│   ├── model/                   # Modelos de dominio (UserRole, AuthRepository)
│   ├── remote/                  # Retrofit API Service (preparado)
│   └── repository/              # InventoryRepository (fuente única de verdad)
├── navigation/
│   └── AppNav.kt                # Grafo de navegación + RBAC
└── ui/
    ├── dashboard/               # Dashboard (Screen + ViewModel)
    ├── login/                   # Login (HomeScreen + AuthViewModel)
    ├── products/                # Productos (Screen, FormScreen, ViewModel)
    ├── movements/               # Movimientos (Screen, FormScreen, ViewModel)
    ├── reposition/              # Reposición (Screen + ViewModel)
    ├── suppliers/               # Proveedores (Screen + ViewModel)
    ├── locations/               # Ubicaciones (Screen + ViewModel)
    ├── qr/                      # Escáner QR (CameraX + ML Kit)
    └── theme/                   # Tema Material 3 (Color, Theme, Type)
```

---

## 🚀 Pasos para Ejecutar el Proyecto

### Requisitos Previos

1. **Android Studio** Ladybug (2024.2) o superior.
2. **JDK 11** o superior.
3. **Android SDK** con API 36 instalada.
4. **Emulador** con API 24+ o un dispositivo físico Android.

### Instrucciones

1. **Clonar el repositorio:**
   ```bash
   git clone <URL_DEL_REPOSITORIO>
   ```

2. **Abrir en Android Studio:**
   - Abre Android Studio → `File` → `Open` → Selecciona la carpeta `proyecto_semestral`.

3. **Sincronizar dependencias:**
   - Android Studio sincronizará automáticamente Gradle al abrir el proyecto.
   - Si no lo hace, presiona `File` → `Sync Project with Gradle Files`.

4. **Configurar un emulador o dispositivo:**
   - `Tools` → `Device Manager` → Crear un AVD con **API 24** o superior.
   - Alternativamente, conecta un dispositivo Android físico con depuración USB activada.

5. **Ejecutar la aplicación:**
   - Selecciona el dispositivo/emulador en la barra superior.
   - Presiona el botón ▶️ **Run** (o `Shift + F10`).

6. **Credenciales de prueba:**

   | Perfil | Usuario | Contraseña |
   | :--- | :--- | :--- |
   | Administrador | `admin` | `123` |
   | Vendedor | `vendedor` | `123` |

> **Nota sobre QR:** Para probar la funcionalidad de lectura QR en un emulador, utiliza la cámara virtual del emulador apuntando a un código QR mostrado en pantalla. En dispositivo físico funciona directamente con la cámara.

---

## 📄 Documentación Adicional

| Documento | Descripción |
| :--- | :--- |
| `informe.md` | Informe técnico con los 10 hitos de desarrollo y evidencias |
| `GUIA_ESTUDIO_DEFENSA_CODIGO.md` | Guía técnica para la defensa del código fuente |
| `GUIA_PREGUNTAS_BASICAS_DEFENSA.md` | Preguntas y respuestas para la defensa oral |
| `DEFENSA_PREGUNTAS_EXAMEN.md` | Guía rápida de preguntas clave del examen |
| `LISTA_PRODUCTOS_QR.md` | Listado de productos con sus códigos QR para pruebas |

---

## 📝 Licencia

Proyecto desarrollado con fines académicos para la asignatura DSY1105 — Desarrollo de Aplicaciones Móviles.
