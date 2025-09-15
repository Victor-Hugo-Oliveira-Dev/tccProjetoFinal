package com.example.appfinal.Interfaces

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.appfinal.Componente.CaixaDeTexto
import com.example.appfinal.R
import com.example.appfinal.ui.theme.fundo
import com.example.appfinal.FireBase.SimpleFirebaseService
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.util.Log
import kotlinx.coroutines.launch

@Composable
fun TelaLogin(navController: NavController, modifier: Modifier = Modifier) {
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var mostrarSenha by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val firebaseService = remember { SimpleFirebaseService() }

    // Configuração do Google Sign-In
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("YOUR_WEB_CLIENT_ID_HERE") 
            .requestEmail()
            .build()
    }

    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    // Launcher para resultado do Google Sign-In
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)

            coroutineScope.launch {
                isLoading = true
                val loginResult = firebaseService.signInWithGoogle(account)
                if (loginResult.isSuccess) {
                    navController.navigate("TelaPrincipal") {
                        popUpTo("TelaLogin") { inclusive = true }
                    }
                } else {
                    snackbarHostState.showSnackbar("Erro no login com Google: ${loginResult.exceptionOrNull()?.message}")
                }
                isLoading = false
            }
        } catch (e: ApiException) {
            Log.e("GoogleSignIn", "Erro no Google Sign-In", e)
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Erro no login com Google")
            }
        }
    }

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
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isLoading
                ) {
                    Text(if (isLoading) "Entrando..." else "Entrar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = {
                        if (!isLoading) {
                            val signInIntent = googleSignInClient.signInIntent
                            googleSignInLauncher.launch(signInIntent)
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Entrar com Google",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Divisor
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Divider(modifier = Modifier.weight(1f))
                    Text(
                        text = " ou ",
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Divider(modifier = Modifier.weight(1f))
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
