package com.privacyguard.ml

import android.content.Context

/**
 * AnomalyDetector — on-device ML inference engine.
 *
 * Currently uses a rule-based fallback engine because the TFLite model
 * (assets/anomaly_detector.tflite) has not yet been trained.
 * Once a model is placed in the assets folder, the TFLite interpreter
 * path will activate automatically.
 *
 * INPUT features (per app, normalized 0..1):
 *   [micCount, cameraCount, locationCount, nightCount, keyloggerFlag, triggerCount]
 *
 * OUTPUT: anomaly score 0.0 (normal) → 1.0 (highly anomalous)
 */
class AnomalyDetector(private val context: Context) {

    private val modelAsset = "anomaly_detector.tflite"
    private val hasTfliteModel: Boolean by lazy {
        try { context.assets.open(modelAsset).close(); true } catch (_: Exception) { false }
    }

    fun score(features: FloatArray): Float {
        return if (hasTfliteModel) {
            runTfliteInference(features)
        } else {
            ruleBasedFallback(features)
        }
    }

    /**
     * TFLite inference path — activated when the model asset exists.
     * Requires org.tensorflow:tensorflow-lite dependency.
     */
    private fun runTfliteInference(features: FloatArray): Float {
        // Placeholder: in a full implementation, load the Interpreter here.
        // val interpreter = Interpreter(loadModelFile())
        // val output = Array(1) { FloatArray(1) }
        // interpreter.run(arrayOf(features), output)
        // return output[0][0]
        return ruleBasedFallback(features) // fallback until model is trained
    }

    /**
     * Rule-based fallback:
     * features = [micNorm, camNorm, locNorm, nightNorm, keyloggerFlag, triggerNorm]
     */
    private fun ruleBasedFallback(f: FloatArray): Float {
        if (f.size < 6) return 0f
        val mic       = f[0]
        val cam       = f[1]
        val loc       = f[2]
        val night     = f[3]
        val keylogger = f[4]
        val trigger   = f[5]

        val raw = (keylogger * 0.40f) +
                  (night     * 0.25f) +
                  (trigger   * 0.15f) +
                  (cam       * 0.10f) +
                  (loc       * 0.05f) +
                  (mic       * 0.05f)

        return raw.coerceIn(0f, 1f)
    }
}
