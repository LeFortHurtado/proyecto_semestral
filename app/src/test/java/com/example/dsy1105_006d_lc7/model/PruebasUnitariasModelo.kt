package com.example.dsy1105_006d_lc7.model

import com.example.dsy1105_006d_lc7.model.data.local.ProductoEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Pruebas unitarias para la capa de Modelo (Entidades y Lógica de Negocio).
 */
class PruebasUnitariasModelo {

    /**
     * Prueba 1: Verifica que las credenciales válidas asignen el rol correcto (ADMIN o SELLER).
     */
    @Test
    fun probarValidacionCredencialesCorrectas() {
        // Arrange & Act: Probar usuario administrador
        val rolAdministrador = Credential.validar("admin", "123")

        // Assert: Debe reconocer el rol ADMIN
        assertNotNull("El rol no debe ser nulo para credenciales válidas de admin", rolAdministrador)
        assertEquals(UserRole.ADMIN, rolAdministrador)

        // Arrange & Act: Probar usuario vendedor
        val rolVendedor = Credential.validar("vendedor", "123")

        // Assert: Debe reconocer el rol SELLER
        assertNotNull("El rol no debe ser nulo para credenciales válidas de vendedor", rolVendedor)
        assertEquals(UserRole.SELLER, rolVendedor)

        // Arrange & Act: Probar que tolere espacios accidentales o mayúsculas
        val rolConEspaciosYMayusculas = Credential.validar("  ADMIN  ", "123")
        assertEquals(UserRole.ADMIN, rolConEspaciosYMayusculas)
    }

    /**
     * Prueba 2: Verifica que ante credenciales inválidas o incompletas se rechace el acceso (retornando null).
     */
    @Test
    fun probarValidacionCredencialesInvalidas() {
        // Caso A: Contraseña incorrecta
        val resultadoClaveIncorrecta = Credential.validar("admin", "clave_erronea")
        assertNull("Debe retornar null si la clave es incorrecta", resultadoClaveIncorrecta)

        // Caso B: Usuario inexistente
        val resultadoUsuarioNoExiste = Credential.validar("mecanico", "123")
        assertNull("Debe retornar null si el usuario no existe", resultadoUsuarioNoExiste)

        // Caso C: Campos vacíos
        val resultadoCamposVacios = Credential.validar("", "")
        assertNull("Debe retornar null cuando los campos están vacíos", resultadoCamposVacios)
    }

    /**
     * Prueba 3: Verifica la integridad de la entidad ProductoEntity con sus valores por defecto.
     */
    @Test
    fun probarValoresPorDefectoEInmutabilidadEnProducto() {
        // Arrange & Act: Crear un producto con valores esenciales
        val producto = ProductoEntity(
            id = 1L,
            codigoInterno = "REP-001",
            nombre = "Pastillas de Freno",
            categoria = "Frenos",
            precioCompra = 12000.0,
            precioVenta = 22000.0
        )

        // Assert: Verificar los valores iniciales y por defecto de la regla de negocio
        assertEquals("REP-001", producto.codigoInterno)
        assertEquals("Pastillas de Freno", producto.nombre)
        assertEquals(0, producto.cantidadDisponible) // Stock inicial debe ser 0
        assertEquals(1, producto.existenciaMinima)   // Stock mínimo de alerta por defecto es 1
        assertEquals("activo", producto.estado)      // Estado inicial activo
        assertEquals("", producto.descripcion)
        assertNull(producto.proveedorId)
        assertNull(producto.ubicacionId)

        // Inmutabilidad: modificar con copy crea una nueva instancia sin alterar la original
        val productoModificado = producto.copy(cantidadDisponible = 20)
        assertEquals(20, productoModificado.cantidadDisponible)
        assertEquals(0, producto.cantidadDisponible)
    }
}
