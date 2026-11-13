package com.example.pizzeriailnono

import java.time.LocalDate

// Marca las clases de productos (pizzas o combos) que califican para promociones especiales.
// Se lee en tiempo de ejecución con reflection, así que agregar un producto promocionable
// es solo anotar su clase: no hay que tocar la lógica de pedidos.
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Promocionable(val descuentoPorcentaje: Int = 10)

object Promociones {
    // Período de promoción (rango de fechas, ambos extremos incluidos)
    val PERIODO: ClosedRange<LocalDate> = LocalDate.of(2026, 11, 1)..LocalDate.of(2026, 11, 30)

    fun enPeriodo(fecha: LocalDate): Boolean = fecha in PERIODO

    // Busca la anotación @Promocionable en la clase real del producto (PizzaEspecial, Combo...)
    private fun anotacionDe(producto: Producto): Promocionable? =
        producto.javaClass.getAnnotation(Promocionable::class.java)

    fun esPromocionable(producto: Producto): Boolean = anotacionDe(producto) != null

    fun descuentoDe(producto: Producto): Int = anotacionDe(producto)?.descuentoPorcentaje ?: 0
}
