package com.example.appfinal.ML

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import kotlin.math.*

data class MLResult(
    val success: Boolean,
    val tendencia: String = "",
    val status: Boolean = false,
    val similaridadePorcentagem: Double = 0.0,
    val descricaoPadrao: String = "",
    val distanciaEuclidiana: Double = 0.0,
    val dataAnalise: String = "",
    val horaAnalise: String = "",
    val error: String = ""
)

class SimplifiedMLAnalyzer(private val context: Context) {

    private var interpreter: Interpreter? = null
    private var datasetFeatures: List<FloatArray> = emptyList()
    private var datasetLabels: List<String> = emptyList()
    private var datasetDescriptions: List<String> = emptyList()
    private var isInitialized = false

    suspend fun initialize(): Boolean = withContext(Dispatchers.IO) {
        try {
            if (isInitialized) return@withContext true

            // Carrega features do dataset
            loadDatasetFromAssets()

            isInitialized = true
            android.util.Log.d("MLAnalyzer", "ML Inicializado com ${datasetFeatures.size} referências")
            true
        } catch (e: Exception) {
            android.util.Log.e("MLAnalyzer", "Erro na inicialização", e)
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun analyzeImage(bitmap: Bitmap): MLResult = withContext(Dispatchers.IO) {
        if (!isInitialized) {
            initialize()
            if (!isInitialized) {
                return@withContext MLResult(success = false, error = "ML não foi inicializado")
            }
        }

        try {
            val features = extractFeatures(bitmap)
            val result = compareWithDataset(features)

            android.util.Log.d("MLAnalyzer", "✅ Análise concluída: ${result.tendencia}")
            result

        } catch (e: Exception) {
            android.util.Log.e("MLAnalyzer", "Erro na análise", e)
            MLResult(success = false, error = e.message ?: "Erro desconhecido")
        }
    }

    private fun loadDatasetFromAssets() {
        try {
            val mockFeatures = listOf(
                generateMockFeatures("ALTA", 0.8f),
                generateMockFeatures("ALTA", 0.85f),
                generateMockFeatures("ALTA", 0.9f),
                generateMockFeatures("BAIXA", 0.2f),
                generateMockFeatures("BAIXA", 0.15f),
                generateMockFeatures("BAIXA", 0.1f)
            )

            val mockLabels = listOf("UP", "UP", "UP", "DOWN", "DOWN", "DOWN")
            val mockDescriptions = listOf(
                "Padrão de ALTA - Tendência Crescente",
                "Padrão de ALTA - Forte Crescimento",
                "Padrão de ALTA - Pico Ascendente",
                "Padrão de BAIXA - Tendência Decrescente",
                "Padrão de BAIXA - Forte Queda",
                "Padrão de BAIXA - Vale Descendente"
            )

            datasetFeatures = mockFeatures
            datasetLabels = mockLabels
            datasetDescriptions = mockDescriptions

        } catch (e: Exception) {
            android.util.Log.e("MLAnalyzer", "Erro ao carregar dataset", e)
        }
    }

    private fun generateMockFeatures(type: String, intensity: Float): FloatArray {
        val size = 128
        return FloatArray(size) { i ->
            when (type) {
                "ALTA" -> (intensity + (i.toFloat() / size) * 0.3f + (Math.random() * 0.1f).toFloat())
                "BAIXA" -> (intensity - (i.toFloat() / size) * 0.3f + (Math.random() * 0.1f).toFloat())
                else -> Math.random().toFloat()
            }
        }
    }

    private fun extractFeatures(bitmap: Bitmap): FloatArray {
        // 🔒 Converte caso seja Config.HARDWARE
        val safeBitmap = if (bitmap.config == Bitmap.Config.HARDWARE) {
            bitmap.copy(Bitmap.Config.ARGB_8888, false)
        } else {
            bitmap
        }

        // Redimensiona
        val resizedBitmap = Bitmap.createScaledBitmap(safeBitmap, 128, 128, true)

        val pixels = IntArray(128 * 128)
        resizedBitmap.getPixels(pixels, 0, 128, 0, 0, 128, 128)

        var upperBrightness = 0f
        var lowerBrightness = 0f
        var centerBrightness = 0f

        for (y in 0 until 128) {
            for (x in 0 until 128) {
                val pixel = pixels[y * 128 + x]
                val brightness = (
                        ((pixel shr 16) and 0xFF) * 0.299f +
                                ((pixel shr 8) and 0xFF) * 0.587f +
                                (pixel and 0xFF) * 0.114f
                        ) / 255f

                when {
                    y < 42 -> upperBrightness += brightness
                    y > 85 -> lowerBrightness += brightness
                    else -> centerBrightness += brightness
                }
            }
        }

        upperBrightness /= (42 * 128)
        lowerBrightness /= (42 * 128)
        centerBrightness /= (43 * 128)

        return FloatArray(128) { i ->
            when {
                i < 43 -> upperBrightness + (Math.random() * 0.1f).toFloat()
                i < 86 -> centerBrightness + (Math.random() * 0.1f).toFloat()
                else -> lowerBrightness + (Math.random() * 0.1f).toFloat()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun compareWithDataset(testFeatures: FloatArray): MLResult {
        if (datasetFeatures.isEmpty()) {
            return MLResult(success = false, error = "Dataset não carregado")
        }

        var minDistance = Double.MAX_VALUE
        var bestMatchIndex = -1

        for (i in datasetFeatures.indices) {
            val distance = euclideanDistance(testFeatures, datasetFeatures[i])
            if (distance < minDistance) {
                minDistance = distance
                bestMatchIndex = i
            }
        }

        if (bestMatchIndex == -1) {
            return MLResult(success = false, error = "Nenhuma correspondência encontrada")
        }

        val maxDistance = sqrt(testFeatures.size.toDouble()) * 2.0
        val normalizedDistance = minDistance / maxDistance
        val similarity = max(0.0, min(100.0, (1 - normalizedDistance) * 100))

        val label = datasetLabels[bestMatchIndex]
        val status = label.contains("UP", ignoreCase = true)
        val tendencia = if (status) "ALTA" else "BAIXA"

        val now = java.time.LocalDateTime.now()
        val data = now.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val hora = now.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))

        return MLResult(
            success = true,
            tendencia = tendencia,
            status = status,
            similaridadePorcentagem = similarity,
            descricaoPadrao = datasetDescriptions[bestMatchIndex],
            distanciaEuclidiana = minDistance,
            dataAnalise = data,
            horaAnalise = hora
        )
    }

    private fun euclideanDistance(a: FloatArray, b: FloatArray): Double {
        if (a.size != b.size) return Double.MAX_VALUE
        var sum = 0.0
        for (i in a.indices) {
            val diff = (a[i] - b[i]).toDouble()
            sum += diff * diff
        }
        return sqrt(sum)
    }
}
