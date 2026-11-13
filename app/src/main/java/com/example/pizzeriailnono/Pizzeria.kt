package com.example.pizzeriailnono

import java.time.LocalDate

class Pizzeria {

    private val productos = DatosPizzeria.crearProductos()

    private val menu: List<Pizza> = productos.todos.filterIsInstance<Pizza>()

    // Clientes registrados sin duplicados (mismo nombre y correo = mismo cliente); si repiten, se actualizan sus datos
    private val clientes = Registro<Cliente>(actualizarDuplicados = true)

    private val repartidores = DatosPizzeria.crearRepartidores()

    private val inventario = DatosPizzeria.crearInventario()

    // Todos los pedidos del día
    private val pedidosDelDia = mutableListOf<Pedido>()

    fun abrir() {
        println("::::::::::::::::::::::::::::::::::::::::::::")
        println(":::: Bienvenido a la Pizzaria Il Nono:::::::")
        println("::::::::::::::::::::::::::::::::::::::::::::")
        println("--- Tradición Italiana ---------------------")
        println("::::::::::::::::::::::::::::::::::::::::::::")

        while (true) {
            if (!inventario.hayStockParaAlguna(menu)) {
                println("Lo sentimos, nos quedamos sin ingredientes por hoy.")
                break
            }

            atenderCliente()

            if (!Consola.preguntarSiNo("¿Hay otro cliente por atender?")) break
        }

        actualizarEstados()
        Reporte.generar(pedidosDelDia, inventario)
        ResumenFuncionalidades.imprimir(menu, repartidores.first())

        println(":::: Cerrando la pizzería. ¡Hasta mañana! ::::")
    }

    // Los estados solo avanzan en orden: En preparación -> Listo -> En camino
    private fun actualizarEstados() {
        if (pedidosDelDia.isEmpty()) return

        println("\n--- Actualización de estados ---")
        for (pedido in pedidosDelDia) {
            while (true) {
                val siguiente = pedido.estado.siguiente() ?: break
                val avanzar = Consola.preguntarSiNo(
                    "Pedido de ${pedido.cliente.nombre} (${pedido.detallePizzas}) está '${pedido.estado.texto}'. ¿Avanzar a '${siguiente.texto}'?"
                )
                if (!avanzar) break

                pedido.avanzarEstado()
                notificar(pedido, "Tu pedido ahora está: ${siguiente.texto}.")
            }
        }
    }

    // Flujo por pasos: 0 datos, 1 comuna, 2 pizzas, 3 notas/notificaciones, 4 revisión y confirmación.
    // Escribir "volver" (o 0) retrocede un paso. El inventario solo se descuenta al confirmar.
    private fun atenderCliente() {
        println("\nTip: en cualquier momento escribe 'volver' (o 0) para regresar al paso anterior.\n")

        var cliente: Cliente? = null
        var repartidor: Repartidor? = null
        val items = mutableListOf<ItemPedido>()
        var notas: String? = null
        var quiereNotificaciones = false
        var paso = 0

        while (true) {
            try {
                when (paso) {
                    0 -> { cliente = pedirDatosCliente(); paso = 1 }
                    1 -> { repartidor = elegirRepartidor(); paso = 2 }
                    2 -> {
                        val elegidos = elegirItems(items.toList())
                        items.clear()
                        items.addAll(elegidos)
                        paso = 3
                    }
                    3 -> {
                        notas = Consola.leerNotas()
                        quiereNotificaciones = Consola.preguntarSiNo(
                            "¿Deseas recibir notificaciones del estado de tu pedido por correo?", permitirVolver = true
                        )
                        paso = 4
                    }
                    else -> {
                        val datos = cliente!!
                        val correo = if (quiereNotificaciones) datos.correo else null
                        val pedido = revisarPedido(datos, items, repartidor!!, notas, correo)
                        if (pedido == null) {
                            println("\nPedido cancelado. No se descontó ningún ingrediente.\n")
                            return
                        }
                        try {
                            inventario.descontar(pedido.items)
                        } catch (e: IllegalStateException) {
                            println("\nNo se pudo reservar el pedido: ${e.message}\nElige tus pizzas nuevamente.\n")
                            paso = 2
                            continue
                        } catch (e: IllegalArgumentException) {
                            println("\nNo se pudo reservar el pedido: ${e.message}\nElige tus pizzas nuevamente.\n")
                            paso = 2
                            continue
                        }
                        if (!clientes.registrar(datos)) println("\n${datos.nombre} ya estaba registrado: se actualizaron sus datos de contacto.")
                        pedidosDelDia.add(pedido)
                        finalizarPedido(pedido)
                        return
                    }
                }
            } catch (e: VolverAtras) {
                if (paso == 0) println("\nYa estás en el primer paso.\n") else paso--
            }
        }
    }

