package com.example.pizzeriailnono

import java.text.Normalizer

// Minúsculas, sin espacios en los extremos y sin tildes: "  Sí " -> "si". Sirve para comparar textos.
fun String.normalizado(): String =
    Normalizer.normalize(trim(), Normalizer.Form.NFD)
        .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
        .lowercase()

// Formatea un entero como monto en pesos con separador de miles: 12000 -> "$12.000"
fun Int.comoMonto(): String {
    val digitos = kotlin.math.abs(this).toString().reversed().chunked(3).joinToString(".").reversed()
    return (if (this < 0) "-$" else "$") + digitos
}

// Acepta 9 dígitos, con o sin código de país (56). Devuelve "+56 (XXX) XXX-XXX" o null si es inválido.
fun formatearTelefono(entrada: String): String? {
    var digitos = entrada.filter { it.isDigit() }
    if (digitos.length == 11 && digitos.startsWith("56")) digitos = digitos.removePrefix("56")
    if (digitos.length != 9) return null
    return "+56 (${digitos.substring(0, 3)}) ${digitos.substring(3, 6)}-${digitos.substring(6)}"
}

// Distancia de Levenshtein: cuántas ediciones separan dos textos (para sugerir comunas mal escritas)
fun distanciaEdicion(a: String, b: String): Int {
    var anterior = IntArray(b.length + 1) { it }
    for (i in 1..a.length) {
        val actual = IntArray(b.length + 1)
        actual[0] = i
        for (j in 1..b.length) {
            val costo = if (a[i - 1] == b[j - 1]) 0 else 1
            actual[j] = minOf(actual[j - 1] + 1, anterior[j] + 1, anterior[j - 1] + costo)
        }
        anterior = actual
    }
    return anterior[b.length]
}
