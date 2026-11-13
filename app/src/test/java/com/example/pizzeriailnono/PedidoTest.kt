package com.example.pizzeriailnono

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class PedidoTest {
    private val receta = mapOf("Masa" to 1)
    private val margarita = Pizza("Margarita", 10000, receta)
    private val especial = PizzaEspecial("Cuatro Quesos", 15000, receta)
    private val cliente = Cliente("Camila Rojas", "Av. 1", "+56 (912) 345-678", "camila@correo.cl")
    private val repartidor = Repartidor("Pablo", listOf("Macul"))
    private val enPromo = LocalDate.of(2026, 11, 15)
    private val fueraPromo = LocalDate.of(2026, 12, 15)

    @Test fun totalSinDescuentos() {
        val p = Pedido(cliente, listOf(ItemPedido(margarita, 2)), repartidor, fecha = enPromo)
        assertEquals(20000, p.subtotal)
        assertEquals(0, p.montoDescuento)
        assertEquals(23800, p.total)   // 20000 + 19% IVA
    }

    @Test fun promocionSoloEnPeriodo() {
        val dentro = Pedido(cliente, listOf(ItemPedido(especial, 1)), repartidor, fecha = enPromo)
        val fuera = Pedido(cliente, listOf(ItemPedido(especial, 1)), repartidor, fecha = fueraPromo)
        assertEquals(1500, dentro.descuentoPromocion)
        assertEquals(0, fuera.descuentoPromocion)
    }

    @Test fun descuentoPorVolumen() {
        val p = Pedido(cliente, listOf(ItemPedido(margarita, 5)), repartidor, fecha = fueraPromo)
        assertEquals(5000, p.descuentoVolumen)
    }

    @Test fun sumarPedidosJuntaCantidades() {
        val a = Pedido(cliente, listOf(ItemPedido(margarita, 2)), repartidor, "sin cebolla", fecha = enPromo)
        val b = Pedido(cliente, listOf(ItemPedido(margarita, 1), ItemPedido(especial, 1)), repartidor, fecha = enPromo)
        val c = a + b
        assertEquals(2, c.items.size)
        assertEquals(3, c.items.first { it.pizza == margarita }.cantidad)
        assertEquals("sin cebolla", c.notas)
    }

    @Test(expected = IllegalArgumentException::class)
    fun sumarClientesDistintosFalla() {
        val otro = Cliente("Luis", "Av. 2", "+56 (987) 654-321", "luis@correo.cl")
        Pedido(cliente, listOf(ItemPedido(margarita, 1)), repartidor) + Pedido(otro, listOf(ItemPedido(margarita, 1)), repartidor)
    }

    @Test fun estadosAvanzanEnOrden() {
        val p = Pedido(cliente, listOf(ItemPedido(margarita, 1)), repartidor)
        p.avanzarEstado(); p.avanzarEstado()
        assertEquals(EstadoPedido.EN_CAMINO, p.estado)
        assertNull(p.estado.siguiente())
    }

    @Test fun telefonoYTextos() {
        assertEquals("+56 (912) 345-678", formatearTelefono("912345678"))
        assertEquals("+56 (912) 345-678", formatearTelefono("+56 9 1234 5678"))
        assertNull(formatearTelefono("12345"))
        assertEquals("$12.000", 12000.comoMonto())
        assertEquals("si", "  Sí ".normalizado())
        assertEquals(1, distanciaEdicion("maipu", "maipo"))
    }

    @Test fun igualdadDeClientesYProductos() {
        assertEquals(cliente, Cliente("  camila ROJAS ", "otra", "x", "CAMILA@correo.cl"))
        assertTrue(margarita == Producto("MARGARITA", 1, Categoria.PIZZA))
        assertFalse(margarita == Producto("Margarita", 1, Categoria.BEBIDA))
    }

    @Test fun resumenFuncionalidadesCorreSinErrores() {
        ResumenFuncionalidades.imprimir(listOf(margarita, especial), repartidor)
    }
}
