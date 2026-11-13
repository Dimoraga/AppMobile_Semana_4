package com.example.pizzeriailnono

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pizzeriailnono.ui.theme.PizzeriaIlNonoTheme
import java.time.LocalDate

@Composable
fun PizzeriaApp(vm: PizzeriaViewModel) {
    // El botón "atrás" del teléfono retrocede un paso en lugar de cerrar la app
    BackHandler(enabled = vm.pantalla != Pantalla.BIENVENIDA) { vm.volver() }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        val modifier = Modifier.padding(innerPadding)
        when (vm.pantalla) {
            Pantalla.BIENVENIDA -> BienvenidaScreen(
                modifier = modifier,
                hayStock = vm.hayStock,
                pedidosHoy = vm.pedidosDelDia.size,
                onHacerPedido = vm::iniciarPedido,
                onVerPedidos = { vm.irA(Pantalla.PEDIDOS) }
            )
            Pantalla.DATOS -> DatosScreen(vm, modifier)
            Pantalla.MENU -> MenuScreen(vm, modifier)
            Pantalla.RESUMEN -> ResumenScreen(vm, modifier)
            Pantalla.CONFIRMADO -> ConfirmadoScreen(vm, modifier)
            Pantalla.PEDIDOS -> PedidosScreen(vm, modifier)
        }
    }
}

@Composable
private fun Titulo(texto: String) {
    Text(texto, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
}

@Composable
private fun Error(texto: String?) {
    if (texto != null) Text(texto, color = MaterialTheme.colorScheme.error)
}

// ---------------------------------------------------------------- Bienvenida
@Composable
fun BienvenidaScreen(
    modifier: Modifier = Modifier,
    hayStock: Boolean = true,
    pedidosHoy: Int = 0,
    onHacerPedido: () -> Unit = {},
    onVerPedidos: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.bienvenida_titulo),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.bienvenida_subtitulo),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onHacerPedido, enabled = hayStock) {
            Text(stringResource(R.string.ver_menu))
        }
        if (!hayStock) {
            Spacer(Modifier.height(8.dp))
            Text("Lo sentimos, nos quedamos sin ingredientes por hoy.", color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onVerPedidos) { Text("Pedidos del día ($pedidosHoy)") }
    }
}

// ---------------------------------------------------------------- Paso 1: datos
@Composable
private fun DatosScreen(vm: PizzeriaViewModel, modifier: Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Titulo("Tus datos")
        Text("Paso 1 de 3", style = MaterialTheme.typography.labelLarge)

        OutlinedTextField(
            value = vm.nombre, onValueChange = { vm.nombre = it },
            label = { Text("Nombre") }, singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = vm.direccion, onValueChange = { vm.direccion = it },
            label = { Text("Dirección") }, singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = vm.telefono, onValueChange = { vm.telefono = it },
            label = { Text("Teléfono (9 dígitos)") }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = vm.correo, onValueChange = { vm.correo = it },
            label = { Text("Correo electrónico") }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        Text("Solo repartimos en Santiago. Elige tu comuna:", style = MaterialTheme.typography.bodyMedium)
        SelectorComuna(vm)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = vm.quiereNotificaciones, onCheckedChange = { vm.quiereNotificaciones = it })
            Text("Quiero recibir notificaciones del estado de mi pedido por correo")
        }
        OutlinedTextField(
            value = vm.notas, onValueChange = { vm.notas = it },
            label = { Text("Notas para tu pedido (opcional)") }, modifier = Modifier.fillMaxWidth()
        )

        Error(vm.errorDatos)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = vm::volver) { Text("Volver") }
            Button(onClick = vm::continuarDatos) { Text("Continuar") }
        }
    }
}

