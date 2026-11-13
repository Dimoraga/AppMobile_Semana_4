package com.example.pizzeriailnono

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

enum class Pantalla { BIENVENIDA, DATOS, MENU, RESUMEN, CONFIRMADO, PEDIDOS }

// Estado y reglas de la app. Reutiliza el mismo dominio que la versión de consola (Pizzeria).
class PizzeriaViewModel : ViewModel() {

    private val productos = DatosPizzeria.crearProductos()
    val menu: List<Pizza> = productos.todos.filterIsInstance<Pizza>()
    private val repartidores = DatosPizzeria.crearRepartidores()
    val inventario = DatosPizzeria.crearInventario()
    private val clientes = Registro<Cliente>(actualizarDuplicados = true)

    val comunas: List<String> = repartidores.flatMap { it.comunas }.sorted()
    val pedidosDelDia = mutableStateListOf<Pedido>()

    var pantalla by mutableStateOf(Pantalla.BIENVENIDA)
        private set

    // Sube cada vez que cambia algo que Compose no observa solo (estado de un pedido, inventario)
    var cambios by mutableIntStateOf(0)
        private set

    // --- Formulario del cliente ---
    var nombre by mutableStateOf("")
    var direccion by mutableStateOf("")
    var telefono by mutableStateOf("")
    var correo by mutableStateOf("")
    var comuna by mutableStateOf<String?>(null)
    var notas by mutableStateOf("")
    var quiereNotificaciones by mutableStateOf(false)
    var errorDatos by mutableStateOf<String?>(null)
        private set

    // --- Pedido en armado ---
    private val cantidades = mutableStateMapOf<Pizza, Int>()
    var errorMenu by mutableStateOf<String?>(null)
        private set
    private var cliente: Cliente? = null
    private var repartidor: Repartidor? = null

    var ultimoPedido by mutableStateOf<Pedido?>(null)
        private set
    var mensajeNotificacion by mutableStateOf<String?>(null)
        private set

    val hayStock: Boolean get() = cambios >= 0 && inventario.hayStockParaAlguna(menu)

    // ---------- Navegación ----------
    fun irA(destino: Pantalla) {
        pantalla = destino
    }

    fun iniciarPedido() {
        errorDatos = null
        pantalla = Pantalla.DATOS
    }

    fun volver() {
        pantalla = when (pantalla) {
            Pantalla.MENU -> Pantalla.DATOS
            Pantalla.RESUMEN -> Pantalla.MENU
            else -> Pantalla.BIENVENIDA
        }
    }

    // ---------- Paso 1: datos del cliente ----------
    fun continuarDatos() {
        val telefonoValido = Cliente.validarTelefono(telefono)
        val correoValido = Cliente.validarCorreo(correo)
        val comunaElegida = comuna
        errorDatos = when {
            nombre.isBlank() -> "Ingresa tu nombre."
            direccion.isBlank() -> "Ingresa tu dirección."
            telefonoValido == null -> "Teléfono inválido. Debe tener 9 dígitos, por ejemplo: 912345678."
            correoValido == null -> "Correo inválido. Ejemplo: nombre@dominio.com."
            comunaElegida == null -> "Elige tu comuna de reparto."
            else -> null
        }
        if (errorDatos != null) return

        cliente = Cliente(nombre.trim(), direccion.trim(), telefonoValido!!, correoValido!!)
        repartidor = repartidores.first { it.atiende(comunaElegida!!) }
        errorMenu = null
        pantalla = Pantalla.MENU
    }

    val nombreRepartidor: String? get() = repartidor?.nombre

    // ---------- Paso 2: elegir pizzas ----------
    fun cantidadDe(pizza: Pizza): Int = cantidades[pizza] ?: 0

    private fun items(): List<ItemPedido> =
        cantidades.filterValues { it > 0 }.map { (pizza, cantidad) -> ItemPedido(pizza, cantidad) }

