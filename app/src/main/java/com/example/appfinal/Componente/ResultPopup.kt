package com.example.appfinal.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.appfinal.ML.MLResult

@Composable
fun ResultPopup(
    result: MLResult,
    onDismiss: () -> Unit,
    onSaveToHistory: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Título
                Text(
                    text = "Resultado da Análise",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xff1b2e3a),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Ícone de tendência
                Text(
                    text = if (result.status) "📈" else "📉",
                    fontSize = 48.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Status da tendência
                Text(
                    text = "Tendência de ${result.tendencia}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (result.status) Color(0xFF4CAF50) else Color(0xFFF44336)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Informações detalhadas
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        DetailRow("Similaridade:", "${String.format("%.2f", result.similaridadePorcentagem)}%")
                        DetailRow("Padrão:", result.descricaoPadrao)
                        DetailRow("Data:", result.dataAnalise)
                        DetailRow("Hora:", result.horaAnalise)
                        if (result.distanciaEuclidiana > 0) {
                            DetailRow("Distância:", String.format("%.4f", result.distanciaEuclidiana))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Botões
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Fechar", color = Color.Gray)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            onSaveToHistory()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xff1b2e3a)
                        )
                    ) {
                        Text("Salvar no Histórico", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF666666),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            color = Color(0xFF333333),
            modifier = Modifier.weight(1.5f),
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun LoadingPopup(onDismiss: () -> Unit = {}) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.size(200.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    color = Color(0xff1b2e3a),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Analisando imagem...",
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun ErrorPopup(
    errorMessage: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Erro na Análise",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF44336)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "❌",
                    fontSize = 48.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = errorMessage,
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF44336)
                    )
                ) {
                    Text("Fechar", color = Color.White)
                }
            }
        }
    }
}