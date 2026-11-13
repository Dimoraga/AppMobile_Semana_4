package com.example.pizzeriailnono

// Se lanza cuando el cliente escribe "volver" (o 0) para regresar al paso anterior
class VolverAtras : Exception()

// Lectura y validación de datos ingresados por consola
object Consola {

    private val RESPUESTAS_SI = setOf("si", "s", "yes", "y")
    private val RESPUESTAS_NO = setOf("no", "n")

    // Lee una línea. "volver" o "0" lanzan VolverAtras (salvo que se desactive).
    private fun leer(permitirVolver: Boolean = true): String {
        val entrada = readln().trim()
        if (permitirVolver && (entrada == "0" || entrada.normalizado() == "volver")) throw VolverAtras()
        return entrada
    }

    fun leerTexto(mensaje: String): String {
        while (true) {
            println(mensaje)
            val texto = leer()
            if (texto.isNotBlank()) return texto
            println("Este campo no puede estar vacío.\n")
        }
    }

    // Lee un entero y valida su rango con try-catch. Devuelve null (tras explicar el error) si es inválido.
    fun leerEnteroEnRango(nombre: String, minimo: Int, maximo: Int): Int? {
        val entrada = leer(permitirVolver = minimo > 0)
        return try {
            val valor = entrada.toInt()
            require(valor in minimo..maximo) {
                when {
                    valor < 0 -> "$nombre no válida: $valor. No se permiten números negativos."
                    maximo == Int.MAX_VALUE -> "$nombre no válida: $valor. Debe ser mayor o igual a $minimo."
                    else -> "$nombre no válida: $valor. Elige un número del $minimo al $maximo."
                }
            }
            valor
        } catch (e: NumberFormatException) {
            println("'$entrada' no es un número entero válido. Ingresa solo dígitos, por ejemplo: $minimo.\n")
            null
        } catch (e: IllegalArgumentException) {
            println("${e.message}\n")
            null
        }
    }

    fun leerTelefono(): String {
        while (true) {
            println("Por favor, ingresa tu número de contacto (9 dígitos, ej: 912345678): ")
            val telefono = Cliente.validarTelefono(leer())
            if (telefono != null) return telefono
            println("Teléfono inválido. Debe tener 9 dígitos, con o sin código de país (+56), por ejemplo: 912345678 o +56 9 1234 5678.\n")
        }
    }

    fun leerCorreo(): String {
        while (true) {
            println("Por favor, ingresa tu correo electrónico (ej: nombre@dominio.com): ")
            val correo = Cliente.validarCorreo(leer())
            if (correo != null) return correo
            println("Correo inválido. Debe contener un @ y un punto (.) después del @, por ejemplo: nombre@dominio.com.\n")
        }
    }

    // Campo opcional: Enter lo deja en null
    fun leerNotas(): String? {
        println("¿Alguna nota u observación para tu pedido? (Enter para omitir)")
        return leer().takeIf { it.isNotEmpty() }
    }

    // Acepta SI/S/YES/Y y NO/N sin importar mayúsculas ni tildes ("sí", "Sí", "s"...)
    fun preguntarSiNo(pregunta: String, permitirVolver: Boolean = false): Boolean {
        while (true) {
            println("$pregunta Escribe SI o NO.")
            when (leer(permitirVolver).normalizado()) {
                in RESPUESTAS_SI -> return true
                in RESPUESTAS_NO -> return false
                else -> println("Responde SI o NO (también sirven S o N).")
            }
        }
    }
}
