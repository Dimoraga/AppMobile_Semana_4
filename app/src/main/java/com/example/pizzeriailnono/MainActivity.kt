package com.example.pizzeriailnono

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.pizzeriailnono.ui.theme.PizzeriaIlNonoTheme

class MainActivity : ComponentActivity() {

    // El ViewModel conserva el pedido, el inventario y los pedidos del día al rotar la pantalla
    private val viewModel: PizzeriaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // En algunos emuladores el splash del sistema (Android 12+) se queda tapando la app.
        // Se retira en cuanto la app dibuja su primer frame.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            splashScreen.setOnExitAnimationListener { splashView -> splashView.remove() }
        }
        enableEdgeToEdge()
        setContent {
            PizzeriaIlNonoTheme {
                PizzeriaApp(viewModel)
            }
        }
    }
}
