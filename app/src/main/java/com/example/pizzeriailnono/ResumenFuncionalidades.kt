package com.example.pizzeriailnono

import java.time.LocalDate

// Resumen en consola de las funcionalidades avanzadas (operadores, anotaciones, desestructuración, igualdad, reflection).
// Usa datos de demostración fijos y el menú real de la pizzería.
object ResumenFuncionalidades {

    fun imprimir(menu: List<Pizza>, repartidor: Repartidor) {
        println("\n================ RESUMEN DE FUNCIONALIDADES (datos de demostración) ================")

        val margarita = menu.first { !Promociones.esPromocionable(it) }
        val especial = menu.first { Promociones.esPromocionable(it) }
        val cliente = Cliente("Camila Rojas", "Av. Providencia 1234", formatearTelefono("912345678")!!, "camila@correo.cl")

        validaciones()
        combinarPedidos(cliente, margarita, especial, repartidor)
        desestructuracion(cliente, margarita, especial, repartidor)
        promocionables(menu)
        igualdad(cliente, menu)
        reflexion()

        println("====================================================================================")
    }

    private fun validaciones() {
        println("\n--- Validaciones (Regex y rangos) ---")
        for (correo in listOf("nombre@dominio.com", "nombre@dominio", "nombre dominio.com")) {
            println("Correo '$correo': ${if (Cliente.validarCorreo(correo) != null) "válido" else "INVÁLIDO"}")
        }
        for (tel in listOf("912345678", "+56 9 1234 5678", "56912345678", "12345")) {
            println("Teléfono '$tel' -> ${formatearTelefono(tel) ?: "INVÁLIDO"}")
        }
        for (cantidad in listOf(0, 1, 100, 101)) {
            println("Cantidad $cantidad ${if (cantidad in Pedido.RANGO_CANTIDAD) "dentro" else "FUERA"} del rango ${Pedido.RANGO_CANTIDAD}")
        }
        val dentro = LocalDate.of(2026, 11, 15)
        val fuera = LocalDate.of(2026, 12, 15)
        println("Período de promoción ${Promociones.PERIODO}: $dentro -> ${dentro in Promociones.PERIODO}, $fuera -> ${fuera in Promociones.PERIODO}")
    }

    private fun combinarPedidos(cliente: Cliente, margarita: Pizza, especial: Pizza, repartidor: Repartidor) {
        println("\n--- Pedidos combinados con el operador + ---")
        val fecha = LocalDate.of(2026, 11, 15)   // dentro del período de promoción
        val a = Pedido(cliente, listOf(ItemPedido(margarita, 2)), repartidor, "sin cebolla", null, fecha)
        val b = Pedido(cliente, listOf(ItemPedido(margarita, 1), ItemPedido(especial, 1)), repartidor, null, null, fecha)
        val combinado = a + b

        describir("Pedido A", a)
        describir("Pedido B", b)
        describir("A + B   ", combinado)
        println("Suma de totales por separado: ${(a.total + b.total).comoMonto()} | combinado (con descuentos recalculados): ${combinado.total.comoMonto()}")

        val fueraDePeriodo = Pedido(cliente, listOf(ItemPedido(especial, 1)), repartidor, null, null, LocalDate.of(2026, 12, 15))
        println("Mismo producto fuera del período: promoción ${fueraDePeriodo.descuentoPromocion.comoMonto()} " +
                "(dentro: ${Pedido(cliente, listOf(ItemPedido(especial, 1)), repartidor, null, null, fecha).descuentoPromocion.comoMonto()})")

        val otroCliente = Cliente("Luis Pérez", "Av. 2", "+56 (987) 654-321", "luis@correo.cl")
        try {
            a + Pedido(otroCliente, listOf(ItemPedido(margarita, 1)), repartidor)
        } catch (e: IllegalArgumentException) {
            println("Combinar pedidos de clientes distintos se rechaza: ${e.message}")
        }
    }

