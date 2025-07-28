package com.example.appfinal.Interfaces

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.appfinal.Componente.CaixaDeTexto
import com.example.appfinal.ui.theme.fundo
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Registro(navController: NavController, modifier: Modifier = Modifier) {
    var ID by remember {mutableStateOf("")}
    var nomeUsuario by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var confirmarSenha by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }

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
        snackbarHost = { androidx.compose.material3.SnackbarHost(hostState = snackbarHostState) },
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

                            else -> {
                                navController.navigate("TelaPrincipal")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xff1b2e3a),
                        contentColor = Color.White
                    )
                ) {
                    Text("Cadastrar")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Já possuo cadastro",
                    color = Color.Black,
                    modifier = Modifier
                        .clickable {
                            navController.navigate("TelaLogin")
                        },
                    textDecoration = TextDecoration.Underline
                )
            }
        }
    )
}