package com.example.pizzeriailnono
// Registro sin duplicados: usa equals()/hashCode() de los elementos (Cliente: nombre y correo; Producto: nombre y categoría).
class Registro<T : Any>(private val actualizarDuplicados: Boolean = false) {
    private val elementos = LinkedHashMap<T, T>()

    val todos: List<T> get() = elementos.values.toList()
    val cantidad: Int get() = elementos.size

    // Devuelve true si es nuevo, false si ya existía un elemento igual.
    // Con actualizarDuplicados = true, un duplicado reemplaza los datos guardados (ej: dirección nueva de un cliente).
    fun registrar(elemento: T): Boolean {
        val existe = elemento in elementos
        if (!existe || actualizarDuplicados) elementos[elemento] = elemento
        return !existe
    }
}
