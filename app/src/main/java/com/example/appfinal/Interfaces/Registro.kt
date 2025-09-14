package com.example.appfinal.Interfaces

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.appfinal.Componente.CaixaDeTexto
import com.example.appfinal.ui.theme.fundo
import com.example.appfinal.FireBase.SimpleFirebaseService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Registro(navController: NavController, modifier: Modifier = Modifier) {
    var nomeUsuario by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var confirmarSenha by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Cadastro") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xff1b2e3a),
                    titleContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        content = { paddingValues ->
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

                CaixaDeTexto(
                    value = nomeUsuario,
                    onValueChange = { nomeUsuario = it },
                    label = "Nome",
                    keyboardType = KeyboardType.Text,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                CaixaDeTexto(
                    value = email,
                    onValueChange = { email = it },
                    label = "E-mail",
                    keyboardType = KeyboardType.Email,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                CaixaDeTexto(
                    value = senha,
                    onValueChange = { senha = it },
                    label = "Senha",
                    keyboardType = KeyboardType.Password,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                CaixaDeTexto(
                    value = confirmarSenha,
                    onValueChange = { confirmarSenha = it },
                    label = "Confirmar Senha",
                    keyboardType = KeyboardType.Password,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(23.dp))

                Button(
                    onClick = {
                        when {
                            nomeUsuario.isBlank() || email.isBlank() || senha.isBlank() || confirmarSenha.isBlank() -> {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Preencha todos os campos.")
                                }
                            }
                            senha != confirmarSenha -> {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("As senhas não coincidem.")
                                }
                            }
                            senha.length < 6 -> {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("A senha deve ter pelo menos 6 caracteres.")
                                }
                            }
                            else -> {
                                isLoading = true
                                coroutineScope.launch {
                                    val firebaseService = SimpleFirebaseService()
                                    val result = firebaseService.registerUser(email, senha, nomeUsuario)
                                    if (result.isSuccess) {
                                        // ✅ MUDANÇA: Agora vai para tela de login em vez de TelaPrincipal
                                        snackbarHostState.showSnackbar("Cadastro realizado! Faça login para continuar.")

                                        // Aguarda um pouco para mostrar a mensagem antes de navegar
                                        kotlinx.coroutines.delay(1000)

                                        navController.navigate("TelaLogin") {
                                            popUpTo("Registro") { inclusive = true }
                                        }
                                    } else {
                                        val errorMsg = result.exceptionOrNull()?.message ?: "Erro desconhecido"
                                        snackbarHostState.showSnackbar("Erro: $errorMsg")
                                    }
                                    isLoading = false
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xff1b2e3a),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isLoading
                ) {
                    Text(if (isLoading) "Cadastrando..." else "Cadastrar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Já possuo cadastro",
                    color = Color.Black,
                    modifier = Modifier.clickable {
                        navController.navigate("TelaLogin")
                    },
                    textDecoration = TextDecoration.Underline
                )
            }
        }
    )
}