package com.reelrecall.app.util

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import java.io.File
import java.util.Locale
import kotlin.math.roundToInt

data class DeviceHardwareInfo(
    val processorName: String,
    val coreCount: Int,
    val totalRamGb: Int,
    val ramDisplay: String,
    val totalRomGb: Int,
    val storageDisplay: String,
    val npuAcceleratorStatus: String,
    val architecture: String
)

object DeviceHardwareHelper {

    fun getHardwareInfo(context: Context): DeviceHardwareInfo {
        val cores = Runtime.getRuntime().availableProcessors()
        val arch = Build.SUPPORTED_ABIS.firstOrNull() ?: System.getProperty("os.arch") ?: "arm64-v8a"
        val processorName = resolveProcessorName()
        val ramData = resolveRam(context)
        val storageData = resolveStorage()
        val npuStatus = resolveNpuStatus(context, cores)

        return DeviceHardwareInfo(
            processorName = processorName,
            coreCount = cores,
            totalRamGb = ramData.first,
            ramDisplay = ramData.second,
            totalRomGb = storageData.first,
            storageDisplay = storageData.second,
            npuAcceleratorStatus = npuStatus,
            architecture = arch
        )
    }

    private fun resolveProcessorName(): String {
        var soc = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Build.SOC_MODEL
        } else {
            ""
        }
        if (soc.isNullOrBlank() || soc.equals("unknown", ignoreCase = true)) {
            soc = Build.HARDWARE ?: ""
        }
        if (soc.isBlank() || soc.equals("unknown", ignoreCase = true)) {
            soc = Build.BOARD ?: ""
        }

        // Try reading /proc/cpuinfo if available
        val cpuInfoHardware = readCpuInfoHardware()
        val activeIdentifier = if (soc.isNotBlank() && !soc.equals("unknown", ignoreCase = true)) soc else cpuInfoHardware