    private fun finalizarPedido(pedido: Pedido) {
        println("\n*******************************")
        println("PEDIDO CONFIRMADO: ")
        println(pedido.resumen())

        notificar(pedido, "Recibimos tu pedido de ${pedido.detallePizzas}. Total: ${pedido.total.comoMonto()}.")

        println("Te garantizamos que tu pedido llegará en menos de 40 minutos.")
        println("::::: MUCHAS GRACIAS POR SU COMPRA ::::::::")
        println(":::::::::::::::::::::::::::::::::::::::::::")
        println(":::::::::::::::::::::::::::::::::::::::::::")
        println("**** Es probable que recibas un mensaje para evaluar la calidad de la atención.******" +
                "**** Es importante que respondas para mejorar nuestra atención***********" +
                "**** GRACIAS POR PREFERIRNOS **********")
    }

    // Muestra el resumen con el total y deja confirmar, editar o cancelar. Devuelve null si se cancela.
    private fun revisarPedido(
        cliente: Cliente, items: MutableList<ItemPedido>, repartidor: Repartidor, notas: String?, correo: String?
    ): Pedido? {
        while (true) {
            val pedido = Pedido(cliente, items.toList(), repartidor, notas, correo)
            println("\n*******************************")
            println("RESUMEN DEL PEDIDO (aún no confirmado): ")
            println(pedido.resumen())
            println("*******************************")
            println("¿Qué deseas hacer?")
            println("1. Confirmar pedido")
            println("2. Agregar otra pizza")
            println("3. Quitar una pizza")
            println("4. Cambiar una cantidad")
            println("5. Cancelar pedido")

            val opcion = Consola.leerEnteroEnRango("Opción", 1, 5) ?: continue
            try {
                when (opcion) {
                    1 -> return pedido
                    2 -> {
                        val nuevos = elegirItems(items.toList())
                        items.clear()
                        items.addAll(nuevos)
                    }
                    3 -> quitarPizza(items)
                    4 -> cambiarCantidad(items)
                    5 -> if (Consola.preguntarSiNo("¿Seguro que deseas cancelar el pedido?", permitirVolver = true)) return null
                }
            } catch (e: VolverAtras) {
                // "volver" dentro de una edición: se regresa al menú de revisión
            }
        }
    }

    private fun quitarPizza(items: MutableList<ItemPedido>) {
        if (items.size == 1) {
            println("\nEl pedido tiene una sola pizza. Si no la quieres, cancela el pedido (opción 5).")
            return
        }
        val i = elegirLinea(items, "¿Cuál pizza quieres quitar?")
        println("Se quitó ${items[i].cantidad} x ${items[i].pizza.nombre}.")
        items.removeAt(i)
    }

    private fun cambiarCantidad(items: MutableList<ItemPedido>) {
        val i = if (items.size == 1) 0 else elegirLinea(items, "¿A cuál pizza quieres cambiarle la cantidad?")
        val pizza = items[i].pizza
        val nueva = leerCantidad()

        val propuesta = items.toMutableList().also { it[i] = ItemPedido(pizza, nueva) }
        val faltantes = inventario.faltantes(propuesta)
        if (faltantes.isNotEmpty()) {
            println("\nNo podemos cambiar la cantidad: hay ingredientes insuficientes.")
            mostrarFaltantes(faltantes)
            return
        }
        items[i] = propuesta[i]
    }

    // Lista las líneas del pedido y devuelve la posición elegida
    private fun elegirLinea(items: List<ItemPedido>, pregunta: String): Int {
        while (true) {
            println(pregunta)
            items.forEachIndexed { i, item -> println("${i + 1}. ${item.cantidad} x ${item.pizza.nombre} (${item.subtotal.comoMonto()})") }
            val n = Consola.leerEnteroEnRango("Opción", 1, items.size) ?: continue
            return n - 1
        }
    }

