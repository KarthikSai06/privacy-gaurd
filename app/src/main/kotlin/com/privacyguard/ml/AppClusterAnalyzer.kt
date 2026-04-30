package com.privacyguard.ml

import kotlin.math.sqrt

/**
 * App Behavior Clustering — K-Means implementation.
 *
 * Clusters apps into behavioral groups based on their privacy feature vectors.
 * Runs entirely on-device without any external dependencies.
 */
class AppClusterAnalyzer {

    data class Cluster(
        val id: Int,
        val label: String,
        val centroid: FloatArray,
        val members: List<String>    // package names
    )

    /**
     * Run K-Means clustering on app feature vectors.
     * @param features Map of packageName -> normalized feature vector
     * @param k Number of clusters (default 4)
     * @param maxIterations Maximum iterations for convergence
     */
    fun cluster(
        features: Map<String, FloatArray>,
        k: Int = 4,
        maxIterations: Int = 50
    ): List<Cluster> {
        if (features.size < k) return emptyList()

        val packages = features.keys.toList()
        val vectors = features.values.toList()
        val dim = vectors[0].size

        // Initialize centroids using K-Means++ style (pick spread out points)
        val centroids = mutableListOf<FloatArray>()
        centroids.add(vectors.random().copyOf())

        for (i in 1 until k) {
            val distances = vectors.map { v ->
                centroids.minOf { c -> euclideanDist(v, c) }
            }
            val totalDist = distances.sum()
            if (totalDist == 0f) {
                centroids.add(vectors[i].copyOf())
                continue
            }
            // Weighted random selection
            var target = (Math.random() * totalDist).toFloat()
            for (j in distances.indices) {
                target -= distances[j]
                if (target <= 0) {
                    centroids.add(vectors[j].copyOf())
                    break
                }
            }
            if (centroids.size <= i) centroids.add(vectors.random().copyOf())
        }

        // Iterate
        var assignments = IntArray(vectors.size)
        for (iter in 0 until maxIterations) {
            // Assign each point to nearest centroid
            val newAssignments = IntArray(vectors.size) { i ->
                centroids.indices.minByOrNull { c -> euclideanDist(vectors[i], centroids[c]) } ?: 0
            }

            // Check convergence
            if (newAssignments.contentEquals(assignments) && iter > 0) break
            assignments = newAssignments

            // Update centroids
            for (c in 0 until k) {
                val clusterMembers = vectors.indices.filter { assignments[it] == c }
                if (clusterMembers.isEmpty()) continue
                for (d in 0 until dim) {
                    centroids[c][d] = clusterMembers.map { vectors[it][d] }.average().toFloat()
                }
            }
        }

        // Build cluster objects with labels
        return (0 until k).map { c ->
            val memberIndices = assignments.indices.filter { assignments[it] == c }
            val memberPackages = memberIndices.map { packages[it] }

            Cluster(
                id = c,
                label = labelCluster(centroids[c]),
                centroid = centroids[c],
                members = memberPackages
            )
        }.filter { it.members.isNotEmpty() }
    }

    private fun labelCluster(centroid: FloatArray): String {
        if (centroid.size < 13) return "Unknown"

        val permScore = (centroid[0] + centroid[1] + centroid[2]) / 3f
        val sensorScore = (centroid[6] + centroid[7] + centroid[8]) / 3f
        val nightScore = centroid[10]
        val keyloggerScore = centroid[12]
        val networkScore = centroid[9]

        return when {
            keyloggerScore > 0.5f && nightScore > 0.3f -> "🔴 Potential Stalkerware"
            sensorScore > 0.5f && networkScore > 0.5f -> "🟠 High Sensor + Network"
            permScore > 0.6f && sensorScore < 0.2f -> "🟡 Permission Heavy"
            sensorScore < 0.1f && networkScore < 0.1f -> "🟢 Low Risk Utilities"
            else -> "🔵 Moderate Activity"
        }
    }

    private fun euclideanDist(a: FloatArray, b: FloatArray): Float {
        var sum = 0f
        for (i in a.indices) {
            val diff = a[i] - (b.getOrElse(i) { 0f })
            sum += diff * diff
        }
        return sqrt(sum)
    }
}
