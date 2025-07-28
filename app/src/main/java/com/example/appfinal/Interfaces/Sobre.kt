package com.example.appfinal.Interfaces

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Sobre(navController: NavController) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Sobre") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xff1b2e3a),
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            BottomAppBar(
                contentColor = Color.White,
                containerColor = Color(0xff1b2e3a)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    SmallFloatingActionButton(
                        onClick = { navController.navigate("TelaPrincipal") },
                        modifier = Modifier.size(50.dp),
                        containerColor = Color.White,
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Home,
                            contentDescription = "Ínicio",
                            tint = Color(0xff1b2e3a),
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))

                    SmallFloatingActionButton(
                        onClick = { navController.navigate("Dados") },
                        modifier = Modifier.size(50.dp),
                        containerColor = Color.White,
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Menu,
                            contentDescription = "Dados",
                            tint = Color(0xff1b2e3a),
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    SmallFloatingActionButton(
                        onClick = { navController.navigate("Sobre") },
                        modifier = Modifier.size(53.dp),
                        containerColor = Color.White,
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = "Sobre",
                            tint = Color(0xff1b2e3a),
                        )
                    }
                }
            }
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE0E0E0)
                    ),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Sobre o Projeto",
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xff1b2e3a)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Olá, sou Victor. Este projeto tem como objetivo auxiliar traders e investidores em seu dia a dia.\n\n" +
                                    "Por lidar com um grande volume de dados, ele busca, junto a outras ferramentas, aumentar a taxa de operações bem-sucedidas,\n" +
                                    "informando — com base em dados internos — a probabilidade de ganho (gain) ou perda (loss),\n" +
                                    "a partir da análise dos padrões dos 10 últimos CandleSticks do gráfico.\n\n" +
                                    "Projeto desenvolvido para a conclusão do curso de Análise e Desenvolvimento de Sistemas,\n" +
                                    "utilizando ferramentas como Android Studio e linguagens como Kotlin e Python.",
                            fontSize = 16.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }
    )
}