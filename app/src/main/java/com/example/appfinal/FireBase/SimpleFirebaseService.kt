package com.example.appfinal.FireBase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.example.appfinal.DataClass.Historico
import kotlinx.coroutines.tasks.await
import android.util.Log
import java.util.*

class SimpleFirebaseService {

    private val firestore = FirebaseFirestore.getInstance()

    // Gera um ID único para o dispositivo (simula usuário)
    private fun getDeviceId(): String {
        // Em produção, você pode usar:
        // Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        // Por enquanto, vamos usar um ID fixo para testes
        return "device_${UUID.randomUUID().toString().take(8)}"
    }

    suspend fun saveHistorico(historico: Historico): Result<String> {
        return try {
            val historicoData = mapOf(
                "data" to historico.data,
                "hora" to historico.hora,
                "status" to historico.status,
                "similaridadePorcentagem" to historico.similaridadePorcentagem,
                "descricaoPadrao" to historico.descricaoPadrao,
                "imagemSimilar" to historico.imagemSimilar,
                "distanciaEuclidiana" to historico.distanciaEuclidiana,
                "deviceId" to getDeviceId(),
                "timestamp" to System.currentTimeMillis()
            )

            val documentRef = firestore
                .collection("historicos")
                .add(historicoData)
                .await()

            Log.d("SimpleFirebase", "✅ Histórico salvo: ${documentRef.id}")
            Result.success(documentRef.id)
        } catch (e: Exception) {
            Log.e("SimpleFirebase", "❌ Erro ao salvar histórico", e)
            Result.failure(e)
        }
    }

    suspend fun getHistoricos(): Result<List<Historico>> {
        return try {
            val querySnapshot = firestore
                .collection("historicos")
                .whereEqualTo("deviceId", getDeviceId())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50) // Limita a 50 registros
                .get()
                .await()

            val historicos = querySnapshot.documents.mapNotNull { document ->
                try {
                    Historico(
                        data = document.getString("data") ?: "",
                        hora = document.getString("hora") ?: "",
                        status = document.getBoolean("status") ?: false,
                        similaridadePorcentagem = document.getDouble("similaridadePorcentagem") ?: 0.0,
                        descricaoPadrao = document.getString("descricaoPadrao") ?: "",
                        imagemSimilar = document.getString("imagemSimilar") ?: "",
                        distanciaEuclidiana = document.getDouble("distanciaEuclidiana") ?: 0.0
                    )
                } catch (e: Exception) {
                    Log.w("SimpleFirebase", "Erro ao converter documento: ${document.id}", e)
                    null
                }
            }

            Log.d("SimpleFirebase", "✅ Históricos carregados: ${historicos.size}")
            Result.success(historicos)
        } catch (e: Exception) {
            Log.e("SimpleFirebase", "❌ Erro ao carregar históricos", e)
            Result.failure(e)
        }
    }

    suspend fun deleteHistorico(historico: Historico): Result<Boolean> {
        return try {
            val querySnapshot = firestore
                .collection("historicos")
                .whereEqualTo("deviceId", getDeviceId())
                .whereEqualTo("data", historico.data)
                .whereEqualTo("hora", historico.hora)
                .whereEqualTo("similaridadePorcentagem", historico.similaridadePorcentagem)
                .get()
                .await()

            if (querySnapshot.documents.isNotEmpty()) {
                querySnapshot.documents[0].reference.delete().await()
                Log.d("SimpleFirebase", "✅ Histórico deletado")
                Result.success(true)
            } else {
                Result.failure(Exception("Histórico não encontrado"))
            }
        } catch (e: Exception) {
            Log.e("SimpleFirebase", "❌ Erro ao deletar histórico", e)
            Result.failure(e)
        }
    }

    // Função para limpar todos os dados (útil para testes)
    suspend fun clearAllData(): Result<Boolean> {
        return try {
            val querySnapshot = firestore
                .collection("historicos")
                .whereEqualTo("deviceId", getDeviceId())
                .get()
                .await()

            val batch = firestore.batch()
            querySnapshot.documents.forEach { document ->
                batch.delete(document.reference)
            }

            batch.commit().await()
            Log.d("SimpleFirebase", "✅ Todos os dados limpos")
            Result.success(true)
        } catch (e: Exception) {
            Log.e("SimpleFirebase", "❌ Erro ao limpar dados", e)
            Result.failure(e)
        }
    }
}