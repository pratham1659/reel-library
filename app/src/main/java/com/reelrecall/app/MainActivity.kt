package com.reelrecall.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.reelrecall.app.data.local.AppDatabase
import com.reelrecall.app.data.model.IngestionStatus
import com.reelrecall.app.data.model.ReelEntity
import com.reelrecall.app.ui.screens.settings.SettingsScreen
import com.reelrecall.app.ui.screens.settings.SettingsViewModel
import com.reelrecall.app.ui.screens.vault.VaultScreen
import com.reelrecall.app.ui.screens.vault.VaultViewModel
import com.reelrecall.app.ui.screens.vault.components.KnowledgeDrawerContent
import com.reelrecall.app.ui.theme.DarkNeumorphic
import com.reelrecall.app.ui.theme.LightNeumorphic
import com.reelrecall.app.ui.theme.ObsidianBg
import com.reelrecall.app.ui.theme.ObsidianCard
import com.reelrecall.app.ui.theme.ObsidianPill
import com.reelrecall.app.ui.theme.ObsidianPillBorder
import com.reelrecall.app.ui.theme.ReelRecallTheme
import com.reelrecall.app.ui.theme.SparkleOrange
import com.reelrecall.app.ui.theme.TextMuted
import com.reelrecall.app.ui.theme.TextPrimary
import com.reelrecall.app.ui.theme.TextSecondary
import com.reelrecall.app.worker.InstagramUrlParser
import com.reelrecall.app.worker.ReelIngestWorker
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val vaultViewModel by viewModels<VaultViewModel>()
    private val settingsViewModel by viewModels<SettingsViewModel>()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val preferences by settingsViewModel.preferences.collectAsState()
            val isDarkTheme = preferences.themeMode != "OBSIDIAN_LIGHT"

            ReelRecallTheme(isDarkTheme = isDarkTheme) {
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                var currentTab by remember { mutableIntStateOf(0) } // 0 = Vault, 1 = Settings, 2 = Drawer/Knowledge

                val groupedReels by vaultViewModel.groupedReels.collectAsState()
                val allReels = remember(groupedReels) { groupedReels.flatMap { it.reels } }

                var showAddReelDialog by remember { mutableStateOf(false) }
                var showAiAssistantDialog by remember { mutableStateOf(false) }

                androidx.activity.compose.BackHandler(enabled = drawerState.isOpen) {
                    scope.launch { drawerState.close() }
                }
                androidx.activity.compose.BackHandler(enabled = !drawerState.isOpen && currentTab != 0) {
                    currentTab = 0
                }

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        KnowledgeDrawerContent(
                            reels = allReels,
                            onSelectReel = { reel ->
                                scope.launch { drawerState.close() }
                                openReel(reel.canonicalUrl)
                            },
                            onClose = { scope.launch { drawerState.close() } }
                        )
                    }
                ) {
                    val rootBg = if (isDarkTheme) DarkNeumorphic.background else LightNeumorphic.background
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(rootBg)
                    ) {
                        // MAIN CONTENT AREA
                        when (currentTab) {
                            0 -> {
                                VaultScreen(
                                    viewModel = vaultViewModel,
                                    onOpenDrawer = { scope.launch { drawerState.open() } },
                                    onOpenSettings = { currentTab = 1 },
                                    onAddReel = { showAddReelDialog = true },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            1 -> {
                                SettingsScreen(
                                    viewModel = settingsViewModel,
                                    onBack = { currentTab = 0 },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        // QUICK ADD REEL URL DIALOG
                        if (showAddReelDialog) {
                            AddReelDialog(
                                onDismiss = { showAddReelDialog = false },
                                onAddReel = { url ->
                                    showAddReelDialog = false
                                    ingestReelUrl(url)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun ingestReelUrl(url: String) {
        val parsed = InstagramUrlParser.parse(url)
        if (parsed == null) {
            Toast.makeText(this, "Please enter a valid Instagram Reel link", Toast.LENGTH_SHORT).show()
            return
        }

        val db = AppDatabase.getInstance(applicationContext)
        val dao = db.reelDao()

        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
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
        }

        Toast.makeText(this, "Ingesting Reel into your Vault...", Toast.LENGTH_SHORT).show()
    }

    private fun openReel(canonicalUrl: String) {
        val uri = Uri.parse(canonicalUrl)
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.instagram.android")
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
    }
}

@Composable
fun AddReelDialog(
    onDismiss: () -> Unit,
    onAddReel: (String) -> Unit
) {
    var urlInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianCard,
        shape = RoundedCornerShape(22.dp),
        title = {
            Text("Add to ReelRecall", color = TextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(
                    "Paste any Instagram Reel link below to extract its takeaways and poster preview:",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    placeholder = { Text("https://www.instagram.com/reel/...", color = TextMuted) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ObsidianPill,
                        unfocusedContainerColor = ObsidianPill,
                        focusedBorderColor = SparkleOrange,
                        unfocusedBorderColor = ObsidianPillBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onAddReel(urlInput.trim()) },
                colors = ButtonDefaults.buttonColors(containerColor = SparkleOrange),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Add Reel")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
fun AiAssistantDialog(
    reelCount: Int,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianCard,
        shape = RoundedCornerShape(22.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("✨ Ask Knowledge Vault", color = TextPrimary, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                Text(
                    text = "Your vault currently has $reelCount indexed Reel knowledge item(s). Ask questions to synthesize insights across all your saved Reels.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF22242F),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "💡 Tip: Share educational Reels from Instagram via Android share sheet to continuously expand your vault.",
                        color = Color(0xFFFFB74D),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = SparkleOrange),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Got it")
            }
        }
    )
}
