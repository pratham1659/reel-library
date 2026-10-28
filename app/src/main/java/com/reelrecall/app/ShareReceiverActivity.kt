package com.reelrecall.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.reelrecall.app.data.local.AppDatabase
import com.reelrecall.app.data.model.IngestionStatus
import com.reelrecall.app.data.model.ReelEntity
import com.reelrecall.app.worker.InstagramUrlParser
import com.reelrecall.app.worker.ReelIngestWorker
import kotlinx.coroutines.launch

class ShareReceiverActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
            handleSharedText(sharedText)
        } else {
            finish()
        }
    }

    private fun handleSharedText(sharedText: String) {
        val parsed = InstagramUrlParser.parse(sharedText)

        if (parsed == null) {
            Toast.makeText(this, "Could not detect a valid Instagram Reel URL", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        lifecycleScope.launch {
            val db = AppDatabase.getInstance(applicationContext)
            val dao = db.reelDao()

            var existing = dao.findByCanonicalUrl(parsed.canonicalUrl)
            val reelId = if (existing == null) {
                val newReel = ReelEntity(
                    canonicalUrl = parsed.canonicalUrl,
                    shortcode = parsed.shortcode,
                    status = IngestionStatus.QUEUED
                )
                dao.insert(newReel)
            } else {
                dao.updateStatus(existing.id, IngestionStatus.QUEUED, null)
                existing.id
            }

            // Enqueue background processing via WorkManager
            val workRequest = OneTimeWorkRequestBuilder<ReelIngestWorker>()
                .setInputData(
                    workDataOf(
                        ReelIngestWorker.KEY_REEL_ID to reelId,
                        ReelIngestWorker.KEY_CANONICAL_URL to parsed.canonicalUrl
                    )
                )
                .build()

            WorkManager.getInstance(applicationContext).enqueueUniqueWork(
                "ingest_${parsed.shortcode}",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )


            Toast.makeText(applicationContext, "Saving to ReelRecall...", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