    private fun pedirDatosCliente(): Cliente {
        while (true) {
            val nombre = Consola.leerTexto("Por favor, ingresa tu nombre: ")
            val direccion = Consola.leerTexto("$nombre, por favor, ingresa tu dirección: ")
            val telefono = Consola.leerTelefono()
            val correo = Consola.leerCorreo()

            val cliente = Cliente(nombre, direccion, telefono, correo)
            println("Confirmación.\n" +
                    "Los datos ingresados son:\n" +
                    "Nombre: $nombre\n" +
                    "Dirección: $direccion\n" +
                    "Teléfono: $telefono\n" +
                    "email: $correo\n")

            if (Consola.preguntarSiNo("¿Son correctos los datos ingresados?", permitirVolver = true)) return cliente
            println("Volvamos a ingresar los datos nuevamente\n")
        }
    }

    // Arma la lista de pizzas del pedido a partir de las ya elegidas (si las hay).
    // El stock se valida con todo lo ya elegido más la nueva pizza; el inventario aún no se toca.
    private fun elegirItems(inicial: List<ItemPedido> = emptyList()): List<ItemPedido> {
        val items = inicial.toMutableList()
        while (true) {
            if (menu.none { inventario.unidadesPosibles(it, items) > 0 }) {
                println("\nYa no queda stock para agregar más pizzas.")
                return items
            }

            val pizza = leerPizza(items)
            val cantidad = try {
                leerCantidad()
            } catch (e: VolverAtras) {
                continue   // vuelve a elegir pizza
            }

            val propuestos = conPizza(items, ItemPedido(pizza, cantidad))
            if (propuestos.any { it.cantidad !in Pedido.RANGO_CANTIDAD }) {
                println("\nUn pedido admite entre ${Pedido.RANGO_CANTIDAD.first} y ${Pedido.RANGO_CANTIDAD.last} unidades de la misma pizza. Ingresa una cantidad menor.\n")
                continue
            }
            val faltantes = inventario.faltantes(propuestos)
            if (faltantes.isNotEmpty()) {
                println("\nNo podemos agregar esa pizza: hay ingredientes insuficientes.")
                mostrarFaltantes(faltantes)
                println("Elige otra pizza o una cantidad menor.\n")
                if (items.isNotEmpty() && Consola.preguntarSiNo(
                        "¿Deseas continuar solo con las pizzas ya elegidas (${items.joinToString { "${it.cantidad} x ${it.pizza.nombre}" }})?",
                        permitirVolver = true
                    )
                ) {
                    return items
                }
                continue
            }

            items.clear()
            items.addAll(propuestos)
            if (!Consola.preguntarSiNo("¿Deseas agregar otra pizza al pedido?", permitirVolver = true)) return items
        }
    }

    // Si la pizza ya estaba en la lista, suma la cantidad en la misma línea
    private fun conPizza(items: List<ItemPedido>, nuevo: ItemPedido): List<ItemPedido> {
        val existente = items.firstOrNull { it.pizza == nuevo.pizza } ?: return items + nuevo
        return items.map { if (it === existente) ItemPedido(it.pizza, it.cantidad + nuevo.cantidad) else it }
    }

    private fun mostrarFaltantes(faltantes: List<Faltante>) {
        for (f in faltantes) {
            val u = inventario.unidadDe(f.ingrediente)
            println("- ${f.ingrediente}: necesitas ${f.necesario} $u, hay ${f.disponible} $u (faltan ${f.diferencia} $u)")
        }
    }

    // El cliente elige su comuna por número o por nombre; si se equivoca al escribir, se le sugiere una
    private fun elegirRepartidor(): Repartidor {
        val comunasDisponibles = repartidores.flatMap { it.comunas }.sorted()
        while (true) {
            println("Solo repartimos en Santiago. Comunas con reparto:")
            mostrarComunas(comunasDisponibles)

            val entrada = Consola.leerTexto("Escribe el número o el nombre de tu comuna:")
            val comuna = resolverComuna(entrada, comunasDisponibles) ?: continue
            val repartidor = repartidores.first { it.atiende(comuna) }
            println("Tu pedido será entregado por ${repartidor.nombre}.\n")
            return repartidor
        }
    }

    private fun mostrarComunas(comunas: List<String>) {
        comunas.chunked(3).forEachIndexed { fila, grupo ->
            println(grupo.mapIndexed { j, c -> "${fila * 3 + j + 1}. $c".padEnd(28) }.joinToString("").trimEnd())
        }
    }

