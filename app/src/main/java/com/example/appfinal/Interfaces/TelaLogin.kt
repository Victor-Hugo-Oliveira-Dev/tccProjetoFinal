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
    val webClientId = "814599502740-agdd8bmbudie37sgku479iksvpnhsuhe.apps.googleusercontent.com"

    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
    }

    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    // Launcher para resultado do Google Sign-In
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        coroutineScope.launch {
            try {
                isLoading = true
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                val account = task.getResult(ApiException::class.java)

                Log.d("GoogleSignIn", "Conta obtida: ${account.email}")

                if (account.idToken != null) {
                    val loginResult = firebaseService.signInWithGoogle(account)
                    if (loginResult.isSuccess) {
                        Log.d("GoogleSignIn", "Login com Google bem-sucedido")
                        navController.navigate("TelaPrincipal") {
                            popUpTo("TelaLogin") { inclusive = true }
                        }
                    } else {
                        val error = loginResult.exceptionOrNull()?.message ?: "Erro desconhecido"
                        Log.e("GoogleSignIn", "Erro no login: $error")
                        snackbarHostState.showSnackbar("Erro no login com Google: $error")
                    }
                } else {
                    Log.e("GoogleSignIn", "Token ID é nulo")
                    snackbarHostState.showSnackbar("Erro: Token de autenticação não encontrado")
                }
            } catch (e: ApiException) {
                Log.e("GoogleSignIn", "Erro no Google Sign-In: ${e.statusCode} - ${e.message}", e)
                when (e.statusCode) {
                    12501 -> snackbarHostState.showSnackbar("Login cancelado pelo usuário")
                    12502 -> snackbarHostState.showSnackbar("Erro de rede. Verifique sua conexão")
                    else -> snackbarHostState.showSnackbar("Erro no login com Google: ${e.message}")
                }
            } catch (e: Exception) {
                Log.e("GoogleSignIn", "Erro inesperado", e)
                snackbarHostState.showSnackbar("Erro inesperado no login")
            } finally {
                isLoading = false
            }
        }
    }

    // Verificar se o usuário já está logado ao inicializar a tela
    LaunchedEffect(Unit) {
        if (firebaseService.checkUserLoggedIn()) {
            Log.d("TelaLogin", "Usuário já está logado, redirecionando...")
            navController.navigate("TelaPrincipal") {
                popUpTo("TelaLogin") { inclusive = true }
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
                    Text(
                        text = "Realizando login...",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
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
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                CaixaDeTexto(
                    value = senha,
                    onValueChange = { senha = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = "Senha",
                    keyboardType = KeyboardType.Password,
                    visualTransformation = if (mostrarSenha) VisualTransformation.None else PasswordVisualTransformation(),
                    enabled = !isLoading,
                    trailingIcon = {
                        val iconRes = if (mostrarSenha) {
                            R.drawable.baseline_remove_red_eye_24
                        } else {
                            R.drawable.baseline_visibility_off_24
                        }
                        IconButton(
                            onClick = { mostrarSenha = !mostrarSenha },
                            enabled = !isLoading
                        ) {
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
                            coroutineScope.launch {
                                isLoading = true
                                val result = firebaseService.loginUser(email, senha)
                                if (result.isSuccess) {
                                    navController.navigate("TelaPrincipal") {
                                        popUpTo("TelaLogin") { inclusive = true }
                                    }
                                } else {
                                    val error = result.exceptionOrNull()?.message ?: "Erro desconhecido"
                                    snackbarHostState.showSnackbar("Erro: $error")
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
                    Text(
                        text = if (isLoading) "Entrando..." else "Entrar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = {
                        if (!isLoading) {
                            coroutineScope.launch {
                                try {
                                    // Fazer sign out antes de tentar novo login para evitar conflitos
                                    googleSignInClient.signOut()
                                    Log.d("GoogleSignIn", "Iniciando Google Sign-In")
                                    val signInIntent = googleSignInClient.signInIntent
                                    googleSignInLauncher.launch(signInIntent)
                                } catch (e: Exception) {
                                    Log.e("GoogleSignIn", "Erro ao iniciar intent", e)
                                    snackbarHostState.showSnackbar("Erro ao iniciar login com Google")
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (isLoading) "Entrando..." else "Entrar com Google",
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
                    modifier = Modifier.clickable(enabled = !isLoading) {
                        navController.navigate("Registro")
                    },
                    textDecoration = TextDecoration.Underline
                )
            }
        }
    }
}