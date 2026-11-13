package com.example.pizzeriailnono

class Cliente (
    val nombre: String,
    val direccion: String,
    val telefono: String,
    val correo: String

){
    operator fun component1(): String = nombre
    operator fun component2(): String = correo
    operator fun component3(): String = telefono
    operator fun component4(): String = direccion

    private val clave: Pair<String, String> get() = nombre.normalizado() to correo.trim().lowercase()

    override fun equals(other: Any?): Boolean {
        if(this === other) return true
        if(other !is Cliente) return false
        return clave == other.clave
    }

    override fun hashCode(): Int = clave.hashCode()

    override fun toString(): String = "Cliente(nombre=$nombre, correo=$correo, telefono=$telefono, direccion=$direccion)"

    companion object {
        // Formato nombre@dominio.com: texto, un @, dominio y un punto con al menos 2 letras al final
        private val regexCorreo = Regex("""^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\.[A-Za-z0-9-]+)*\.[A-Za-z]{2,}$""")

        // Devuelve el teléfono en formato "+XX (XXX) XXX-XXXX", o null si es inválido.
        fun validarTelefono(entrada: String): String? = formatearTelefono(entrada)

        // Devuelve el correo sin espacios, o null si el formato es inválido.
        fun validarCorreo(entrada: String): String? =
            entrada.trim().takeIf { regexCorreo.matches(it) }
    }

}