    val totalPizzas: Int get() = cantidades.values.sum()

    // Cuántas unidades más de esta pizza permite el stock, considerando lo ya elegido
    fun disponibles(pizza: Pizza): Int = cambios.let { inventario.unidadesPosibles(pizza, items()) }

    fun puedeAgregar(pizza: Pizza): Boolean =
        cantidadDe(pizza) < Pedido.RANGO_CANTIDAD.last && disponibles(pizza) > 0

    fun agregar(pizza: Pizza) {
        if (!puedeAgregar(pizza)) return
        cantidades[pizza] = cantidadDe(pizza) + 1
        errorMenu = null
    }

    fun quitar(pizza: Pizza) {
        val nueva = cantidadDe(pizza) - 1
        if (nueva <= 0) cantidades.remove(pizza) else cantidades[pizza] = nueva
    }

    fun mensajeDescuento(): String {
        val minimo = Pedido.MAX_PIZZAS_SIN_DESCUENTO + 1
        val total = totalPizzas
        return when {
            total == 0 -> "Promoción: lleva $minimo o más pizzas y obtén ${Pedido.PORCENTAJE_DESCUENTO}% de descuento."
            total < minimo -> "Llevas $total pizza(s). Agrega ${minimo - total} más y obtienes ${Pedido.PORCENTAJE_DESCUENTO}% de descuento."
            else -> "¡Ya tienes el ${Pedido.PORCENTAJE_DESCUENTO}% de descuento por llevar $total pizzas!"
        }
    }

    fun irAResumen() {
        if (items().isEmpty()) {
            errorMenu = "Elige al menos una pizza para continuar."
            return
        }
        errorMenu = null
        pantalla = Pantalla.RESUMEN
    }

    // ---------- Paso 3: revisar y confirmar ----------
    // Pedido aún no confirmado: no toca el inventario
    fun pedidoBorrador(): Pedido? {
        val datos = cliente ?: return null
        val entrega = repartidor ?: return null
        val itemsActuales = items()
        if (itemsActuales.isEmpty()) return null
        return Pedido(
            datos, itemsActuales, entrega,
            notas.trim().ifEmpty { null },
            if (quiereNotificaciones) datos.correo else null
        )
    }

    fun confirmar() {
        val pedido = pedidoBorrador() ?: return
        try {
            inventario.descontar(pedido.items)
        } catch (e: IllegalStateException) {
            errorMenu = "No se pudo reservar el pedido: ${e.message}"
            pantalla = Pantalla.MENU
            return
        } catch (e: IllegalArgumentException) {
            errorMenu = "No se pudo reservar el pedido: ${e.message}"
            pantalla = Pantalla.MENU
            return
        }
        clientes.registrar(pedido.cliente)
        pedidosDelDia.add(pedido)
        ultimoPedido = pedido
        mensajeNotificacion = pedido.correoNotificaciones?.let {
            "[Notificación enviada a $it] Recibimos tu pedido de ${pedido.detallePizzas}. Total: ${pedido.total.comoMonto()}."
        }
        limpiarBorrador()
        cambios++
        pantalla = Pantalla.CONFIRMADO
    }

    fun cancelarPedido() {
        limpiarBorrador()
        pantalla = Pantalla.BIENVENIDA
    }

    private fun limpiarBorrador() {
        cantidades.clear()
        nombre = ""; direccion = ""; telefono = ""; correo = ""; notas = ""
        comuna = null
        quiereNotificaciones = false
        cliente = null
        repartidor = null
        errorDatos = null
        errorMenu = null
    }

    // ---------- Pedidos del día ----------
    fun avanzarEstado(pedido: Pedido) {
        if (pedido.estado.siguiente() == null) return
        pedido.avanzarEstado()
        mensajeNotificacion = pedido.correoNotificaciones?.let {
            "[Notificación enviada a $it] Tu pedido ahora está: ${pedido.estado.texto}."
        }
        cambios++
    }
}
