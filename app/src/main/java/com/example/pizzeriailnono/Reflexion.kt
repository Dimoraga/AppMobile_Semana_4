package com.example.pizzeriailnono

import java.lang.reflect.Modifier

// Analiza dinámicamente una clase (Pedido, Producto, Cliente...) con la reflection de Java
object Reflexion {

    class Descripcion(val clase: String, val padre: String?, val propiedades: Map<String, String>, val metodos: List<String>)

    private fun esGetter(nombre: String, parametros: Int, retorno: Class<*>): Boolean =
        parametros == 0 && ((nombre.startsWith("get") && nombre.length > 3) ||
                (nombre.startsWith("is") && nombre.length > 2 && retorno == Boolean::class.javaPrimitiveType))

    private fun nombreDePropiedad(getter: String): String {
        val base = if (getter.startsWith("get")) getter.removePrefix("get") else getter
        return base.replaceFirstChar { it.lowercase() }
    }

    fun analizar(clase: Class<*>): Descripcion {
        val propiedades = sortedMapOf<String, String>()

        // Propiedades con campo propio (backing field)
        for (campo in clase.declaredFields) {
            if (campo.isSynthetic || Modifier.isStatic(campo.modifiers)) continue
            propiedades[campo.name] = campo.type.simpleName
        }

        // Propiedades calculadas (solo tienen getter) y métodos propiamente tales
        val metodos = mutableListOf<String>()
        for (m in clase.declaredMethods.sortedBy { it.name }) {
            if (m.isSynthetic || m.name.contains('$')) continue
            if (esGetter(m.name, m.parameterCount, m.returnType)) {
                propiedades.putIfAbsent(nombreDePropiedad(m.name), m.returnType.simpleName)
            } else if (!m.name.startsWith("set") || m.parameterCount != 1) {
                val parametros = m.parameterTypes.joinToString(", ") { it.simpleName }
                metodos += "${m.name}($parametros): ${m.returnType.simpleName}"
            }
        }

        val padre = clase.superclass?.takeIf { it != Any::class.java }?.simpleName
        return Descripcion(clase.simpleName, padre, propiedades, metodos)
    }

    fun listar(clase: Class<*>) {
        val d = analizar(clase)
        println("Clase ${d.clase}" + (d.padre?.let { " (hereda de $it)" } ?: ""))
        println("  Propiedades (${d.propiedades.size}): " + d.propiedades.entries.joinToString(", ") { "${it.key}: ${it.value}" })
        println("  Métodos (${d.metodos.size}): " + d.metodos.joinToString(", "))
    }
}