        return mapSocToMarketingName(activeIdentifier)
    }

    private fun mapSocToMarketingName(rawId: String): String {
        val clean = rawId.trim()
        val lower = clean.lowercase(Locale.ROOT)

        return when {
            // Samsung Exynos
            lower.contains("s5e9965") || lower.contains("erd9965") -> "Samsung Exynos 2500"
            lower.contains("s5e9945") || lower.contains("erd9945") -> "Samsung Exynos 2400"
            lower.contains("s5e9925") -> "Samsung Exynos 2200"
            lower.contains("s5e9840") -> "Samsung Exynos 2100"
            lower.contains("s5e8845") -> "Samsung Exynos 1480"
            lower.contains("s5e8835") -> "Samsung Exynos 1380"
            lower.startsWith("s5e") || lower.startsWith("universal") -> "Samsung Exynos ($clean)"

            // Qualcomm Snapdragon
            lower.contains("sm8750") || lower.contains("sun") -> "Snapdragon 8 Elite"
            lower.contains("sm8650") || lower.contains("pineapple") -> "Snapdragon 8 Gen 3"
            lower.contains("sm8550") || lower.contains("kalama") -> "Snapdragon 8 Gen 2"
            lower.contains("sm8475") -> "Snapdragon 8+ Gen 1"
            lower.contains("sm8450") || lower.contains("taro") -> "Snapdragon 8 Gen 1"
            lower.contains("sm7675") -> "Snapdragon 7+ Gen 3"
            lower.contains("sm7550") -> "Snapdragon 7 Gen 3"
            lower.startsWith("sm") || lower.contains("qcom") || lower.contains("qualcomm") -> "Qualcomm Snapdragon ($clean)"

            // Google Tensor
            lower.contains("zuma_pro") || lower.contains("g4") -> "Google Tensor G4"
            lower.contains("zuma") || lower.contains("g3") -> "Google Tensor G3"
            lower.contains("cloudripper") || lower.contains("g2") -> "Google Tensor G2"
            lower.contains("gs101") || lower.contains("whitechapel") -> "Google Tensor"

            // MediaTek Dimensity
            lower.contains("mt6991") -> "MediaTek Dimensity 9400"
            lower.contains("mt6989") -> "MediaTek Dimensity 9300"
            lower.contains("mt6985") -> "MediaTek Dimensity 9200"
            lower.startsWith("mt6") || lower.contains("dimensity") -> "MediaTek Dimensity ($clean)"

            clean.isNotBlank() && !clean.equals("unknown", ignoreCase = true) -> {
                val manufacturer = Build.MANUFACTURER.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
                "$manufacturer $clean"
            }
            else -> {
                val manufacturer = Build.MANUFACTURER.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
                "$manufacturer ${Build.MODEL}"
            }
        }
    }

    private fun readCpuInfoHardware(): String {
        return try {
            val file = File("/proc/cpuinfo")
            if (file.exists() && file.canRead()) {
                file.useLines { lines ->
                    for (line in lines) {
                        if (line.startsWith("Hardware", ignoreCase = true) || line.startsWith("model name", ignoreCase = true)) {
                            val parts = line.split(":")
                            if (parts.size > 1 && parts[1].isNotBlank()) {
                                return parts[1].trim()
                            }
                        }
                    }
                }
            }
            ""
        } catch (e: Exception) {
            ""
        }
    }

    private fun resolveRam(context: Context): Pair<Int, String> {
        return try {
            val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            actManager?.getMemoryInfo(memInfo)

            val totalBytes = memInfo.totalMem
            val availBytes = memInfo.availMem

            val ramGbExact = totalBytes / (1024.0 * 1024 * 1024.0)
            val ramTier = when {
                ramGbExact <= 4.2 -> 4
                ramGbExact <= 6.3 -> 6
                ramGbExact <= 8.4 -> 8
                ramGbExact <= 12.5 -> 12
                ramGbExact <= 16.5 -> 16
                ramGbExact <= 24.5 -> 24
                else -> ramGbExact.roundToInt()
            }
            val availGb = String.format(Locale.US, "%.1f", availBytes / (1024.0 * 1024 * 1024.0))
            Pair(ramTier, "${ramTier}GB RAM (${availGb}GB Free)")
        } catch (e: Exception) {
            Pair(8, "8GB RAM")
        }
    }

    private fun resolveStorage(): Pair<Int, String> {
        return try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val totalBytes = stat.totalBytes
            val availBytes = stat.availableBytes

            val totalGbExact = totalBytes / (1000.0 * 1000 * 1000.0)
            val storageTier = when {
                totalGbExact <= 68.0 -> 64
                totalGbExact <= 136.0 -> 128
                totalGbExact <= 270.0 -> 256
                totalGbExact <= 540.0 -> 512
                totalGbExact <= 1080.0 -> 1024
                else -> totalGbExact.roundToInt()
            }

            val availGb = (availBytes / (1024.0 * 1024 * 1024.0)).roundToInt()
            Pair(storageTier, "${storageTier}GB ROM (${availGb}GB Free)")
        } catch (e: Exception) {
            Pair(128, "128GB ROM")
        }
    }

    private fun resolveNpuStatus(context: Context, cores: Int): String {
        return try {
            val pm = context.packageManager
            val hasVulkan = pm.hasSystemFeature("android.hardware.vulkan.compute")
            val hasAiCore = pm.hasSystemFeature("com.google.android.feature.AICORE_SLSI") ||
                    pm.hasSystemFeature("com.google.android.feature.AICORE_QUALCOMM") ||
                    pm.hasSystemFeature("android.hardware.neuralnetworks")

            when {
                hasAiCore && hasVulkan -> "NPU / AI Engine & Vulkan ($cores Cores)"
                hasAiCore -> "Hexagon / NPU Accelerated ($cores Cores)"
                hasVulkan -> "Vulkan Vector Compute ($cores Cores)"
                else -> "Multi-Core CPU ($cores Cores)"
            }
        } catch (e: Exception) {
            "Hardware Accelerated ($cores Cores)"
        }
    }
}