@Composable
private fun SelectorComuna(vm: PizzeriaViewModel) {
    var abierto by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { abierto = true }, modifier = Modifier.fillMaxWidth()) {
            Text(vm.comuna ?: "Seleccionar comuna")
        }
        DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
            vm.comunas.forEach { comuna ->
                DropdownMenuItem(
                    text = { Text(comuna) },
                    onClick = { vm.comuna = comuna; abierto = false }
                )
            }
        }
    }
}

// ---------------------------------------------------------------- Paso 2: menú
@Composable
private fun MenuScreen(vm: PizzeriaViewModel, modifier: Modifier) {
    Column(modifier = modifier.fillMaxSize()) {
        Column(Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Titulo("Elige tus pizzas")
            Text("Paso 2 de 3 · Entrega: ${vm.nombreRepartidor ?: "-"}", style = MaterialTheme.typography.labelLarge)
            Text(vm.mensajeDescuento(), color = MaterialTheme.colorScheme.primary)
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(vm.menu) { pizza -> TarjetaPizza(vm, pizza) }
        }

        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Error(vm.errorMenu)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = vm::volver) { Text("Volver") }
                Button(onClick = vm::irAResumen) { Text("Ver resumen (${vm.totalPizzas})") }
            }
        }
    }
}

@Composable
private fun TarjetaPizza(vm: PizzeriaViewModel, pizza: Pizza) {
    val disponibles = vm.disponibles(pizza)
    val cantidad = vm.cantidadDe(pizza)
    val agotada = disponibles == 0 && cantidad == 0

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("${pizza.nombre} · ${pizza.precio.comoMonto()}", style = MaterialTheme.typography.titleMedium)
            Text("Ingredientes: ${pizza.receta.keys.joinToString(", ")}", style = MaterialTheme.typography.bodySmall)
            Text(
                if (agotada) "AGOTADA" else "Disponibles: hasta ${disponibles + cantidad}",
                color = if (agotada) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodySmall
            )
            if (Promociones.esPromocionable(pizza) && Promociones.enPeriodo(LocalDate.now())) {
                Text(
                    "Promoción vigente: ${Promociones.descuentoDe(pizza)}% de descuento (hasta el ${Promociones.PERIODO.endInclusive})",
                    color = MaterialTheme.colorScheme.tertiary, style = MaterialTheme.typography.bodySmall
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(onClick = { vm.quitar(pizza) }, enabled = cantidad > 0) { Text("−") }
                Text("$cantidad", modifier = Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.titleMedium)
                Button(onClick = { vm.agregar(pizza) }, enabled = vm.puedeAgregar(pizza)) { Text("+") }
            }
        }
    }
}

// ---------------------------------------------------------------- Paso 3: resumen
@Composable
private fun ResumenScreen(vm: PizzeriaViewModel, modifier: Modifier) {
    val pedido = vm.pedidoBorrador()
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Titulo("Resumen del pedido")
        Text("Paso 3 de 3 · aún no confirmado", style = MaterialTheme.typography.labelLarge)
        if (pedido == null) {
            Text("No hay pizzas en el pedido.")
        } else {
            Card(Modifier.fillMaxWidth()) {
                Text(pedido.resumen(), fontFamily = FontFamily.Monospace, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodySmall)
            }
            if (pedido.correoNotificaciones != null) {
                Text("Recibirás notificaciones en ${pedido.correoNotificaciones}.")
            }
        }
        Button(onClick = vm::confirmar, enabled = pedido != null, modifier = Modifier.fillMaxWidth()) { Text("Confirmar pedido") }
        OutlinedButton(onClick = vm::volver, modifier = Modifier.fillMaxWidth()) { Text("Editar pizzas") }
        OutlinedButton(onClick = vm::cancelarPedido, modifier = Modifier.fillMaxWidth()) { Text("Cancelar pedido") }
    }
}