    // Devuelve la comuna elegida, o null (explicando por qué) si hay que volver a preguntar
    private fun resolverComuna(entrada: String, comunas: List<String>): String? {
        entrada.toIntOrNull()?.let { n ->
            if (n in 1..comunas.size) return comunas[n - 1]
            println("El número $n no está en la lista (del 1 al ${comunas.size}).\n")
            return null
        }

        val buscada = entrada.normalizado()
        comunas.firstOrNull { it.normalizado() == buscada }?.let { return it }

        // Coincidencias parciales ("san" -> San Ramón, San Miguel...) o, si no hay, nombres parecidos (errores de tipeo)
        var sugerencias = if (buscada.length >= 3) {
            comunas.filter { val c = it.normalizado(); c.contains(buscada) || buscada.contains(c) }
        } else emptyList()
        if (sugerencias.isEmpty()) {
            sugerencias = comunas.filter { distanciaEdicion(it.normalizado(), buscada) <= 2 }
        }

        when {
            sugerencias.size == 1 -> {
                if (Consola.preguntarSiNo("No encontré '$entrada'. ¿Quisiste decir ${sugerencias[0]}?", permitirVolver = true)) {
                    return sugerencias[0]
                }
            }
            sugerencias.size > 1 -> println("No encontré '$entrada'. ¿Quisiste decir: ${sugerencias.joinToString(", ")}?")
            else -> println("No repartimos en '$entrada'. Revisa la lista e inténtalo nuevamente.")
        }
        println()
        return null
    }

    // Muestra cada pizza con sus ingredientes y cuántas unidades se pueden pedir aún (considerando lo ya elegido)
    private fun leerPizza(elegidas: List<ItemPedido>): Pizza {
        while (true) {
            println("\nSeleccione la pizza que desee: ")
            menu.forEachIndexed { i, p ->
                val disponibles = inventario.unidadesPosibles(p, elegidas)
                val estado = if (disponibles == 0) "AGOTADA" else "Disponibles: hasta $disponibles"
                println("${i + 1}. ${p.nombre} (${p.precio.comoMonto()})")
                println("     Ingredientes: ${p.receta.keys.joinToString(", ")}")
                println("     $estado")
                if (Promociones.esPromocionable(p) && Promociones.enPeriodo(LocalDate.now())) {
                    println("     Promoción vigente: ${Promociones.descuentoDe(p)}% de descuento (hasta el ${Promociones.PERIODO.endInclusive})")
                }
            }
            println(mensajeDescuento(elegidas.sumOf { it.cantidad }))

            val indice = Consola.leerEnteroEnRango("Opción", 1, menu.size) ?: continue
            val pizza = menu[indice - 1]
            if (inventario.unidadesPosibles(pizza, elegidas) == 0) {
                println("Lo sentimos, la ${pizza.nombre} está agotada. Elige otra.")
                continue
            }
            return pizza
        }
    }

    private fun mensajeDescuento(totalPizzas: Int): String {
        val minimo = Pedido.MAX_PIZZAS_SIN_DESCUENTO + 1
        return when {
            totalPizzas == 0 -> "Promoción: lleva $minimo o más pizzas y obtén ${Pedido.PORCENTAJE_DESCUENTO}% de descuento."
            totalPizzas < minimo -> "Llevas $totalPizzas pizza(s). Agrega ${minimo - totalPizzas} más y obtienes ${Pedido.PORCENTAJE_DESCUENTO}% de descuento."
            else -> "¡Ya tienes el ${Pedido.PORCENTAJE_DESCUENTO}% de descuento por llevar $totalPizzas pizzas!"
        }
    }

    private fun leerCantidad(): Int {
        while (true) {
            println("¿Cuántas pizzas quiere? (entre ${Pedido.RANGO_CANTIDAD.first} y ${Pedido.RANGO_CANTIDAD.last})")
            val cantidad = Consola.leerEnteroEnRango("Cantidad", Pedido.RANGO_CANTIDAD.first, Pedido.RANGO_CANTIDAD.last)
            if (cantidad != null) return cantidad
        }
    }

    // Solo notifica si el cliente dejó un correo para notificaciones (let se ejecuta únicamente si no es null)
    private fun notificar(pedido: Pedido, mensaje: String) {
        pedido.correoNotificaciones?.let { correo ->
            println("[Notificación enviada a $correo] $mensaje")
        }
    }
}
