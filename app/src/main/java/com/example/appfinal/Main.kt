package com.example.appfinal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appfinal.Interfaces.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppFinal()
        }
    }
}

@Composable
fun AppFinal() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "TelaLogin"
    ) {
        composable("TelaPrincipal") { TelaPrincipal(navController) }
        composable("TelaLogin") { TelaLogin(navController = navController) }
        composable("Registro") { Registro(navController) }
        composable("Dados") { Dados(navController) }
        composable("Sobre") { Sobre(navController) }
    }
}
