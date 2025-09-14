package com.example.appfinal.Interfaces

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.appfinal.Componente.CaixaDeTexto
import com.example.appfinal.R
import com.example.appfinal.ui.theme.fundo
import com.example.appfinal.FireBase.SimpleFirebaseService
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.launch

@Composable
fun TelaLogin(navController: NavController, modifier: Modifier = Modifier) {
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var mostrarSenha by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = modifier
                    .background(color = fundo)
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color(0xff1b2e3a))
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo GrafVar",
                )

                Spacer(modifier = Modifier.height(16.dp))

                CaixaDeTexto(
                    value = email,
                    onValueChange = { email = it },
                    label = "E-mail",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                CaixaDeTexto(
                    value = senha,
                    onValueChange = { senha = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = "Senha",
                    keyboardType = KeyboardType.Password,
                    visualTransformation = if (mostrarSenha) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val iconRes = if (mostrarSenha) {
                            R.drawable.baseline_remove_red_eye_24
                        } else {
                            R.drawable.baseline_visibility_off_24
                        }
                        IconButton(onClick = { mostrarSenha = !mostrarSenha }) {
                            Icon(
                                painter = painterResource(id = iconRes),
                                contentDescription = "Alternar Visibilidade da Senha",
                                tint = Color.Gray
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (email.isBlank() || senha.isBlank()) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Preencha todos os campos.")
                            }
                        } else {
                            isLoading = true
                            coroutineScope.launch {
                                val firebaseService = SimpleFirebaseService()
                                val result = firebaseService.loginUser(email, senha)
                                if (result.isSuccess) {
                                    navController.navigate("TelaPrincipal") {
                                        popUpTo("TelaLogin") { inclusive = true }
                                    }
                                } else {
                                    snackbarHostState.showSnackbar("Erro: ${result.exceptionOrNull()?.message}")
                                }
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xff1b2e3a),
                        contentColor = Color.White
                    ),
                    enabled = !isLoading
                ) {
                    Text(if (isLoading) "Entrando..." else "Entrar")
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Cadastrar",
                    color = Color.Black,
                    modifier = Modifier.clickable {
                        navController.navigate("Registro")
                    },
                    textDecoration = TextDecoration.Underline
                )
            }
        }
    }
}
