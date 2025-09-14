package com.example.appfinal.Interfaces

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.appfinal.DataClass.Historico
import com.example.appfinal.FireBase.SimpleFirebaseService
import com.example.appfinal.ui.theme.fundo
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dados(navController: NavController) {
    val firebaseService = remember { SimpleFirebaseService() }
    val scope = rememberCoroutineScope()
    var historico by remember { mutableStateOf<List<Historico>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Carregar dados do Firebase quando a tela for exibida
    LaunchedEffect(Unit) {
        scope.launch {
            isLoading = true
            val result = firebaseService.getHistoricos()
            if (result.isSuccess) {
                historico = result.getOrNull() ?: emptyList()
                errorMessage = null
            } else {
                errorMessage = "Erro ao carregar histórico: ${result.exceptionOrNull()?.message}"
                historico = emptyList()
            }
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBarCentralizado(titulo = "Histórico")
        },
        bottomBar = {
            BarraInferiorNavegacao(navController)
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(fundo)
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xff1b2e3a))
            }
        } else if (errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(fundo)
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "❌",
                        fontSize = 48.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = errorMessage ?: "Erro desconhecido",
                        color = Color.Red,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                val result = firebaseService.getHistoricos()
                                if (result.isSuccess) {
                                    historico = result.getOrNull() ?: emptyList()
                                    errorMessage = null
                                } else {
                                    errorMessage = "Erro ao recarregar: ${result.exceptionOrNull()?.message}"
                                }
                                isLoading = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xff1b2e3a))
                    ) {
                        Text("Tentar Novamente", color = Color.White)
                    }
                }
            }
        } else if (historico.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(fundo)
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "📊",
                        fontSize = 48.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Nenhum histórico encontrado",
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Realize análises para ver o histórico aqui",
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            ListaHistorico(
                historico = historico,
                onRemoverItem = { item ->
                    scope.launch {
                        val result = firebaseService.deleteHistorico(item)
                        if (result.isSuccess) {
                            // Atualiza a lista localmente após exclusão bem-sucedida
                            historico = historico.filterNot { it == item }
                        } else {
                            errorMessage = "Erro ao excluir: ${result.exceptionOrNull()?.message}"
                        }
                    }
                },
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopAppBarCentralizado(titulo: String) {
    CenterAlignedTopAppBar(
        title = { Text(titulo) },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color(0xff1b2e3a),
            titleContentColor = Color.White
        )
    )
}

@Composable
private fun BarraInferiorNavegacao(navController: NavController) {
    BottomAppBar(
        contentColor = Color.White,
        containerColor = Color(0xff1b2e3a)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            BotaoNavegacao(
                onClick = { navController.navigate("TelaPrincipal") },
                icone = Icons.Rounded.Home,
                descricao = "Início",
                tamanho = 50.dp
            )

            Spacer(Modifier.width(16.dp))

            BotaoNavegacao(
                onClick = { navController.navigate("Dados") },
                icone = Icons.Rounded.Menu,
                descricao = "Dados",
                tamanho = 53.dp,
                corFundo = Color(0xfff0f0f0)
            )

            Spacer(Modifier.width(16.dp))

            BotaoNavegacao(
                onClick = { navController.navigate("Sobre") },
                icone = Icons.Rounded.Info,
                descricao = "Sobre",
                tamanho = 50.dp
            )
        }
    }
}

@Composable
private fun BotaoNavegacao(
    onClick: () -> Unit,
    icone: ImageVector,
    descricao: String,
    tamanho: Dp,
    corFundo: Color = Color.White
) {
    SmallFloatingActionButton(
        onClick = onClick,
        modifier = Modifier.size(tamanho),
        containerColor = corFundo,
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(
            imageVector = icone,
            contentDescription = descricao,
            tint = Color(0xff1b2e3a)
        )
    }
}

@Composable
private fun ListaHistorico(
    historico: List<Historico>,
    onRemoverItem: (Historico) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .background(color = fundo)
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = historico,
            key = { item -> "${item.data}_${item.hora}_${item.similaridadePorcentagem}" }
        ) { item ->
            ItemHistorico(
                item = item,
                onRemover = { onRemoverItem(item) }
            )
        }
    }
}

@Composable
private fun ItemHistorico(item: Historico, onRemover: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (item.status) "📈" else "📉",
                    fontSize = 32.sp,
                    modifier = Modifier.padding(end = 12.dp)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${item.data} | ${item.hora}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF333333)
                    )
                    Text(
                        text = if (item.status) "Tendência de ALTA" else "Tendência de BAIXA",
                        fontSize = 14.sp,
                        color = if (item.status) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )
                    if (item.similaridadePorcentagem > 0) {
                        Text(
                            text = "Similaridade: ${String.format("%.1f", item.similaridadePorcentagem)}%",
                            fontSize = 12.sp,
                            color = Color(0xFF666666)
                        )
                    }
                    if (item.descricaoPadrao.isNotEmpty()) {
                        Text(
                            text = item.descricaoPadrao,
                            fontSize = 12.sp,
                            color = Color(0xFF666666),
                            maxLines = 1
                        )
                    }
                }
            }

            IconButton(onClick = onRemover) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Excluir",
                    tint = Color.Red
                )
            }
        }
    }
}