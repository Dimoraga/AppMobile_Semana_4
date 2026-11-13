package com.example.pizzeriailnono

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FlujoPedidoTest {

    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    private fun llenarDatos(nombre: String = "Camila Rojas", telefono: String = "912345678") {
        rule.onNodeWithText("Ver menú").performClick()
        rule.onNodeWithText("Nombre").performTextInput(nombre)
        rule.onNodeWithText("Dirección").performTextInput("Av. Providencia 1234")
        rule.onNodeWithText("Teléfono (9 dígitos)").performTextInput(telefono)
        rule.onNodeWithText("Correo electrónico").performTextInput("camila@correo.cl")
        rule.onNodeWithText("Seleccionar comuna").performScrollTo().performClick()
        rule.onNodeWithText("Providencia").performClick()
        rule.onNodeWithText("Continuar").performScrollTo().performClick()
    }

    @Test
    fun datosInvalidosMuestranError() {
        rule.onNodeWithText("Ver menú").performClick()
        rule.onNodeWithText("Continuar").performScrollTo().performClick()
        rule.onNodeWithText("Ingresa tu nombre.").assertIsDisplayed()
    }

    @Test
    fun telefonoInvalidoMuestraError() {
        llenarDatos(telefono = "123")
        rule.onNodeWithText("Teléfono inválido", substring = true).assertIsDisplayed()
    }

    @Test
    fun pedidoCompletoDescuentaYSeAvanzaElEstado() {
        llenarDatos()
        rule.onNodeWithText("Elige tus pizzas").assertIsDisplayed()

        // Sin pizzas no se puede continuar
        rule.onNodeWithText("Ver resumen (0)").performClick()
        rule.onNodeWithText("Elige al menos una pizza", substring = true).assertIsDisplayed()

        // Cinco Margarita => descuento por cantidad
        repeat(5) { rule.onAllNodesWithText("+")[0].performClick() }
        rule.onNodeWithText("Ya tienes el 10%", substring = true).assertIsDisplayed()
        rule.onNodeWithText("Ver resumen (5)").performClick()

        rule.onNodeWithText("Resumen del pedido").assertIsDisplayed()
        rule.onNodeWithText("Confirmar pedido").performClick()
        rule.onNodeWithText("¡Pedido confirmado!", substring = true).assertIsDisplayed()

        rule.onNodeWithText("Ver pedidos del día").performScrollTo().performClick()
        rule.onNodeWithText("Camila Rojas").assertIsDisplayed()
        rule.onNodeWithText("Estado: En preparación").assertIsDisplayed()

        rule.onNodeWithText("Avanzar a 'Listo'").performClick()
        rule.onNodeWithText("Estado: Listo").assertIsDisplayed()
        rule.onNodeWithText("Avanzar a 'En camino'").performClick()
        rule.onNodeWithText("Estado: En camino").assertIsDisplayed()

        // 5 Margarita consumieron 5 de masa (20 -> 15)
        rule.onNodeWithText("Masa: 15 de 20 unidades", substring = true).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun noSePuedePasarDelStock() {
        llenarDatos()
        // Combo Familiar usa 3 de masa: con 20 de masa solo caben 6 combos
        val combo = rule.onAllNodesWithText("+")
        repeat(30) { runCatching { combo[4].performClick() } }
        rule.onNodeWithText("Ver resumen (6)").assertIsEnabled()
    }
}
