package com.example.engine

data class ModelInfo(
    val fileName: String,
    val filePath: String,
    val fileSizeBytes: Long,
    val modelFormat: String, // "GGUF", "TFLITE", "LITERT", "UNRECOGNIZED"
    val isLoaded: Boolean = false,
    val supportsTextInference: Boolean = false,
    val supportsVision: Boolean = false,
    val estimatedRamRequiredMb: Int = 0,
    val isCompatible: Boolean = false,
    val errorMessage: String? = null
) {
    val formattedSize: String
        get() {
            val mb = fileSizeBytes / (1024.0 * 1024.0)
            return if (mb >= 1024) {
                String.format("%.2f GB", mb / 1024.0)
            } else {
                String.format("%.1f MB", mb)
            }
        }
}