// ---------------------------------------------------------------- Confirmación
@Composable
private fun ConfirmadoScreen(vm: PizzeriaViewModel, modifier: Modifier) {
    val pedido = vm.ultimoPedido
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Titulo("¡Pedido confirmado! 🍕")
        if (pedido != null) {
            Card(Modifier.fillMaxWidth()) {
                Text(pedido.resumen(), fontFamily = FontFamily.Monospace, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodySmall)
            }
        }
        vm.mensajeNotificacion?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
        Text("Te garantizamos que tu pedido llegará en menos de 40 minutos. ¡Muchas gracias por preferirnos!")
        Button(onClick = vm::iniciarPedido, enabled = vm.hayStock, modifier = Modifier.fillMaxWidth()) { Text("Atender otro cliente") }
        OutlinedButton(onClick = { vm.irA(Pantalla.PEDIDOS) }, modifier = Modifier.fillMaxWidth()) { Text("Ver pedidos del día") }
        OutlinedButton(onClick = { vm.irA(Pantalla.BIENVENIDA) }, modifier = Modifier.fillMaxWidth()) { Text("Inicio") }
    }
}

// ---------------------------------------------------------------- Pedidos del día + reporte
@Composable
private fun PedidosScreen(vm: PizzeriaViewModel, modifier: Modifier) {
    @Suppress("UNUSED_VARIABLE") val refrescar = vm.cambios   // vuelve a dibujar al cambiar un estado
    val pedidos = vm.pedidosDelDia

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Titulo("Pedidos del día") }
        vm.mensajeNotificacion?.let { mensaje -> item { Text(mensaje, color = MaterialTheme.colorScheme.primary) } }

        if (pedidos.isEmpty()) {
            item { Text("No se registraron pedidos hoy.") }
        }
        items(pedidos) { pedido ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(pedido.cliente.nombre, style = MaterialTheme.typography.titleMedium)
                    Text(pedido.detallePizzas)
                    Text("Total: ${pedido.total.comoMonto()} · Entrega: ${pedido.repartidor.nombre}", style = MaterialTheme.typography.bodySmall)
                    Text("Notas: ${pedido.notasTexto}", style = MaterialTheme.typography.bodySmall)
                    Text("Estado: ${pedido.estado.texto}", fontWeight = FontWeight.Bold)
                    val siguiente = pedido.estado.siguiente()
                    if (siguiente != null) {
                        Button(onClick = { vm.avanzarEstado(pedido) }) { Text("Avanzar a '${siguiente.texto}'") }
                    }
                }
            }
        }

        if (pedidos.isNotEmpty()) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Totales del día", style = MaterialTheme.typography.titleMedium)
                        Text("Subtotal ventas: ${pedidos.sumOf { it.subtotal }.comoMonto()}")
                        Text("Descuentos: -${pedidos.sumOf { it.montoDescuento }.comoMonto()}")
                        Text("Valor neto: ${pedidos.sumOf { it.totalNeto }.comoMonto()}")
                        Text("IVA (${Pedido.TASA_IVA}%): ${pedidos.sumOf { it.iva }.comoMonto()}")
                        Text("Total vendido: ${pedidos.sumOf { it.total }.comoMonto()}", fontWeight = FontWeight.Bold)
                        val completados = pedidos.count { it.estado.esCompletado }
                        Text("Completados: $completados · Pendientes: ${pedidos.size - completados}")
                    }
                }
            }
        }

        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Stock restante", style = MaterialTheme.typography.titleMedium)
                    for (s in vm.inventario.estadoStock()) {
                        val unidad = vm.inventario.unidadDe(s.ingrediente)
                        Text(
                            "${s.ingrediente}: ${s.disponible} de ${s.inicial} $unidad" + if (s.bajo) "  ¡STOCK BAJO!" else "",
                            color = if (s.bajo) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        item { OutlinedButton(onClick = vm::volver) { Text("Inicio") } }
    }
}

@Preview(showBackground = true)
@Composable
private fun BienvenidaPreview() {
    PizzeriaIlNonoTheme { BienvenidaScreen() }
}
