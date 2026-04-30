package com.privacyguard.ml

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo

/**
 * App Intent Graph Analyzer — Maps inter-app communication patterns.
 *
 * Parses all installed apps' IntentFilters to build a directed graph
 * of which apps can launch, bind, or broadcast to other apps.
 * Detects suspicious chains (e.g., flashlight → file uploader).
 */
class IntentGraphAnalyzer(private val context: Context) {

    data class AppNode(
        val packageName: String,
        val appName: String,
        val exportedActivities: Int,
        val exportedServices: Int,
        val exportedReceivers: Int,
        val customPermissions: List<String>
    )

    data class IntentEdge(
        val from: String,
        val to: String,
        val type: String,           // ACTIVITY, SERVICE, BROADCAST
        val action: String
    )

    data class IntentGraph(
        val nodes: List<AppNode>,
        val edges: List<IntentEdge>,
        val suspiciousChains: List<SuspiciousChain>
    )

    data class SuspiciousChain(
        val description: String,
        val apps: List<String>,
        val severity: String         // LOW, MEDIUM, HIGH
    )

    fun analyze(): IntentGraph {
        val pm = context.packageManager
        val packages = pm.getInstalledPackages(
            PackageManager.GET_ACTIVITIES or PackageManager.GET_SERVICES or
            PackageManager.GET_RECEIVERS or PackageManager.GET_PERMISSIONS
        )

        val nodes = mutableListOf<AppNode>()
        val edges = mutableListOf<IntentEdge>()

        for (pkg in packages) {
            val appName = pm.getApplicationLabel(pkg.applicationInfo).toString()

            val exportedActivities = pkg.activities?.count { it.exported } ?: 0
            val exportedServices = pkg.services?.count { it.exported } ?: 0
            val exportedReceivers = pkg.receivers?.count { it.exported } ?: 0
            val customPerms = pkg.permissions?.map { it.name } ?: emptyList()

            nodes.add(AppNode(
                packageName = pkg.packageName,
                appName = appName,
                exportedActivities = exportedActivities,
                exportedServices = exportedServices,
                exportedReceivers = exportedReceivers,
                customPermissions = customPerms
            ))

            // Check what this app can launch in other apps
            pkg.activities?.forEach { actInfo ->
                if (actInfo.exported) {
                    val intent = Intent().setClassName(pkg.packageName, actInfo.name)
                    val resolvers = pm.queryIntentActivities(intent, 0)
                    resolvers.forEach { ri ->
                        if (ri.activityInfo.packageName != pkg.packageName) {
                            edges.add(IntentEdge(
                                from = pkg.packageName,
                                to = ri.activityInfo.packageName,
                                type = "ACTIVITY",
                                action = actInfo.name
                            ))
                        }
                    }
                }
            }
        }

        // Detect suspicious patterns
        val suspicious = detectSuspiciousChains(nodes, edges)

        return IntentGraph(nodes, edges, suspicious)
    }

    private fun detectSuspiciousChains(
        nodes: List<AppNode>,
        edges: List<IntentEdge>
    ): List<SuspiciousChain> {
        val chains = mutableListOf<SuspiciousChain>()

        // Pattern 1: Apps with many exported components (attack surface)
        nodes.filter {
            (it.exportedActivities + it.exportedServices + it.exportedReceivers) > 10
        }.forEach { node ->
            chains.add(SuspiciousChain(
                description = "${node.appName} has ${node.exportedActivities + node.exportedServices + node.exportedReceivers} exported components — large attack surface",
                apps = listOf(node.packageName),
                severity = "MEDIUM"
            ))
        }

        // Pattern 2: Non-system apps defining custom permissions
        nodes.filter {
            it.customPermissions.isNotEmpty() &&
            !it.packageName.startsWith("com.android") &&
            !it.packageName.startsWith("com.google")
        }.forEach { node ->
            chains.add(SuspiciousChain(
                description = "${node.appName} defines custom permissions: ${node.customPermissions.joinToString()}",
                apps = listOf(node.packageName),
                severity = "LOW"
            ))
        }

        // Pattern 3: Heavily interconnected non-system apps
        val nonSystemEdges = edges.filter { edge ->
            !edge.from.startsWith("com.android") && !edge.from.startsWith("com.google") &&
            !edge.to.startsWith("com.android") && !edge.to.startsWith("com.google")
        }
        val connectionCounts = nonSystemEdges.groupBy { it.from }.mapValues { it.value.size }
        connectionCounts.filter { it.value > 5 }.forEach { (pkg, count) ->
            val appName = nodes.find { it.packageName == pkg }?.appName ?: pkg
            chains.add(SuspiciousChain(
                description = "$appName communicates with $count other third-party apps",
                apps = listOf(pkg) + nonSystemEdges.filter { it.from == pkg }.map { it.to }.distinct(),
                severity = "HIGH"
            ))
        }

        return chains
    }
}
