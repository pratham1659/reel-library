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
    val deviceName: String = "",
    val processorName: String,
    val coreCount: Int,
    val totalRamGb: Int,
    val ramDisplay: String,
    val totalRomGb: Int,
    val storageDisplay: String,
    val npuAcceleratorStatus: String,
    val architecture: String,
    val hasNpu: Boolean = true,
    val hasVulkan: Boolean = true,
    val npuVendorName: String = "Neural Processing Unit (NPU)"
)

object DeviceHardwareHelper {

    data class NpuCapabilities(
        val status: String,
        val hasNpu: Boolean,
        val hasVulkan: Boolean,
        val npuVendorName: String
    )

    fun getHardwareInfo(context: Context): DeviceHardwareInfo {
        val cores = Runtime.getRuntime().availableProcessors()
        val arch = Build.SUPPORTED_ABIS.firstOrNull() ?: System.getProperty("os.arch") ?: "arm64-v8a"
        val deviceName = resolveDeviceName()
        val processorName = resolveProcessorName()
        val ramData = resolveRam(context)
        val storageData = resolveStorage()
        val npuCaps = resolveNpuCapabilities(context, cores, processorName)

        return DeviceHardwareInfo(
            deviceName = deviceName,
            processorName = processorName,
            coreCount = cores,
            totalRamGb = ramData.first,
            ramDisplay = ramData.second,
            totalRomGb = storageData.first,
            storageDisplay = storageData.second,
            npuAcceleratorStatus = npuCaps.status,
            architecture = arch,
            hasNpu = npuCaps.hasNpu,
            hasVulkan = npuCaps.hasVulkan,
            npuVendorName = npuCaps.npuVendorName
        )
    }

