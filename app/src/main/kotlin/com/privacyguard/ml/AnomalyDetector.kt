package com.privacyguard.ml

import android.content.Context
import org.json.JSONObject
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

/**
 * AnomalyDetector — on-device ML inference engine using TensorFlow Lite.
 *
 * INPUT features (13 total):
 *   [perm_RECORD_AUDIO, perm_CAMERA, perm_ACCESS_FINE_LOCATION, total_permissions,
 *    receivers_count, services_count, audio_record_starts, camera_opens,
 *    location_requests, network_bytes_sent, night_activity_count, trigger_count, is_keylogger]
 */
class AnomalyDetector(private val context: Context) {

    private val modelAsset = "spyware_detector.tflite"
    private val scalerAsset = "scaler_params.json"
    
    private var interpreter: Interpreter? = null
    private var scalerMean: FloatArray? = null
    private var scalerScale: FloatArray? = null

    init {
        try {
            interpreter = Interpreter(loadModelFile())
            loadScalerParams()
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback will be used if model/scaler fails to load
        }
    }

    private fun loadModelFile(): ByteBuffer {
        val fileDescriptor = context.assets.openFd(modelAsset)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun loadScalerParams() {
        val jsonString = context.assets.open(scalerAsset).bufferedReader().use { it.readText() }
        val jsonObject = JSONObject(jsonString)
        val meanArray = jsonObject.getJSONArray("mean")
        val scaleArray = jsonObject.getJSONArray("scale")
        
        scalerMean = FloatArray(meanArray.length()) { meanArray.getDouble(it).toFloat() }
        scalerScale = FloatArray(scaleArray.length()) { scaleArray.getDouble(it).toFloat() }
    }

    fun score(features: FloatArray): Float {
        // Must match 13 features exactly
        if (features.size != 13) return ruleBasedFallback(features)

        val tflite = interpreter
        val mean = scalerMean
        val scale = scalerScale
        
        return if (tflite != null && mean != null && scale != null) {
            // Apply StandardScaler: (x - mean) / scale
            val scaledFeatures = FloatArray(13)
            for (i in 0 until 13) {
                val scaleVal = if (scale[i] == 0f) 1f else scale[i]
                scaledFeatures[i] = (features[i] - mean[i]) / scaleVal
            }

            // Run inference
            val input = Array(1) { scaledFeatures }
            val output = Array(1) { FloatArray(1) }
            
            try {
                tflite.run(input, output)
                output[0][0].coerceIn(0f, 1f)
            } catch (e: Exception) {
                e.printStackTrace()
                ruleBasedFallback(features)
            }
        } else {
            ruleBasedFallback(features)
        }
    }

    private fun ruleBasedFallback(f: FloatArray): Float {
        if (f.size < 13) return 0f
        // Heuristic fallback using some key features
        val kl = f[12]
        val night = f[10] / 10f
        val trigger = f[11] / 5f
        val cam = f[7] / 20f
        val mic = f[6] / 20f
        val loc = f[8] / 20f

        val raw = (kl * 0.40f) +
                  (night * 0.25f) +
                  (trigger * 0.15f) +
                  (cam * 0.10f) +
                  (loc * 0.05f) +
                  (mic * 0.05f)

        return raw.coerceIn(0f, 1f)
    }
    
    fun close() {
        interpreter?.close()
    }
}
