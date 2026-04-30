package com.privacyguard.ml

/**
 * Explainable AI — Permutation Feature Importance.
 *
 * For each feature, we shuffle it (set to 0) and measure how much the
 * anomaly score drops. The bigger the drop, the more important that feature is.
 * This gives users a human-readable explanation of WHY an app is flagged.
 */
class ExplainableAI(private val detector: AnomalyDetector) {

    data class FeatureImportance(
        val featureName: String,
        val importance: Float,       // 0.0 – 1.0
        val percentage: Int          // 0 – 100
    )

    private val featureNames = listOf(
        "Audio Permission", "Camera Permission", "Location Permission",
        "Total Permissions", "Receivers", "Services",
        "Mic Usage", "Camera Usage", "Location Usage",
        "Network Traffic", "Night Activity", "Co-Triggers", "Keylogger Flag"
    )

    fun explain(features: FloatArray): List<FeatureImportance> {
        if (features.size != 13) return emptyList()

        val baselineScore = detector.score(features)
        if (baselineScore < 0.01f) return emptyList() // nothing to explain for safe apps

        val importances = mutableListOf<FeatureImportance>()

        for (i in features.indices) {
            // Create a copy with this feature zeroed out
            val perturbed = features.copyOf()
            perturbed[i] = 0f

            val perturbedScore = detector.score(perturbed)
            val drop = (baselineScore - perturbedScore).coerceAtLeast(0f)

            importances.add(
                FeatureImportance(
                    featureName = featureNames.getOrElse(i) { "Feature $i" },
                    importance = drop,
                    percentage = 0 // will be normalized below
                )
            )
        }

        // Normalize to percentages
        val totalImportance = importances.sumOf { it.importance.toDouble() }.toFloat()
        if (totalImportance == 0f) return emptyList()

        return importances.map { fi ->
            fi.copy(percentage = ((fi.importance / totalImportance) * 100).toInt())
        }.sortedByDescending { it.percentage }
    }
}
