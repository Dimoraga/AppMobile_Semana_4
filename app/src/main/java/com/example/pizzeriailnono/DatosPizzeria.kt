package com.example.pizzeriailnono

// Datos iniciales de la pizzería, compartidos por la versión de consola (Pizzeria) y la app Android.
object DatosPizzeria {

    // Los productos se registran sin duplicados (mismo nombre y categoría = mismo producto)
    fun crearProductos(): Registro<Producto> = Registro<Producto>().also { registro ->
        listOf(
            Pizza("Margarita", 10000, mapOf("Masa" to 1, "Salsa de tomate" to 80, "Mozzarella" to 120, "Tomate" to 50)),
            Pizza("Pepperoni", 12000, mapOf("Masa" to 1, "Salsa de tomate" to 80, "Mozzarella" to 120, "Pepperoni" to 60)),
            PizzaEspecial("Cuatro Quesos", 15000, mapOf("Masa" to 1, "Mozzarella" to 100, "Gorgonzola" to 50, "Parmesano" to 40, "Provolone" to 50)),
            PizzaEspecial("Napolitana", 11000, mapOf("Masa" to 1, "Salsa de tomate" to 80, "Mozzarella" to 100, "Tomate" to 60, "Aceitunas" to 30)),
            // 2 Margarita + 1 Pepperoni a precio rebajado
            Combo("Combo Familiar", 28000, mapOf("Masa" to 3, "Salsa de tomate" to 240, "Mozzarella" to 360, "Tomate" to 100, "Pepperoni" to 60))
        ).forEach { registro.registrar(it) }
    }

    fun crearRepartidores(): List<Repartidor> = listOf(
        Repartidor("Marcelo Guerra", listOf("La Florida", "Puente Alto", "Macul", "Peñalolén", "La Granja", "San Ramón")),
        Repartidor("Gonzalo Espinosa", listOf("La Reina", "Providencia", "Las Condes", "Vitacura", "Lo Barnechea")),
        Repartidor("María José Valenzuela", listOf("Santiago", "Pedro Aguirre Cerda", "San Miguel", "Recoleta", "Independencia", "Huechuraba", "Estación Central")),
        Repartidor("Bryan Jiménez", listOf("Maipú", "Cerrillos", "Lo Prado", "Lo Espejo")),
        Repartidor("Pablo Soto", listOf("San Bernardo", "El Bosque", "La Cisterna", "La Pintana")),
        Repartidor("Ximena Salvatierra", listOf("Quilicura", "Pudahuel", "Quinta Normal", "Cerro Navia"))
    )

    fun crearInventario(): Inventario = Inventario(
        mutableMapOf(
            "Masa" to 20, "Salsa de tomate" to 2000, "Mozzarella" to 3000,
            "Pepperoni" to 600, "Tomate" to 1000, "Gorgonzola" to 500,
            "Parmesano" to 500, "Provolone" to 500, "Aceitunas" to 300
        )
    )
}
