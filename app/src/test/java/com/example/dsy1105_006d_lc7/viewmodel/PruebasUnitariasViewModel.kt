package com.example.dsy1105_006d_lc7.viewmodel

import android.app.Application
import com.example.dsy1105_006d_lc7.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Pruebas unitarias para la capa de ViewModel (Gestión del Estado de UI y Lógica de Pantalla).
 */
class PruebasUnitariasViewModel {

    private lateinit var loginViewModel: LoginViewModel

    @Before
    fun prepararAmbiente() {
        val appFalsa = Application()
        loginViewModel = LoginViewModel(appFalsa)
    }

    /**
     * Prueba 1: Valida que un inicio de sesión correcto actualice el estado a autenticado y otorgue el rol correspondiente.
     */
    @Test
    fun probarInicioSesionExitosoActualizaEstadoYRol() {
        // Arrange: Configurar credenciales válidas en el ViewModel
        loginViewModel.onUsernameChange("admin")
        loginViewModel.onPasswordChange("123")

        var fueExitoso = false
        var usuarioRecibido = ""
        var rolRecibido: UserRole? = null

        // Act: Enviar el formulario de login
        loginViewModel.submit { usuario, rol ->
            fueExitoso = true
            usuarioRecibido = usuario
            rolRecibido = rol
        }

        // Assert: Validar resultado del callback y del estado expuesto a la vista (Compose)
        assertTrue("El callback de éxito debe ser invocado", fueExitoso)
        assertEquals("admin", usuarioRecibido)
        assertEquals(UserRole.ADMIN, rolRecibido)

        val estadoFinal = loginViewModel.currentState
        assertTrue("El estado debe reflejar isAuthenticated = true", estadoFinal.isAuthenticated)
        assertEquals(UserRole.ADMIN, estadoFinal.role)
        assertFalse("isLoading debe estar en false al finalizar", estadoFinal.isLoading)
        assertNull("No debe haber mensajes de error", estadoFinal.error)
    }

    /**
     * Prueba 2: Valida que ante credenciales erróneas no se autentique y se genere el mensaje de error para el usuario.
     */
    @Test
    fun probarInicioSesionFallidoMuestraMensajeError() {
        // Arrange: Ingresar credenciales que no coinciden
        loginViewModel.onUsernameChange("admin")
        loginViewModel.onPasswordChange("clave_incorrecta")

        var fueExitoso = false

        // Act: Enviar el formulario
        loginViewModel.submit { _, _ ->
            fueExitoso = true
        }

        // Assert: El usuario no debe ingresar y debe ver el error en pantalla
        assertFalse("El callback de éxito NO debe ser invocado", fueExitoso)

        val estadoFinal = loginViewModel.currentState
        assertFalse("El usuario NO debe quedar autenticado", estadoFinal.isAuthenticated)
        assertNull("El rol debe permanecer nulo", estadoFinal.role)
        assertNotNull("Debe existir un mensaje de error para el usuario", estadoFinal.error)
        assertTrue(
            "El mensaje de error debe indicar credenciales inválidas",
            estadoFinal.error!!.contains("Credenciales Inválidas")
        )
    }

    /**
     * Prueba 3: Valida que cerrar sesión restablezca todas las variables a su estado inicial.
     */
    @Test
    fun probarCierreDeSesionRestableceEstadoInicial() {
        // Arrange: Iniciar sesión exitosamente con vendedor
        loginViewModel.onUsernameChange("vendedor")
        loginViewModel.onPasswordChange("123")
        loginViewModel.submit { _, _ -> }

        assertTrue(loginViewModel.currentState.isAuthenticated)

        // Act: Ejecutar el cierre de sesión
        loginViewModel.logout()

        // Assert: Comprobar que los datos sensibles se hayan limpiado
        val estadoLimpio = loginViewModel.currentState
        assertFalse("Ya no debe estar autenticado", estadoLimpio.isAuthenticated)
        assertEquals("", estadoLimpio.username)
        assertEquals("", estadoLimpio.password)
        assertNull("El rol debe volver a ser null", estadoLimpio.role)
        assertNull("El error debe ser null", estadoLimpio.error)
    }

    /**
     * Prueba 4: Valida que al escribir en los campos de texto se limpie cualquier mensaje de error previo.
     */
    @Test
    fun probarEdicionDeCamposLimpiaMensajesDeError() {
        // Arrange: Provocar un error primero
        loginViewModel.onUsernameChange("usuario_inexistente")
        loginViewModel.onPasswordChange("12345")
        loginViewModel.submit { _, _ -> }

        assertNotNull("Debe haber un error activo en pantalla", loginViewModel.currentState.error)

        // Act: El usuario escribe nuevamente para corregir
        loginViewModel.onUsernameChange("admin")

        // Assert: El error previo debe desaparecer inmediatamente
        assertNull("El mensaje de error debe limpiarse al modificar el texto", loginViewModel.currentState.error)
        assertEquals("admin", loginViewModel.currentState.username)
    }
}