    fun resolveDeviceName(): String {
        val model = Build.MODEL ?: ""
        val brand = Build.MANUFACTURER.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
        val cleanModel = model.trim().uppercase(Locale.ROOT)

        val marketingModel = when {
            // Samsung Galaxy S series
            cleanModel.startsWith("SM-S948") -> "Galaxy S26 Ultra"
            cleanModel.startsWith("SM-S946") -> "Galaxy S26+"
            cleanModel.startsWith("SM-S941") || cleanModel.startsWith("SM-S942") -> "Galaxy S26"
            cleanModel.startsWith("SM-S938") -> "Galaxy S25 Ultra"
            cleanModel.startsWith("SM-S936") -> "Galaxy S25+"
            cleanModel.startsWith("SM-S931") -> "Galaxy S25"
            cleanModel.startsWith("SM-S928") -> "Galaxy S24 Ultra"
            cleanModel.startsWith("SM-S926") -> "Galaxy S24+"
            cleanModel.startsWith("SM-S921") -> "Galaxy S24"
            cleanModel.startsWith("SM-S721") || cleanModel.startsWith("SM-S711") -> "Galaxy S24 FE"
            cleanModel.startsWith("SM-S918") -> "Galaxy S23 Ultra"
            cleanModel.startsWith("SM-S916") -> "Galaxy S23+"
            cleanModel.startsWith("SM-S911") -> "Galaxy S23"
            cleanModel.startsWith("SM-S908") -> "Galaxy S22 Ultra"
            cleanModel.startsWith("SM-S906") -> "Galaxy S22+"
            cleanModel.startsWith("SM-S901") -> "Galaxy S22"
            cleanModel.startsWith("SM-G998") -> "Galaxy S21 Ultra"
            cleanModel.startsWith("SM-G996") -> "Galaxy S21+"
            cleanModel.startsWith("SM-G991") -> "Galaxy S21"
            cleanModel.startsWith("SM-G990") -> "Galaxy S21 FE"

            // Samsung Galaxy Z Fold & Flip
            cleanModel.startsWith("SM-F956") -> "Galaxy Z Fold6"
            cleanModel.startsWith("SM-F741") -> "Galaxy Z Flip6"
            cleanModel.startsWith("SM-F946") -> "Galaxy Z Fold5"
            cleanModel.startsWith("SM-F731") -> "Galaxy Z Flip5"
            cleanModel.startsWith("SM-F936") -> "Galaxy Z Fold4"
            cleanModel.startsWith("SM-F721") -> "Galaxy Z Flip4"
            cleanModel.startsWith("SM-F926") -> "Galaxy Z Fold3"
            cleanModel.startsWith("SM-F711") -> "Galaxy Z Flip3"

            // Samsung Galaxy A series
            cleanModel.startsWith("SM-A556") -> "Galaxy A55 5G"
            cleanModel.startsWith("SM-A546") -> "Galaxy A54 5G"
            cleanModel.startsWith("SM-A536") -> "Galaxy A53 5G"
            cleanModel.startsWith("SM-A356") -> "Galaxy A35 5G"
            cleanModel.startsWith("SM-A346") -> "Galaxy A34 5G"
            cleanModel.startsWith("SM-A256") -> "Galaxy A25 5G"
            cleanModel.startsWith("SM-A156") -> "Galaxy A15 5G"
            cleanModel.startsWith("SM-A146") -> "Galaxy A14 5G"
            cleanModel.startsWith("SM-") -> "Galaxy ${model.removePrefix("SM-")}"

            // Google Pixel devices
            cleanModel.startsWith("PIXEL") -> model
            cleanModel.contains("PIXEL") -> model

            // Any non-empty model
            model.isNotBlank() && !model.equals("unknown", ignoreCase = true) -> model
            else -> "$brand Device"
        }

        return if (brand.isNotBlank() && !marketingModel.startsWith(brand, ignoreCase = true)) {
            "$brand $marketingModel"
        } else {
            marketingModel
        }
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
            lower.contains("s5e9965") || lower.contains("erd9965") || lower.contains("ex2600") -> "Samsung Exynos 2600"
            lower.contains("s5e9955") || lower.contains("erd9955") -> "Samsung Exynos 2500"
            lower.contains("s5e9945") || lower.contains("erd9945") -> "Samsung Exynos 2400"
            lower.contains("s5e9925") -> "Samsung Exynos 2200"
            lower.contains("s5e9840") -> "Samsung Exynos 2100"
            lower.contains("s5e8845") -> "Samsung Exynos 1480"
            lower.contains("s5e8835") -> "Samsung Exynos 1380"
            lower.contains("s5e8825") -> "Samsung Exynos 1280"
            lower.contains("s5e9830") -> "Samsung Exynos 990"
            lower.contains("s5e9820") -> "Samsung Exynos 9820"
            lower.startsWith("s5e") || lower.startsWith("universal") -> "Samsung Exynos ($clean)"

            // Qualcomm Snapdragon Flagship & Upper-Midrange
            lower.contains("sm8750") || lower.contains("sun") -> "Snapdragon 8 Elite"
            lower.contains("sm8650") || lower.contains("pineapple") -> "Snapdragon 8 Gen 3"
            lower.contains("sm8550") || lower.contains("kalama") -> "Snapdragon 8 Gen 2"
            lower.contains("sm8475") -> "Snapdragon 8+ Gen 1"
            lower.contains("sm8450") || lower.contains("taro") -> "Snapdragon 8 Gen 1"
            lower.contains("sm8350") || lower.contains("lahaina") -> "Snapdragon 888"
            lower.contains("sm8250") || lower.contains("kona") -> "Snapdragon 865/870"
            lower.contains("sm7675") -> "Snapdragon 7+ Gen 3"
            lower.contains("sm7550") -> "Snapdragon 7 Gen 3"
            lower.contains("sm7475") -> "Snapdragon 7+ Gen 2"
            lower.contains("sm7450") -> "Snapdragon 7 Gen 1"
            lower.contains("sm7325") || lower.contains("yupik") -> "Snapdragon 778G"
            lower.contains("sm6450") -> "Snapdragon 6 Gen 1"
            lower.contains("sm4450") -> "Snapdragon 4 Gen 2"
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
            lower.contains("mt6983") -> "MediaTek Dimensity 9000"
            lower.contains("mt6897") -> "MediaTek Dimensity 8300"
            lower.contains("mt6896") -> "MediaTek Dimensity 8200"
            lower.contains("mt6886") -> "MediaTek Dimensity 7200"
            lower.contains("mt6877") -> "MediaTek Dimensity 7050"
            lower.contains("mt6789") -> "MediaTek Helio G99"
            lower.startsWith("mt6") || lower.contains("dimensity") -> "MediaTek Dimensity ($clean)"
            lower.startsWith("mt67") || lower.contains("helio") -> "MediaTek Helio ($clean)"

            // Unisoc
            lower.contains("ums9230") || lower.contains("t606") -> "Unisoc T606"
            lower.contains("ums9230t") || lower.contains("t616") -> "Unisoc T616"
            lower.contains("ums9620") || lower.contains("t820") -> "Unisoc T820"
            lower.startsWith("ums") || lower.contains("unisoc") || lower.contains("spreadtrum") -> "Unisoc ($clean)"

            clean.isNotBlank() && !clean.equals("unknown", ignoreCase = true) -> {
                val manufacturer = Build.MANUFACTURER.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
                if (clean.startsWith(manufacturer, ignoreCase = true)) clean else "$manufacturer $clean"
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

    private fun resolveNpuCapabilities(context: Context, cores: Int, processorName: String): NpuCapabilities {
        return try {
            val pm = context.packageManager
            val hasVulkan = pm.hasSystemFeature("android.hardware.vulkan.compute") ||
                    pm.hasSystemFeature("android.hardware.vulkan.level") ||
                    pm.hasSystemFeature("android.hardware.vulkan.version")

            val hasSlsi = pm.hasSystemFeature("com.google.android.feature.AICORE_SLSI")
            val hasQcom = pm.hasSystemFeature("com.google.android.feature.AICORE_QUALCOMM")
            val hasMtk = pm.hasSystemFeature("com.google.android.feature.AICORE_MEDIATEK")
            val hasGoogle = pm.hasSystemFeature("com.google.android.feature.AICORE")
            val hasNnApi = pm.hasSystemFeature("android.hardware.neuralnetworks")

            val procLower = processorName.lowercase(Locale.ROOT)
            val mfgLower = Build.MANUFACTURER.lowercase(Locale.ROOT)
            val brandLower = Build.BRAND.lowercase(Locale.ROOT)

            val isExynos = procLower.contains("exynos") || mfgLower.contains("samsung") || brandLower.contains("samsung")
            val isSnapdragon = procLower.contains("snapdragon") || procLower.contains("qualcomm") || mfgLower.contains("qualcomm")
            val isTensor = procLower.contains("tensor") || mfgLower.contains("google") || brandLower.contains("google")
            val isDimensity = procLower.contains("dimensity") || procLower.contains("mediatek")

            val hasNpu = hasSlsi || hasQcom || hasMtk || hasGoogle || hasNnApi ||
                    isExynos || isSnapdragon || isTensor || isDimensity

            val npuVendorName = when {
                isExynos -> "Samsung NPU"
                isSnapdragon -> "Qualcomm Hexagon NPU"
                isTensor -> "Google Tensor TPU"
                isDimensity -> "MediaTek APU"
                hasSlsi -> "Samsung SLSI NPU"
                hasQcom -> "Qualcomm AI Engine"
                hasGoogle -> "Google Tensor TPU"
                hasMtk -> "MediaTek APU"
                hasNpu -> "Neural Processing Unit (NPU)"
                else -> "Multi-Core CPU"
            }

            val status = when {
                hasNpu && hasVulkan -> "$npuVendorName + Vulkan ($cores Cores)"
                hasNpu -> "$npuVendorName ($cores Cores)"
                hasVulkan -> "Vulkan GPU Acceleration ($cores Cores)"
                else -> "Multi-Core CPU ($cores Cores)"
            }

            NpuCapabilities(
                status = status,
                hasNpu = hasNpu,
                hasVulkan = hasVulkan,
                npuVendorName = npuVendorName
            )
        } catch (e: Exception) {
            NpuCapabilities(
                status = "Hardware Accelerated ($cores Cores)",
                hasNpu = true,
                hasVulkan = true,
                npuVendorName = "Hardware Accelerator"
            )
        }
    }
}

