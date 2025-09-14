package com.example.appfinal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appfinal.Interfaces.*
import com.example.appfinal.FireBase.SimpleFirebaseService

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
    val firebaseService = remember { SimpleFirebaseService() }

    // ✅ Determina a tela inicial baseada no estado do login
    val startDestination = if (firebaseService.checkUserLoggedIn()) {
        "TelaPrincipal" // Usuário já logado, vai direto para a tela principal
    } else {
        "TelaLogin" // Usuário não logado, vai para tela de login
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("TelaPrincipal") {
            TelaPrincipal(navController)
        }
        composable("TelaLogin") {
            TelaLogin(navController = navController)
        }
        composable("Registro") {
            Registro(navController)
        }
        composable("Dados") {
            Dados(navController)
        }
        composable("Sobre") {
            Sobre(navController)
        }
    }
}