    private fun describir(titulo: String, pedido: Pedido) {
        println("$titulo: ${pedido.detallePizzas} | subtotal ${pedido.subtotal.comoMonto()}, " +
                "desc. cantidad -${pedido.descuentoVolumen.comoMonto()}, promoción -${pedido.descuentoPromocion.comoMonto()}, " +
                "IVA ${pedido.iva.comoMonto()} => total ${pedido.total.comoMonto()}")
    }

    private fun desestructuracion(cliente: Cliente, margarita: Pizza, especial: Pizza, repartidor: Repartidor) {
        println("\n--- Desestructuración ---")
        val (nombre, correo, telefono) = cliente
        println("Cliente -> nombre: $nombre | correo: $correo | teléfono: $telefono")

        val pedido = Pedido(cliente, listOf(ItemPedido(margarita, 2), ItemPedido(especial, 1)), repartidor)
        val (dueño, productos, total) = pedido
        println("Pedido  -> cliente: ${dueño.nombre} | productos: ${productos.joinToString { "${it.cantidad} x ${it.pizza.nombre}" }} | total: ${total.comoMonto()}")
    }

    private fun promocionables(menu: List<Pizza>) {
        println("\n--- Productos promocionables (detectados por la anotación @Promocionable) ---")
        for (p in menu) {
            val estado = if (Promociones.esPromocionable(p)) "SÍ  -> ${Promociones.descuentoDe(p)}% de descuento" else "no"
            println("- ${p.nombre} [${p.javaClass.simpleName}]: $estado")
        }
        println("Total: ${menu.count { Promociones.esPromocionable(it) }} de ${menu.size} productos son promocionables.")
    }

    private fun igualdad(cliente: Cliente, menu: List<Pizza>) {
        println("\n--- Igualdad de objetos (equals / hashCode / ==) ---")
        val mismo = Cliente("  camila ROJAS ", "Otra dirección 55", "+56 (900) 000-000", "CAMILA@correo.cl")
        val otro = Cliente("Camila Rojas", "Av. Providencia 1234", "+56 (912) 345-678", "otra@correo.cl")
        println("Cliente == mismo nombre y correo (otra dirección/mayúsculas): ${cliente == mismo} | hashCode igual: ${cliente.hashCode() == mismo.hashCode()}")
        println("Cliente == mismo nombre, distinto correo: ${cliente == otro}")

        val registroClientes = Registro<Cliente>()
        val resultados = listOf(cliente, mismo, otro).map { registroClientes.registrar(it) }
        println("Registro de clientes (intentos $resultados): quedan ${registroClientes.cantidad} de 3 -> duplicado evitado")

        val margarita = menu.first()
        val cocaCola = Producto("Coca-Cola", 2000, Categoria.BEBIDA)
        println("Producto ${margarita.nombre} == Producto('${margarita.nombre}', otro precio, PIZZA): ${margarita == Producto(margarita.nombre.uppercase(), 1, Categoria.PIZZA)}")
        println("Producto ${margarita.nombre} == Producto('${margarita.nombre}', BEBIDA): ${margarita == Producto(margarita.nombre, 1, Categoria.BEBIDA)}")
        println("Producto Coca-Cola == Coca-Cola (otro precio): ${cocaCola == Producto("Coca-Cola", 2500, Categoria.BEBIDA)}")

        val registroProductos = Registro<Producto>()
        val intentos = listOf(margarita, cocaCola, Producto("coca-cola", 1500, Categoria.BEBIDA), Producto("Mozzarella", 500, Categoria.INGREDIENTE))
            .map { registroProductos.registrar(it) }
        println("Registro de productos (intentos $intentos): quedan ${registroProductos.cantidad} de ${intentos.size} -> duplicado evitado")
    }

    private fun reflexion() {
        println("\n--- Reflection: propiedades y métodos de las clases clave ---")
        Reflexion.listar(Cliente::class.java)
        Reflexion.listar(Producto::class.java)
        Reflexion.listar(Pedido::class.java)
    }
}
