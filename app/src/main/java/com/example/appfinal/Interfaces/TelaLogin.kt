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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.appfinal.Componente.CaixaDeTexto
import com.example.appfinal.R
import com.example.appfinal.ui.theme.fundo

@Composable
fun TelaLogin(navController: NavController, modifier: Modifier = Modifier) {
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var mostrarSenha by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize())
    {
        Column(
            modifier = modifier
                .background(color = fundo)
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){

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
                    val icone: Painter = if (mostrarSenha) {
                        painterResource(id = R.drawable.baseline_remove_red_eye_24)
                    } else {
                        painterResource(id = R.drawable.baseline_visibility_off_24)
                    }
                    IconButton(onClick = { mostrarSenha = !mostrarSenha }) {
                        Icon(painter = icone, contentDescription = "Alternar Visibilidade da Senha")
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    navController.navigate("TelaPrincipal")
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xff1b2e3a),
                    contentColor = Color.White
                )
            ) {
                Text("Entrar")
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Cadastrar",
                color = Color.Black,
                modifier = Modifier
                    .clickable {
                        navController.navigate("Registro")
                    },
                textDecoration = TextDecoration.Underline
            )
        }
    }
}