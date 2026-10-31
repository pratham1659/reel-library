package com.reelrecall.app.data.cloud

import android.accounts.Account
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class GoogleDriveFile(
    val id: String,
    val name: String,
    val sizeBytes: Long = 0L,
    val modifiedTime: String = "",
    val formattedSize: String = "",
    val formattedTime: String = ""
)

data class GoogleDriveAccountInfo(
    val email: String,
    val displayName: String,
    val photoUrl: String? = null,
    val account: Account? = null
)

class GoogleDriveManager(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
) {

    companion object {
        const val DRIVE_SCOPE = "https://www.googleapis.com/auth/drive.file"
        const val OAUTH_SCOPE_STRING = "oauth2:https://www.googleapis.com/auth/drive.file"
        const val VAULT_FOLDER_NAME = "ReelLibrary_Vault"
        private val MEDIA_TYPE_RELATED = "multipart/related".toMediaType()
        private val MEDIA_TYPE_JSON = "application/json; charset=UTF-8".toMediaType()
        private val MEDIA_TYPE_ZIP = "application/zip".toMediaType()
    }

    /**
     * Builds and returns GoogleSignInClient configured with the drive.file scope.
     * This scope requests access ONLY to files and folders created or opened by Reel Library,
     * ensuring user privacy.
     */
    fun getSignInClient(context: Context): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .requestScopes(Scope(DRIVE_SCOPE))
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    fun getSignInIntent(context: Context): Intent {
        return getSignInClient(context).signInIntent
    }

    /**
     * Retrieves the currently cached GoogleSignInAccount if signed in.
     */
    fun getLastSignedInAccount(context: Context): GoogleSignInAccount? {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        return if (account != null && GoogleSignIn.hasPermissions(account, Scope(DRIVE_SCOPE))) {
            account
        } else {
            account
        }
    }

    /**
     * Signs out of Google Play Services.
     */
    suspend fun signOut(context: Context): Unit = withContext(Dispatchers.IO) {
        try {
            getSignInClient(context).signOut()
        } catch (_: Exception) {}
    }

    /**
     * Fetches a valid OAuth2 Bearer token from Google Play Services for the authenticated account.
     * Handles background token acquisition on Dispatchers.IO.
     */
    suspend fun getAccessToken(context: Context, account: Account): Result<String> = withContext(Dispatchers.IO) {
        try {
            val token = GoogleAuthUtil.getToken(context, account, OAUTH_SCOPE_STRING)
            Result.success(token)
        } catch (e: UserRecoverableAuthException) {
            // Needs user consent or interactive prompt
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Invalidate cached token when 401 Unauthorized occurs so next attempt gets a fresh token.
     */
    suspend fun invalidateToken(context: Context, token: String) = withContext(Dispatchers.IO) {
        try {
            GoogleAuthUtil.clearToken(context, token)
        } catch (_: Exception) {}
    }

    /**
     * Finds or creates the dedicated "/ReelLibrary_Vault" folder in user's Google Drive.
     */
    suspend fun getOrCreateVaultFolder(accessToken: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val query = "name = '$VAULT_FOLDER_NAME' and mimeType = 'application/vnd.google-apps.folder' and trashed = false"
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val url = "https://www.googleapis.com/drive/v3/files?q=$encodedQuery&fields=files(id,name)&spaces=drive"

            val searchRequest = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $accessToken")
                .get()
                .build()

            client.newCall(searchRequest).execute().use { response ->
                if (!response.isSuccessful) {
                    val err = response.body?.string() ?: "HTTP ${response.code}"
                    return@withContext Result.failure(Exception("Failed to query Drive folder: $err"))
                }

                val json = JSONObject(response.body?.string() ?: "{}")
                val files = json.optJSONArray("files") ?: JSONArray()

                if (files.length() > 0) {
                    val folderId = files.getJSONObject(0).getString("id")
                    return@withContext Result.success(folderId)
                }
            }

            // Folder does not exist, create it
            val createUrl = "https://www.googleapis.com/drive/v3/files"
            val metaJson = JSONObject().apply {
                put("name", VAULT_FOLDER_NAME)
                put("mimeType", "application/vnd.google-apps.folder")
                put("description", "Reel Library Universal Vault Backup Directory")
            }

            val createRequest = Request.Builder()
                .url(createUrl)
                .addHeader("Authorization", "Bearer $accessToken")
                .post(metaJson.toString().toRequestBody(MEDIA_TYPE_JSON))
                .build()

            client.newCall(createRequest).execute().use { response ->
                if (!response.isSuccessful) {
                    val err = response.body?.string() ?: "HTTP ${response.code}"
                    return@withContext Result.failure(Exception("Failed to create Drive folder: $err"))
                }

                val json = JSONObject(response.body?.string() ?: "{}")
                val folderId = json.getString("id")
                Result.success(folderId)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Uploads the backup zip file to the specified Google Drive folder using multipart upload.
     */
    suspend fun uploadVaultBackup(
        accessToken: String,
        folderId: String,
        zipFile: File,
        onProgress: (String) -> Unit = {}
    ): Result<GoogleDriveFile> = withContext(Dispatchers.IO) {
        try {
            onProgress("Preparing metadata & archive...")

            val metadata = JSONObject().apply {
                put("name", zipFile.name)
                put("parents", JSONArray().put(folderId))
                put("description", "Reel Library Universal Vault Backup Archive")
            }

            val multipartBody = MultipartBody.Builder()
                .setType(MEDIA_TYPE_RELATED)
                .addPart(metadata.toString().toRequestBody(MEDIA_TYPE_JSON))
                .addPart(zipFile.asRequestBody(MEDIA_TYPE_ZIP))
                .build()

            onProgress("Uploading to Google Drive (${formatBytes(zipFile.length())})...")

            val uploadUrl = "https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart&fields=id,name,size,createdTime,modifiedTime"
            val request = Request.Builder()
                .url(uploadUrl)
                .addHeader("Authorization", "Bearer $accessToken")
                .post(multipartBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val err = response.body?.string() ?: "HTTP ${response.code}"
                    return@withContext Result.failure(Exception("Upload failed: $err"))
                }

                val json = JSONObject(response.body?.string() ?: "{}")
                val id = json.getString("id")
                val name = json.optString("name", zipFile.name)
                val size = json.optLong("size", zipFile.length())
                val modifiedTime = json.optString("modifiedTime", json.optString("createdTime", ""))

                val driveFile = GoogleDriveFile(
                    id = id,
                    name = name,
                    sizeBytes = size,
                    modifiedTime = modifiedTime,
                    formattedSize = formatBytes(size),
                    formattedTime = formatDriveDate(modifiedTime)
                )

                onProgress("Upload successful!")
                Result.success(driveFile)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Lists all backup archives present in the /ReelLibrary_Vault folder.
     */
    suspend fun listVaultBackups(
        accessToken: String,
        folderId: String
    ): Result<List<GoogleDriveFile>> = withContext(Dispatchers.IO) {
        try {
            val query = "'$folderId' in parents and trashed = false"
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val url = "https://www.googleapis.com/drive/v3/files?q=$encodedQuery&orderBy=createdTime desc&fields=files(id,name,size,createdTime,modifiedTime)&spaces=drive"

            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $accessToken")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val err = response.body?.string() ?: "HTTP ${response.code}"
                    return@withContext Result.failure(Exception("Failed to list backups: $err"))
                }

                val json = JSONObject(response.body?.string() ?: "{}")
                val files = json.optJSONArray("files") ?: JSONArray()
                val list = mutableListOf<GoogleDriveFile>()

                for (i in 0 until files.length()) {
                    val item = files.getJSONObject(i)
                    val id = item.getString("id")
                    val name = item.getString("name")
                    val size = item.optLong("size", 0L)
                    val modified = item.optString("modifiedTime", item.optString("createdTime", ""))

                    list.add(
                        GoogleDriveFile(
                            id = id,
                            name = name,
                            sizeBytes = size,
                            modifiedTime = modified,
                            formattedSize = formatBytes(size),
                            formattedTime = formatDriveDate(modified)
                        )
                    )
                }

                Result.success(list)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Downloads a backup zip from Google Drive to a local destination file.
     */
    suspend fun downloadVaultBackup(
        accessToken: String,
        fileId: String,
        destFile: File,
        onProgress: (String) -> Unit = {}
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            onProgress("Connecting to Google Drive...")
            val downloadUrl = "https://www.googleapis.com/drive/v3/files/$fileId?alt=media"

            val request = Request.Builder()
                .url(downloadUrl)
                .addHeader("Authorization", "Bearer $accessToken")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val err = response.body?.string() ?: "HTTP ${response.code}"
                    return@withContext Result.failure(Exception("Failed to download file: $err"))
                }

                val responseBody = response.body
                    ?: return@withContext Result.failure(Exception("Empty response body from Drive"))

                onProgress("Downloading backup archive...")
                destFile.parentFile?.mkdirs()

                responseBody.byteStream().use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }

                onProgress("Download completed (${formatBytes(destFile.length())})")
                Result.success(destFile)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Deletes a backup file from Google Drive.
     */
    suspend fun deleteVaultBackup(
        accessToken: String,
        fileId: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val url = "https://www.googleapis.com/drive/v3/files/$fileId"
            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $accessToken")
                .delete()
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful || response.code == 204) {
                    Result.success(true)
                } else {
                    Result.failure(Exception("Delete failed: HTTP ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        return when {
            gb >= 1.0 -> String.format(Locale.getDefault(), "%.1f GB", gb)
            mb >= 1.0 -> String.format(Locale.getDefault(), "%.1f MB", mb)
            kb >= 1.0 -> String.format(Locale.getDefault(), "%.1f KB", kb)
            else -> "$bytes B"
        }
    }

    private fun formatDriveDate(raw: String): String {
        if (raw.isBlank()) return "Unknown date"
        return try {
            // ISO 8601 format: 2026-11-01T01:28:45.123Z
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            val date = parser.parse(raw.take(19)) ?: return raw
            val formatter = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())
            formatter.format(date)
        } catch (_: Exception) {
            raw
        }
    }
}
