package com.reelrecall.app.worker

import android.accounts.Account
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.reelrecall.app.data.cloud.GoogleDriveManager
import com.reelrecall.app.data.cloud.VaultBackupExporter
import com.reelrecall.app.data.repository.PreferencesRepository
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Background worker that automatically triggers a Google Drive personal cloud backup
 * of the user's universal vault at the start of each new day.
 *
 * Adheres strictly to security and user preferences:
 * - Backs up exclusively to the isolated /ReelLibrary_Vault folder with drive.file scope.
 * - Respects the user's Wi-Fi network preference (backupOnlyOnWifi).
 * - Ensures idempotency so backups execute only once per calendar day.
 */
class DailyCloudBackupWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val preferencesRepository = PreferencesRepository(appContext)
    private val driveManager = GoogleDriveManager()

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting DailyCloudBackupWorker execution...")
        val prefs = preferencesRepository.preferencesFlow.first()

        // 1. Check if user is connected to Google Drive and auto-backup is enabled
        if (!prefs.isGoogleDriveConnected || !prefs.isCloudBackupEnabled) {
            Log.d(TAG, "Skipping daily backup: Google Drive is not connected or backup is disabled.")
            return Result.success()
        }

        // 2. Check if already backed up today
        val todayStr = preferencesRepository.getTodayDateString()
        if (prefs.lastDailyCloudBackupDate == todayStr) {
            Log.d(TAG, "Skipping daily backup: Vault already backed up today ($todayStr).")
            return Result.success()
        }

        // 3. Check Wi-Fi constraint if user specified 'backupOnlyOnWifi'
        if (prefs.backupOnlyOnWifi && !isWifiConnected(applicationContext)) {
            Log.d(TAG, "Wi-Fi is required for backup, but currently not connected. Retrying later.")
            return Result.retry()
        }

        // 4. Authenticate with Google Drive
        val lastAccount = driveManager.getLastSignedInAccount(applicationContext)
        if (lastAccount == null) {
            Log.w(TAG, "No signed-in Google account found. Cannot perform daily backup.")
            return Result.failure()
        }

        val accountObj = lastAccount.account ?: Account(lastAccount.email ?: "", "com.google")
        val tokenResult = driveManager.getAccessToken(applicationContext, accountObj)
        if (tokenResult.isFailure) {
            Log.e(TAG, "Failed to acquire Google Drive auth token", tokenResult.exceptionOrNull())
            return Result.retry()
        }
        val token = tokenResult.getOrThrow()

        // 5. Locate or create /ReelLibrary_Vault folder
        val folderResult = driveManager.getOrCreateVaultFolder(token)
        if (folderResult.isFailure) {
            Log.e(TAG, "Failed to resolve Drive folder", folderResult.exceptionOrNull())
            return Result.retry()
        }
        val folderId = folderResult.getOrThrow()

        // 6. Create vault archive
        val zipFile = VaultBackupExporter.exportMetadataZip(applicationContext)
        if (zipFile == null || !zipFile.exists() || zipFile.length() == 0L) {
            Log.e(TAG, "Failed to create backup zip archive.")
            return Result.failure()
        }

        // 7. Upload to Google Drive
        val uploadResult = driveManager.uploadVaultBackup(token, folderId, zipFile)
        if (uploadResult.isFailure) {
            Log.e(TAG, "Failed to upload daily backup to Google Drive", uploadResult.exceptionOrNull())
            return Result.retry()
        }

        // 8. Record success in DataStore
        preferencesRepository.markDailyCloudBackupCompleted()
        Log.i(TAG, "Daily Google Drive cloud backup successfully completed for $todayStr.")
        return Result.success()
    }

    private fun isWifiConnected(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return true
            val network = cm.activeNetwork ?: return true
            val capabilities = cm.getNetworkCapabilities(network) ?: return true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        } catch (_: Exception) {
            true
        }
    }

    companion object {
        const val TAG = "DailyCloudBackupWorker"
        const val PERIODIC_WORK_NAME = "DailyCloudBackupPeriodic"
        const val IMMEDIATE_WORK_NAME = "DailyCloudBackupImmediate"

        fun calculateDelayUntilMidnight(): Long {
            val now = System.currentTimeMillis()
            val cal = Calendar.getInstance().apply {
                timeInMillis = now
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 5) // 5 minutes past midnight to guarantee new calendar day
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val delay = cal.timeInMillis - now
            return if (delay > 0) delay else TimeUnit.HOURS.toMillis(24)
        }

        fun scheduleDailyBackup(context: Context) {
            val initialDelay = calculateDelayUntilMidnight()
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val dailyWork = PeriodicWorkRequestBuilder<DailyCloudBackupWorker>(24, TimeUnit.HOURS)
                .setConstraints(constraints)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                dailyWork
            )
        }

        fun triggerDailyBackupIfNeeded(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val oneTimeWork = OneTimeWorkRequestBuilder<DailyCloudBackupWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                IMMEDIATE_WORK_NAME,
                ExistingWorkPolicy.KEEP,
                oneTimeWork
            )
        }
    }
}
