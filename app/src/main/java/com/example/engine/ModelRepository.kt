package com.example.engine

import android.app.ActivityManager
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class ModelRepository(private val context: Context) {

    private val _currentModel = MutableStateFlow<ModelInfo?>(null)
    val currentModel: StateFlow<ModelInfo?> = _currentModel.asStateFlow()

    init {
        // Check if previously saved model exists in app storage
        val modelsDir = File(context.filesDir, "models")
        if (modelsDir.exists()) {
            val files = modelsDir.listFiles()
            if (!files.isNullOrEmpty()) {
                val file = files.first()
                val format = detectFormat(file.name)
                val isCompatible = format in listOf("GGUF", "TFLITE", "LITERT")
                _currentModel.value = ModelInfo(
                    fileName = file.name,
                    filePath = file.absolutePath,
                    fileSizeBytes = file.length(),
                    modelFormat = format,
                    isLoaded = false,
                    supportsTextInference = isCompatible,
                    supportsVision = file.name.contains("vision", ignoreCase = true) || file.name.contains("vl", ignoreCase = true),
                    estimatedRamRequiredMb = (file.length() / (1024 * 1024) * 1.3).toInt(),
                    isCompatible = isCompatible
                )
            }
        }
    }

    fun getDeviceMemoryInfo(): Pair<Long, Long> {
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager.getMemoryInfo(memInfo)
        val availMb = memInfo.availMem / (1024 * 1024)
        val totalMb = memInfo.totalMem / (1024 * 1024)
        return Pair(availMb, totalMb)
    }

    suspend fun importModelFromUri(uri: Uri): Result<ModelInfo> = withContext(Dispatchers.IO) {
        try {
            var fileName = "imported_model"
            var fileSize = 0L

            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex != -1) fileName = cursor.getString(nameIndex) ?: fileName
                    if (sizeIndex != -1) fileSize = cursor.getLong(sizeIndex)
                }
            }

            val format = detectFormat(fileName)
            val isFormatRecognized = format in listOf("GGUF", "TFLITE", "LITERT")

            val modelsDir = File(context.filesDir, "models")
            if (!modelsDir.exists()) modelsDir.mkdirs()

            val destFile = File(modelsDir, fileName)
            // Copy stream to app-private storage safely
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }

            val finalSize = destFile.length()
            val (availMb, _) = getDeviceMemoryInfo()
            val estRam = (finalSize / (1024 * 1024) * 1.35).toInt()

            val errorMessage = when {
                !isFormatRecognized -> "Unrecognized model format. AI BREAK supports .gguf, .tflite, or .litert weight containers."
                estRam > availMb -> "Warning: Model estimated RAM (${estRam}MB) exceeds currently available system RAM (${availMb}MB)."
                else -> null
            }

            val modelInfo = ModelInfo(
                fileName = fileName,
                filePath = destFile.absolutePath,
                fileSizeBytes = finalSize,
                modelFormat = format,
                isLoaded = false,
                supportsTextInference = isFormatRecognized,
                supportsVision = fileName.contains("vision", ignoreCase = true) || fileName.contains("vl", ignoreCase = true),
                estimatedRamRequiredMb = estRam,
                isCompatible = isFormatRecognized,
                errorMessage = errorMessage
            )

            _currentModel.value = modelInfo
            Result.success(modelInfo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun loadModel(): Boolean {
        val current = _currentModel.value ?: return false
        if (!current.isCompatible) return false
        _currentModel.value = current.copy(isLoaded = true)
        return true
    }

    fun unloadModel() {
        val current = _currentModel.value ?: return
        _currentModel.value = current.copy(isLoaded = false)
    }

    fun removeModel(): Boolean {
        val current = _currentModel.value ?: return false
        try {
            val file = File(current.filePath)
            if (file.exists()) file.delete()
            _currentModel.value = null
            return true
        } catch (e: Exception) {
            return false
        }
    }

    private fun detectFormat(name: String): String {
        val lower = name.lowercase()
        return when {
            lower.endsWith(".gguf") -> "GGUF"
            lower.endsWith(".tflite") -> "TFLITE"
            lower.endsWith(".litert") || lower.endsWith(".bin") -> "LITERT"
            lower.endsWith(".onnx") -> "ONNX"
            else -> "UNRECOGNIZED"
        }
    }
}
