package com.example.pizzeriailnono

enum class Categoria { PIZZA, BEBIDA, INGREDIENTE }

// Dos productos son el mismo si tienen igual nombre (sin importar mayúsculas ni tildes) y categoría.
open class Producto(val nombre: String, val precio: Int, val categoria: Categoria) {

    init {
        require(nombre.isNotBlank()) { "El producto necesita un nombre." }
        require(precio >= 0) { "Precio no válido para '$nombre': $precio." }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Producto) return false
        return nombre.normalizado() == other.nombre.normalizado() && categoria == other.categoria
    }

    override fun hashCode(): Int = 31 * nombre.normalizado().hashCode() + categoria.hashCode()

    override fun toString(): String = "$nombre (${precio.comoMonto()}, $categoria)"
}

// Una pizza lleva su receta: ingrediente -> cantidad por pizza (g, o unidades en el caso de la masa)
open class Pizza(nombre: String, precio: Int, val receta: Map<String, Int>) :
    Producto(nombre, precio, Categoria.PIZZA)

@Promocionable
class PizzaEspecial(nombre: String, precio: Int, receta: Map<String, Int>) : Pizza(nombre, precio, receta)

@Promocionable
class Combo(nombre: String, precio: Int, receta: Map<String, Int>) : Pizza(nombre, precio, receta)
