package com.reelrecall.app.data.cloud

import android.content.Context
import com.reelrecall.app.data.local.AppDatabase
import com.reelrecall.app.data.model.ReelLibraryExport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Universal Vault Backup Exporter.
 * Serializes Room database records, metadata, and reel transcripts into a portable .zip archive
 * suitable for Google Drive personal cloud backup or offline export.
 */
object VaultBackupExporter {

    suspend fun exportMetadataZip(context: Context): File? = withContext(Dispatchers.IO) {
        try {
            val dao = AppDatabase.getInstance(context).reelDao()
            val reels = dao.getAllReelsSync()
            val exportData = ReelLibraryExport(
                version = 1,
                exportDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
                totalReels = reels.size,
                reels = reels
            )
            val jsonString = Json { prettyPrint = true; ignoreUnknownKeys = true }.encodeToString(exportData)

            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val zipFile = File(exportDir, "ReelLibrary_Vault_Backup_${System.currentTimeMillis()}.zip")

            ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
                // 1. Write metadata.json
                val metaEntry = ZipEntry("reellibrary_metadata.json")
                zos.putNextEntry(metaEntry)
                zos.write(jsonString.toByteArray(Charsets.UTF_8))
                zos.closeEntry()

                // 2. Write individual reel transcript text files
                reels.forEach { reel ->
                    if (!reel.transcript.isNullOrBlank()) {
                        val txtName = "transcripts/reel_${reel.shortcode}.txt"
                        zos.putNextEntry(ZipEntry(txtName))
                        val content = "Title: ${reel.title ?: "Untitled"}\n" +
                                "URL: ${reel.canonicalUrl}\n" +
                                "Creator: ${reel.creatorHandle ?: "Unknown"}\n" +
                                "Summary: ${reel.oneLineSummary ?: "None"}\n\n" +
                                "--- FULL TRANSCRIPT ---\n" +
                                reel.transcript
                        zos.write(content.toByteArray(Charsets.UTF_8))
                        zos.closeEntry()
                    }
                }
            }
            zipFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
