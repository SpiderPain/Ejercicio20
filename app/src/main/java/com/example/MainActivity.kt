package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.data.local.MotContDatabase
import com.example.data.repository.FuelLogRepository
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.MotContTheme
import com.example.ui.viewmodel.MotContViewModel
import com.example.ui.viewmodel.MotContViewModelFactory

/**
 * Actividad principal de MotCont.
 * Inicializa la base de datos local Room y el ViewModel sin bibliotecas externas de inyección pesadas.
 */
class MainActivity : ComponentActivity() {

    private val viewModel: MotContViewModel by viewModels {
        val database = MotContDatabase.getDatabase(applicationContext, lifecycleScope)
        val repository = FuelLogRepository(database.motContDao())
        MotContViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MotContTheme {
                HomeScreen(viewModel = viewModel)
            }
        }
    }
}
