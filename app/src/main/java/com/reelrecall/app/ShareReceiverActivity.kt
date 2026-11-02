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
            val genericUrl = InstagramUrlParser.extractAnyUrl(sharedText) ?: sharedText.trim()
            if (genericUrl.isNotBlank() && (genericUrl.startsWith("http://") || genericUrl.startsWith("https://"))) {
                lifecycleScope.launch {
                    val db = AppDatabase.getInstance(applicationContext)
                    val dao = db.reelDao()
                    val hash = genericUrl.hashCode().toString().removePrefix("-")
                    val existing = dao.findByCanonicalUrl(genericUrl)
                    if (existing == null) {
                        val unsupportedReel = ReelEntity(
                            canonicalUrl = genericUrl,
                            shortcode = "link_$hash",
                            title = "Unsupported Link",
                            status = IngestionStatus.NOT_SUPPORTED,
                            errorMessage = "Unsupported link: Reel Library only supports Instagram Reels."
                        )
                        dao.insert(unsupportedReel)
                    }

                    val mainIntent = Intent(applicationContext, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("UNSUPPORTED_LINK_ALERT", genericUrl)
                    }
                    startActivity(mainIntent)
                    finish()
                }
            } else {
                Toast.makeText(this, "Could not detect a valid Instagram Reel URL", Toast.LENGTH_SHORT).show()
                finish()
            }
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


            Toast.makeText(applicationContext, "Saving to Reel Library...", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
