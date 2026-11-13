package com.example.pizzeriailnono

import java.time.LocalDate

// Los estados solo avanzan en orden: En preparación -> Listo -> En camino
enum class EstadoPedido(val texto: String) {
    EN_PREPARACION("En preparación"),
    LISTO("Listo"),
    EN_CAMINO("En camino");

    fun siguiente(): EstadoPedido? = entries.getOrNull(ordinal + 1)

    val esCompletado: Boolean get() = this == EN_CAMINO
}

class Pedido(
    val cliente: Cliente,
    val items: List<ItemPedido>,
    val repartidor: Repartidor,
    val notas: String? = null,
    val correoNotificaciones: String? = null,
    val fecha: LocalDate = LocalDate.now()
) {
    init {
        require(items.isNotEmpty()) { "Un pedido necesita al menos una pizza." }
        require(items.all { it.cantidad in RANGO_CANTIDAD }) {
            "Un pedido admite entre ${RANGO_CANTIDAD.first} y ${RANGO_CANTIDAD.last} unidades de la misma pizza."
        }
    }

    var estado: EstadoPedido = EstadoPedido.EN_PREPARACION
        private set

    fun avanzarEstado() {
        estado = estado.siguiente() ?: throw IllegalStateException("El pedido ya está '${estado.texto}' y no puede avanzar más.")
    }

    val totalPizzas: Int get() = items.sumOf { it.cantidad }
    val subtotal: Int get() = items.sumOf { it.subtotal }

    // Descuento de los productos @Promocionable, solo dentro del período de promoción
    val descuentoPromocion: Int
        get() = if (!Promociones.enPeriodo(fecha)) 0 else
            items.sumOf { it.subtotal * Promociones.descuentoDe(it.pizza) / 100 }

    // Descuento por llevar muchas pizzas (se calcula sobre lo que queda tras la promoción)
    val descuentoVolumen: Int
        get() = if (totalPizzas > MAX_PIZZAS_SIN_DESCUENTO) (subtotal - descuentoPromocion) * PORCENTAJE_DESCUENTO / 100 else 0

    val montoDescuento: Int get() = descuentoPromocion + descuentoVolumen
    val totalNeto: Int get() = subtotal - montoDescuento
    val iva: Int get() = totalNeto * TASA_IVA / 100
    val total: Int get() = totalNeto + iva

    val detallePizzas: String get() = items.joinToString(", ") { "${it.cantidad} x ${it.pizza.nombre}" }
    val notasTexto: String get() = notas ?: "Sin notas"

    operator fun component1(): Cliente = cliente
    operator fun component2(): List<ItemPedido> = items
    operator fun component3(): Int = total

    // Une dos pedidos del mismo cliente: suma las cantidades por pizza y recalcula los descuentos
    operator fun plus(otro: Pedido): Pedido {
        require(cliente == otro.cliente) { "No se pueden combinar pedidos de clientes distintos." }
        val combinados = mutableListOf<ItemPedido>()
        for (item in items + otro.items) {
            val i = combinados.indexOfFirst { it.pizza == item.pizza }
            if (i >= 0) combinados[i] = ItemPedido(item.pizza, combinados[i].cantidad + item.cantidad) else combinados += item
        }
        val notasUnidas = listOfNotNull(notas, otro.notas).joinToString("; ").ifEmpty { null }
        return Pedido(cliente, combinados, repartidor, notasUnidas, correoNotificaciones ?: otro.correoNotificaciones, fecha)
    }

    fun resumen(): String = buildString {
        appendLine("Cliente:    ${cliente.nombre} (${cliente.telefono})")
        appendLine("Dirección:  ${cliente.direccion}")
        appendLine("Repartidor: ${repartidor.nombre}")
        appendLine("Pizzas:")
        items.forEach { appendLine("  - ${it.cantidad} x ${it.pizza.nombre}: ${it.subtotal.comoMonto()}") }
        appendLine("Subtotal:   ${subtotal.comoMonto()}")
        if (descuentoPromocion > 0) appendLine("Promoción: -${descuentoPromocion.comoMonto()}")
        if (descuentoVolumen > 0) appendLine("Desc. por cantidad (${PORCENTAJE_DESCUENTO}%): -${descuentoVolumen.comoMonto()}")
        appendLine("Valor neto: ${totalNeto.comoMonto()}")
        appendLine("IVA ($TASA_IVA%):   ${iva.comoMonto()}")
        appendLine("TOTAL:      ${total.comoMonto()}")
        appendLine("Estado:     ${estado.texto}")
        append("Notas:      $notasTexto")
    }

    companion object {
        val RANGO_CANTIDAD = 1..100
        const val MAX_PIZZAS_SIN_DESCUENTO = 4   // desde la quinta pizza se aplica el descuento
        const val PORCENTAJE_DESCUENTO = 10
        const val TASA_IVA = 19
    }
}
