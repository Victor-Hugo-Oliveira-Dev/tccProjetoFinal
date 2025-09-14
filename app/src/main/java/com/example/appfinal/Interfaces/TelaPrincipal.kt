package com.example.appfinal.Interfaces

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.appfinal.R
import com.example.appfinal.ui.theme.fundo
import com.example.appfinal.ML.MLResult
import com.example.appfinal.components.ResultPopup
import com.example.appfinal.components.LoadingPopup
import com.example.appfinal.components.ErrorPopup
import com.example.appfinal.DataClass.Historico
import com.example.appfinal.FireBase.SimpleFirebaseService
import com.example.appfinal.ML.SimplifiedMLAnalyzer
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaPrincipal(
    navController: NavController,
    onAddHistorico: (Historico) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Serviços ML e Firebase
    val mlAnalyzer = remember { SimplifiedMLAnalyzer(context) }
    val firebaseService = remember { SimpleFirebaseService() }

    // Estado para o dialog de logout
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Estados da UI
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    var galleryImageUri by remember { mutableStateOf<Uri?>(null) }
    var showImagePreview by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Estados para popups
    var showLoadingPopup by remember { mutableStateOf(false) }
    var showResultPopup by remember { mutableStateOf(false) }
    var showErrorPopup by remember { mutableStateOf(false) }
    var analysisResult by remember { mutableStateOf<MLResult?>(null) }
    var errorMessage by remember { mutableStateOf("") }

    // Launchers para câmera e galeria
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            selectedImageUri = cameraImageUri
            showImagePreview = true
            Log.d("Camera", "Foto salva em: $cameraImageUri")
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        galleryImageUri = uri
        selectedImageUri = uri
        showImagePreview = true
        Log.d("Gallery", "Imagem selecionada: $galleryImageUri")
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val photoFile = File.createTempFile("foto_", ".jpg", context.cacheDir).apply {
                cameraImageUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    this
                )
            }
            cameraLauncher.launch(cameraImageUri)
        } else {
            Log.w("Permissions", "Permissão da câmera negada!")
        }
    }

    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            galleryLauncher.launch("image/*")
        } else {
            Log.w("Permissions", "Permissão de armazenamento negada!")
        }
    }

    // Função para verificar permissão e abrir galeria
    fun checkStoragePermissionAndOpenGallery() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED -> {
                galleryLauncher.launch("image/*")
            }
            else -> {
                storagePermissionLauncher.launch(permission)
            }
        }
    }

    // Função para converter URI para Bitmap
    fun uriToBitmap(uri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT < 28) {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            } else {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            }
        } catch (e: Exception) {
            Log.e("TelaPrincipal", "Erro ao converter URI para Bitmap", e)
            null
        }
    }

    // Função para analisar imagem com ML local
    fun analyzeImage(bitmap: Bitmap) {
        scope.launch {
            try {
                showLoadingPopup = true

                // Inicializa o ML se necessário
                Log.d("TelaPrincipal", "Inicializando ML Analyzer...")
                if (!mlAnalyzer.initialize()) {
                    throw Exception("Erro ao inicializar o analisador ML")
                }

                // Analisa a imagem localmente
                Log.d("TelaPrincipal", "Analisando imagem...")
                val result = mlAnalyzer.analyzeImage(bitmap)

                showLoadingPopup = false

                if (result.success) {
                    Log.d("TelaPrincipal", "✅ Análise bem-sucedida: ${result.tendencia}")
                    analysisResult = result
                    showResultPopup = true
                } else {
                    Log.e("TelaPrincipal", "❌ Erro na análise: ${result.error}")
                    errorMessage = result.error
                    showErrorPopup = true
                }

            } catch (e: Exception) {
                showLoadingPopup = false
                errorMessage = e.message ?: "Erro desconhecido durante a análise"
                showErrorPopup = true
                Log.e("TelaPrincipal", "❌ Erro na análise", e)
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Grafvar") },
                actions = {
                    // ✅ Botão de Logout no canto superior direito
                    IconButton(
                        onClick = { showLogoutDialog = true }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_logout_24),
                            contentDescription = "Sair",
                            tint = Color.White
                        )
                    }
                },
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
                        modifier = Modifier.size(53.dp),
                        containerColor = Color.White,
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Home,
                            contentDescription = "Início",
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
                        modifier = Modifier.size(50.dp),
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        val photoFile = File.createTempFile("foto_", ".jpg", context.cacheDir).apply {
                            cameraImageUri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.provider",
                                this
                            )
                        }
                        cameraLauncher.launch(cameraImageUri)
                    } else {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
                containerColor = Color(0xff1b2e3a),
                contentColor = Color.White
            ) {
                Icon(
                    painterResource(id = R.drawable.baseline_camera_alt_24),
                    contentDescription = "Tirar foto"
                )
            }
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .background(color = fundo)
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(200.dp)
                            .clickable {
                                checkStoragePermissionAndOpenGallery()
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(8.dp),
                        content = {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                if (galleryImageUri != null) {
                                    AsyncImage(
                                        model = galleryImageUri,
                                        contentDescription = "Imagem da Galeria",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.upload_img),
                                            contentDescription = "Upload",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(60.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Clique para enviar uma imagem", color = Color.Gray)
                                    }
                                }
                            }
                        }
                    )
                }

                // Preview da imagem selecionada
                if (showImagePreview && selectedImageUri != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f))
                            .clickable { showImagePreview = false },
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .height(350.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                AsyncImage(
                                    model = selectedImageUri,
                                    contentDescription = "Imagem para enviar",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .padding(8.dp)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    TextButton(
                                        onClick = { showImagePreview = false },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Cancelar", color = Color.Gray)
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Button(
                                        onClick = {
                                            selectedImageUri?.let { uri ->
                                                val bitmap = uriToBitmap(uri)
                                                if (bitmap != null) {
                                                    selectedBitmap = bitmap
                                                    showImagePreview = false
                                                    analyzeImage(bitmap)
                                                } else {
                                                    errorMessage = "Erro ao processar a imagem selecionada"
                                                    showErrorPopup = true
                                                    showImagePreview = false
                                                }
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xff1b2e3a)
                                        )
                                    ) {
                                        Text("Analisar", color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }

                // Popups
                if (showLoadingPopup) {
                    LoadingPopup(onDismiss = { })
                }

                if (showResultPopup && analysisResult != null) {
                    ResultPopup(
                        result = analysisResult!!,
                        onDismiss = { showResultPopup = false },
                        onSaveToHistory = {
                            analysisResult?.let { result ->
                                scope.launch {
                                    val novoHistorico = Historico(
                                        data = result.dataAnalise,
                                        hora = result.horaAnalise,
                                        status = result.status,
                                        similaridadePorcentagem = result.similaridadePorcentagem,
                                        descricaoPadrao = result.descricaoPadrao,
                                        imagemSimilar = "local_analysis",
                                        distanciaEuclidiana = result.distanciaEuclidiana
                                    )

                                    // Adiciona no estado local
                                    onAddHistorico(novoHistorico)

                                    // Salva no Firebase (opcional, não bloqueia a UI)
                                    try {
                                        firebaseService.saveHistorico(novoHistorico)
                                        Log.d("TelaPrincipal", "✅ Histórico salvo no Firebase")
                                    } catch (e: Exception) {
                                        Log.w("TelaPrincipal", "⚠️ Erro ao salvar no Firebase (continuando...)", e)
                                    }
                                }
                            }
                        }
                    )
                }

                if (showErrorPopup) {
                    ErrorPopup(
                        errorMessage = errorMessage,
                        onDismiss = { showErrorPopup = false }
                    )
                }
            }
        }
    )

    // ✅ Dialog de confirmação de logout
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = "Sair do aplicativo",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Tem certeza que deseja sair da sua conta?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            firebaseService.logoutUser()
                            showLogoutDialog = false
                            navController.navigate("TelaLogin") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF44336)
                    )
                ) {
                    Text("Sair", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar", color = Color(0xff1b2e3a))
                }
            },
            containerColor = Color.White
        )
    }
}