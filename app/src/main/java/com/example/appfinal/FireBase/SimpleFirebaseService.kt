package com.example.appfinal.FireBase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.GoogleAuthProvider
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.example.appfinal.DataClass.Historico
import kotlinx.coroutines.tasks.await
import android.util.Log

class SimpleFirebaseService {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Obtém o ID do usuário logado
    private fun getUserId(): String {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.w("SimpleFirebase", "Usuário não autenticado")
            throw Exception("Usuário não autenticado. Faça login primeiro.")
        }
        return currentUser.uid
    }

    fun checkUserLoggedIn(): Boolean {
        val currentUser = auth.currentUser
        val isLoggedIn = currentUser != null
        Log.d("SimpleFirebase", "✅ Verificação de login: $isLoggedIn")
        if (isLoggedIn) {
            Log.d("SimpleFirebase", "Usuário atual: ${currentUser?.email}")
        }
        return isLoggedIn
    }

    suspend fun signInWithGoogle(account: GoogleSignInAccount): Result<Boolean> {
        return try {
            Log.d("SimpleFirebase", "Iniciando login com Google para: ${account.email}")

            // Verificar se o token ID está presente
            val idToken = account.idToken
            if (idToken == null) {
                Log.e("SimpleFirebase", "Token ID é nulo")
                return Result.failure(Exception("Token de autenticação não encontrado"))
            }

            Log.d("SimpleFirebase", "Token ID obtido, criando credential")
            val credential = GoogleAuthProvider.getCredential(idToken, null)

            Log.d("SimpleFirebase", "Fazendo login com credential")
            val authResult = auth.signInWithCredential(credential).await()

            val user = authResult.user
            if (user == null) {
                Log.e("SimpleFirebase", "Usuário é nulo após autenticação")
                return Result.failure(Exception("Falha na autenticação"))
            }

            Log.d("SimpleFirebase", "Usuário autenticado: ${user.email}")

            // Verifica se é um novo usuário e cria documento no Firestore
            val isNewUser = authResult.additionalUserInfo?.isNewUser ?: false
            Log.d("SimpleFirebase", "É novo usuário: $isNewUser")

            if (isNewUser) {
                val userData = mapOf(
                    "username" to (user.displayName ?: "Usuário Google"),
                    "email" to (user.email ?: ""),
                    "provider" to "google",
                    "createdAt" to System.currentTimeMillis()
                )

                Log.d("SimpleFirebase", "Criando documento no Firestore para novo usuário")
                firestore.collection("users")
                    .document(user.uid)
                    .set(userData)
                    .await()

                Log.d("SimpleFirebase", "Novo usuário Google registrado: ${user.email}")
            } else {
                Log.d("SimpleFirebase", "Usuário Google existente logado: ${user.email}")

                // Verificar se o documento existe no Firestore, se não, criar
                try {
                    val userDoc = firestore.collection("users")
                        .document(user.uid)
                        .get()
                        .await()

                    if (!userDoc.exists()) {
                        Log.d("SimpleFirebase", "Documento do usuário não existe, criando...")
                        val userData = mapOf(
                            "username" to (user.displayName ?: "Usuário Google"),
                            "email" to (user.email ?: ""),
                            "provider" to "google",
                            "createdAt" to System.currentTimeMillis()
                        )

                        firestore.collection("users")
                            .document(user.uid)
                            .set(userData)
                            .await()
                    }
                } catch (e: Exception) {
                    Log.e("SimpleFirebase", "Erro ao verificar/criar documento do usuário", e)
                    // Não falhar o login por causa disso
                }
            }

            Log.d("SimpleFirebase", "Login com Google concluído com sucesso")
            Result.success(true)
        } catch (e: Exception) {
            Log.e("SimpleFirebase", "Erro no login com Google: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Função para registrar novo usuário - NÃO faz login automático
    suspend fun registerUser(email: String, password: String, username: String): Result<Boolean> {
        return try {
            Log.d("SimpleFirebase", "Registrando novo usuário: $email")

            // Cria o usuário no Authentication
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()

            // Atualiza o perfil com o nome de usuário
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .build()

            authResult.user?.updateProfile(profileUpdates)?.await()

            // Cria documento do usuário no Firestore
            val userData = mapOf(
                "username" to username,
                "email" to email,
                "provider" to "email",
                "createdAt" to System.currentTimeMillis()
            )

            firestore.collection("users")
                .document(authResult.user!!.uid)
                .set(userData)
                .await()

            // Fazer logout após registro para que o usuário precise fazer login manualmente
            auth.signOut()

            Log.d("SimpleFirebase", "Usuário registrado (sem login automático): $email")
            Result.success(true)
        } catch (e: Exception) {
            Log.e("SimpleFirebase", "Erro ao registrar usuário: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Função para fazer login
    suspend fun loginUser(email: String, password: String): Result<Boolean> {
        return try {
            Log.d("SimpleFirebase", "Fazendo login: $email")
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            Log.d("SimpleFirebase", "Usuário logado: ${authResult.user?.email}")
            Result.success(true)
        } catch (e: Exception) {
            Log.e("SimpleFirebase", "Erro ao fazer login: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Função para fazer logout
    fun logoutUser() {
        try {
            auth.signOut()
            Log.d("SimpleFirebase", "Usuário deslogado com sucesso")
        } catch (e: Exception) {
            Log.e("SimpleFirebase", "Erro ao fazer logout", e)
        }
    }

    // Verifica se usuário está logado
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    // Obtém nome do usuário logado
    fun getCurrentUsername(): String {
        return auth.currentUser?.displayName ?: "Usuário"
    }

    fun getCurrentUserEmail(): String {
        return auth.currentUser?.email ?: ""
    }

    suspend fun saveHistorico(historico: Historico): Result<String> {
        return try {
            val userId = getUserId()

            val historicoData = mapOf(
                "data" to historico.data,
                "hora" to historico.hora,
                "status" to historico.status,
                "similaridadePorcentagem" to historico.similaridadePorcentagem,
                "descricaoPadrao" to historico.descricaoPadrao,
                "imagemSimilar" to historico.imagemSimilar,
                "distanciaEuclidiana" to historico.distanciaEuclidiana,
                "userId" to userId,
                "timestamp" to System.currentTimeMillis()
            )

            val documentRef = firestore
                .collection("historicos")
                .add(historicoData)
                .await()

            Log.d("SimpleFirebase", "Histórico salvo para usuário: $userId")
            Result.success(documentRef.id)
        } catch (e: Exception) {
            Log.e("SimpleFirebase", "Erro ao salvar histórico", e)
            Result.failure(e)
        }
    }

    suspend fun getHistoricos(): Result<List<Historico>> {
        return try {
            val userId = getUserId()

            val querySnapshot = firestore
                .collection("historicos")
                .whereEqualTo("userId", userId)
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

            val sortedHistoricos = historicos.sortedByDescending {
                it.data + it.hora
            }.take(50)

            Log.d("SimpleFirebase", "Históricos carregados para usuário $userId: ${sortedHistoricos.size}")
            Result.success(sortedHistoricos)
        } catch (e: Exception) {
            Log.e("SimpleFirebase", "Erro ao carregar históricos", e)
            Result.failure(e)
        }
    }

    suspend fun deleteHistorico(historico: Historico): Result<Boolean> {
        return try {
            val userId = getUserId()

            val querySnapshot = firestore
                .collection("historicos")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val documentToDelete = querySnapshot.documents.find { doc ->
                doc.getString("data") == historico.data &&
                        doc.getString("hora") == historico.hora &&
                        doc.getDouble("similaridadePorcentagem") == historico.similaridadePorcentagem
            }

            if (documentToDelete != null) {
                documentToDelete.reference.delete().await()
                Log.d("SimpleFirebase", "Histórico deletado do usuário: $userId")
                Result.success(true)
            } else {
                Result.failure(Exception("Histórico não encontrado"))
            }
        } catch (e: Exception) {
            Log.e("SimpleFirebase", "Erro ao deletar histórico", e)
            Result.failure(e)
        }
    }

    // Função para limpar todos os dados (útil para testes)
    suspend fun clearAllData(): Result<Boolean> {
        return try {
            val userId = getUserId()
            val querySnapshot = firestore
                .collection("historicos")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val batch = firestore.batch()
            querySnapshot.documents.forEach { document ->
                batch.delete(document.reference)
            }

            batch.commit().await()
            Log.d("SimpleFirebase", "Todos os dados do usuário $userId foram limpos")
            Result.success(true)
        } catch (e: Exception) {
            Log.e("SimpleFirebase", "Erro ao limpar dados", e)
            Result.failure(e)
        }
    }
}