package com.reelrecall.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.reelrecall.app.BuildConfig
import com.reelrecall.app.data.model.AppUpdateInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit

@Serializable
private data class GitHubReleaseAsset(
    val name: String,
    @SerialName("browser_download_url") val browserDownloadUrl: String
)

@Serializable
private data class GitHubReleaseResponse(
    @SerialName("tag_name") val tagName: String,
    val name: String? = null,
    val body: String? = null,
    val assets: List<GitHubReleaseAsset> = emptyList()
)

object UpdateManager {

    private const val GITHUB_OWNER = "pratham1659"
    private const val GITHUB_REPO = "reel-library"
    const val DEFAULT_UPDATE_ENDPOINT = "https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO/releases/latest"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Checks GitHub's public releases API directly for a newer version than current BuildConfig.VERSION_NAME/CODE.
     * Returns AppUpdateInfo if an update is available, or null if already up to date.
     */
    suspend fun checkForUpdate(
        endpointUrl: String = DEFAULT_UPDATE_ENDPOINT
    ): Result<AppUpdateInfo?> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(endpointUrl)
                .header("User-Agent", "ReelLibrary/${BuildConfig.VERSION_NAME} (Android ${Build.VERSION.RELEASE})")
                .header("Accept", "application/vnd.github.v3+json, application/json")
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                // If 404, no release exists yet on GitHub
                if (response.code == 404) {
                    return@withContext Result.success(null)
                }
                return@withContext Result.failure(
                    IOException("Release server returned HTTP ${response.code}")
                )
            }

            val body = response.body?.string()
                ?: return@withContext Result.failure(IOException("Empty response from release server"))

            val githubRelease = jsonParser.decodeFromString<GitHubReleaseResponse>(body)
            val releaseVersionName = githubRelease.tagName.trimStart('v', 'V')

            // Locate the .apk asset attached to this GitHub release
            val apkAsset = githubRelease.assets.firstOrNull { it.name.endsWith(".apk", ignoreCase = true) }
            val apkDownloadUrl = apkAsset?.browserDownloadUrl
                ?: "https://github.com/$GITHUB_OWNER/$GITHUB_REPO/releases/download/${githubRelease.tagName}/ReelLibrary-release-v$releaseVersionName.apk"

            // Check if release notes contain an explicit versionCode (e.g. "versionCode: 4")
            val parsedVersionCode = extractVersionCode(githubRelease.body)
            val isNewer = if (parsedVersionCode != null) {
                parsedVersionCode > BuildConfig.VERSION_CODE
            } else {
                isVersionNewer(releaseVersionName, BuildConfig.VERSION_NAME)
            }

            if (isNewer) {
                val updateInfo = AppUpdateInfo(
                    versionCode = parsedVersionCode ?: (BuildConfig.VERSION_CODE + 1),
                    versionName = releaseVersionName,
                    apkUrl = apkDownloadUrl,
                    releaseNotes = githubRelease.body ?: "New release available on GitHub.",
                    forceUpdate = false
                )
                Result.success(updateInfo)
            } else {
                Result.success(null) // Already on the latest version
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun extractVersionCode(notes: String?): Int? {
        if (notes.isNullOrBlank()) return null
        val match = Regex("""versionCode[:=\s]+(\d+)""", RegexOption.IGNORE_CASE).find(notes)
        return match?.groupValues?.get(1)?.toIntOrNull()
    }

    private fun isVersionNewer(remoteVersion: String, localVersion: String): Boolean {
        val remoteParts = remoteVersion.split(".").mapNotNull { it.toIntOrNull() }
        val localParts = localVersion.split(".").mapNotNull { it.toIntOrNull() }
        val maxLen = maxOf(remoteParts.size, localParts.size)
        for (i in 0 until maxLen) {
            val r = remoteParts.getOrElse(i) { 0 }
            val l = localParts.getOrElse(i) { 0 }
            if (r > l) return true
            if (r < l) return false
        }
        return false
    }

    /**
     * Downloads the APK file to the app's cache directory with progress reporting.
     */
    suspend fun downloadApk(
        context: Context,
        apkUrl: String,
        onProgress: (bytesRead: Long, totalBytes: Long) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val updatesDir = File(context.cacheDir, "updates")
            if (!updatesDir.exists()) {
                updatesDir.mkdirs()
            }
            val destinationFile = File(updatesDir, "ReelLibrary-update.apk")
            if (destinationFile.exists()) {
                destinationFile.delete()
            }

            val request = Request.Builder()
                .url(apkUrl)
                .header("User-Agent", "ReelLibrary/${BuildConfig.VERSION_NAME}")
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    IOException("Failed to download APK: HTTP ${response.code}")
                )
            }

            val responseBody = response.body
                ?: return@withContext Result.failure(IOException("Empty APK download body"))

            val totalBytes = responseBody.contentLength()

            responseBody.byteStream().use { input ->
                FileOutputStream(destinationFile).use { output ->
                    val buffer = ByteArray(8 * 1024)
                    var bytesRead: Int
                    var totalRead = 0L

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalRead += bytesRead
                        onProgress(totalRead, totalBytes)
                    }
                    output.flush()
                }
            }

            Result.success(destinationFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Verifies if the app has permission to install unknown apps (Android 8.0+).
     */
    fun canRequestPackageInstalls(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    /**
     * Launches unknown source permission settings or starts the package installer intent.
     */
    fun installApk(context: Context, apkFile: File): Boolean {
        if (!apkFile.exists() || apkFile.length() == 0L) {
            return false
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !context.packageManager.canRequestPackageInstalls()) {
            val manageIntent = Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(manageIntent)
            return false
        }

        val apkUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile
        )

        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(installIntent)
        return true
    }
}
