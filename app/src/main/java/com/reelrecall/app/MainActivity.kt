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
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import com.reelrecall.app.ui.theme.getNeumorphicColors
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
import com.reelrecall.app.data.model.ViewMode
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

            var showSplashScreen by remember { mutableStateOf(true) }
            androidx.compose.runtime.LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(1200)
                showSplashScreen = false
            }

            ReelRecallTheme(isDarkTheme = isDarkTheme) {
                Box(modifier = Modifier.fillMaxSize()) {
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
                                isDarkTheme = isDarkTheme,
                                onSelectReel = { reel ->
                                    scope.launch { drawerState.close() }
                                    currentTab = 0
                                    vaultViewModel.selectReelForDetail(reel.id)
                                },
                                onAddReel = {
                                    showAddReelDialog = true
                                },
                                onStartMultiSelect = {
                                    scope.launch { drawerState.close() }
                                    currentTab = 0
                                    vaultViewModel.toggleSelectionMode(true)
                                },
                                onToggleViewMode = {
                                    scope.launch { drawerState.close() }
                                    currentTab = 0
                                    val currentMode = vaultViewModel.viewMode.value
                                    val newMode = if (currentMode == ViewMode.GRID) {
                                        ViewMode.LIST
                                    } else {
                                        ViewMode.GRID
                                    }
                                    vaultViewModel.setViewMode(newMode)
                                },
                                onReprocessAll = {
                                    scope.launch {
                                        drawerState.close()
                                        vaultViewModel.reprocessDemoReelsIfNeeded()
                                    }
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
                                    isDarkTheme = isDarkTheme,
                                    onDismiss = { showAddReelDialog = false },
                                    onAddReel = { url ->
                                        showAddReelDialog = false
                                        ingestReelUrl(url)
                                    }
                                )
                            }
                        }
                    }

                    // INSTAGRAM STYLE SPLASH SCREEN
                    androidx.compose.animation.AnimatedVisibility(
                        visible = showSplashScreen,
                        enter = androidx.compose.animation.fadeIn(),
                        exit = androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(400))
                    ) {
                        InstagramStyleSplashScreen(isDarkTheme = isDarkTheme)
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
    isDarkTheme: Boolean = true,
    onDismiss: () -> Unit,
    onAddReel: (String) -> Unit
) {
    var urlInput by remember { mutableStateOf("") }
    val colors = getNeumorphicColors(isDarkTheme)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = colors.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
            shadowElevation = 14.dp,
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Row with Gradient Instagram Badge + Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF833AB4), Color(0xFFFD1D1D), Color(0xFFFCB045))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Add to Reel Library",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Paste link to summarize & extract takeaways",
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    placeholder = {
                        Text(
                            "https://www.instagram.com/reel/...",
                            color = colors.textMuted,
                            fontSize = 12.sp
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.pillBg,
                        unfocusedContainerColor = colors.pillBg,
                        focusedBorderColor = colors.accent,
                        unfocusedBorderColor = colors.border,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                    ) {
                        Text(
                            text = "Cancel",
                            color = colors.textSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = {
                            if (urlInput.isNotBlank()) {
                                onAddReel(urlInput.trim())
                            }
                        },
                        enabled = urlInput.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.accent,
                            disabledContainerColor = colors.accent.copy(alpha = 0.35f)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                    ) {
                        Text(
                            text = "Add Reel",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InstagramStyleSplashScreen(isDarkTheme: Boolean) {
    val bg = if (isDarkTheme) Color(0xFF0C0E14) else Color(0xFFFFFFFF)
    val textPrimary = if (isDarkTheme) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val textMuted = if (isDarkTheme) Color(0xFF64748B) else Color(0xFF94A3B8)

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logoScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        // CENTER: App Logo + Name
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .graphicsLayer(scaleX = scale, scaleY = scale)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF833AB4),
                                Color(0xFFFD1D1D),
                                Color(0xFFFCB045)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Reel Library",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = textPrimary,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "AI-Powered Video Knowledge",
                fontSize = 12.sp,
                color = textMuted,
                fontWeight = FontWeight.Normal
            )
        }

        // BOTTOM: "from REEL LIBRARY" footer matching Instagram's opening style
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "from",
                fontSize = 11.sp,
                color = textMuted
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "REEL LIBRARY",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkTheme) Color(0xFFE2E8F0) else Color(0xFF1E293B),
                letterSpacing = 1.5.sp
            )
        }
    }
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
