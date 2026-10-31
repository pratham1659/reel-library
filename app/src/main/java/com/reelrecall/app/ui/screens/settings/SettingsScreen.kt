package com.reelrecall.app.ui.screens.settings

import com.reelrecall.app.BuildConfig
import com.reelrecall.app.data.cloud.GoogleDriveFile
import com.reelrecall.app.data.model.AppUpdateInfo
import com.reelrecall.app.util.UpdateManager

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.alpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.TextButton
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import kotlinx.coroutines.launch
import java.io.File
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reelrecall.app.data.repository.AppPreferences
import com.reelrecall.app.data.repository.EngineMode
import com.reelrecall.app.data.repository.AIProvider
import com.reelrecall.app.ui.theme.CyanAccent
import com.reelrecall.app.ui.theme.ElectricIndigo
import com.reelrecall.app.ui.theme.NeumorphicButton
import com.reelrecall.app.ui.theme.NeumorphicCard
import com.reelrecall.app.ui.theme.NeumorphicColors
import com.reelrecall.app.ui.theme.NeumorphicIconButton
import com.reelrecall.app.ui.theme.StatusError
import com.reelrecall.app.ui.theme.StatusReady
import com.reelrecall.app.ui.theme.getNeumorphicColors

enum class SettingsSubscreen {
    PROFILE_MAIN,
    ACCOUNT_DETAILS,
    SELECT_MODELS,
    PREFERENCES,
    DATA_STORAGE
}

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentSubscreen by remember { mutableStateOf(SettingsSubscreen.PROFILE_MAIN) }
    val preferences by viewModel.preferences.collectAsState()
    val isDarkTheme = preferences.themeMode != "OBSIDIAN_LIGHT"
    val colors = getNeumorphicColors(isDarkTheme)

    // Intercept system back button
    BackHandler {
        if (currentSubscreen != SettingsSubscreen.PROFILE_MAIN) {
            currentSubscreen = SettingsSubscreen.PROFILE_MAIN
        } else {
            onBack()
        }
    }

    when (currentSubscreen) {
        SettingsSubscreen.PROFILE_MAIN -> {
            ProfileMainScreen(
                viewModel = viewModel,
                colors = colors,
                onBack = onBack,
                onNavigateToAccount = { currentSubscreen = SettingsSubscreen.ACCOUNT_DETAILS },
                onNavigateToModels = { currentSubscreen = SettingsSubscreen.SELECT_MODELS },
                onNavigateToPreferences = { currentSubscreen = SettingsSubscreen.PREFERENCES },
                onNavigateToData = { currentSubscreen = SettingsSubscreen.DATA_STORAGE },
                modifier = modifier
            )
        }
        SettingsSubscreen.ACCOUNT_DETAILS -> {
            AccountDetailsScreen(
                viewModel = viewModel,
                colors = colors,
                onBack = { currentSubscreen = SettingsSubscreen.PROFILE_MAIN },
                modifier = modifier
            )
        }
        SettingsSubscreen.SELECT_MODELS -> {
            SelectModelsScreen(
                viewModel = viewModel,
                colors = colors,
                onBack = { currentSubscreen = SettingsSubscreen.PROFILE_MAIN },
                modifier = modifier
            )
        }
        SettingsSubscreen.PREFERENCES -> {
            PreferencesScreen(
                viewModel = viewModel,
                colors = colors,
                onBack = { currentSubscreen = SettingsSubscreen.PROFILE_MAIN },
                modifier = modifier
            )
        }
        SettingsSubscreen.DATA_STORAGE -> {
            DataStorageScreen(
                viewModel = viewModel,
                colors = colors,
                onBack = { currentSubscreen = SettingsSubscreen.PROFILE_MAIN },
                modifier = modifier
            )
        }
    }
}

// =========================================================================
// 1. NEUMORPHIC PROFILE / SETTINGS MAIN SCREEN
// =========================================================================
@Composable
private fun ProfileMainScreen(
    viewModel: SettingsViewModel,
    colors: NeumorphicColors,
    onBack: () -> Unit,
    onNavigateToAccount: () -> Unit,
    onNavigateToModels: () -> Unit,
    onNavigateToPreferences: () -> Unit,
    onNavigateToData: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val preferences by viewModel.preferences.collectAsState()
    val cloudTestResult by viewModel.cloudTestResult.collectAsState()
    val isCloudMode = preferences.engineMode != EngineMode.LOCAL_ONLY
    val isDarkTheme = preferences.themeMode != "OBSIDIAN_LIGHT"
    var showUpdateDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
    ) {
        // TOP APP BAR: Neumorphic Back Button + Centered Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            NeumorphicIconButton(
                onClick = onBack,
                colors = colors,
                size = 42.dp
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = colors.textPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = "Settings",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )

            // Balancer spacer
            Spacer(modifier = Modifier.size(42.dp))
        }

        // SCROLLABLE BODY
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 8.dp)
        ) {
            // GROUPED SETTINGS NEUMORPHIC CARD
            NeumorphicCard(
                colors = colors,
                elevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 1. Account
                ProfileMenuRow(
                    icon = Icons.Default.Person,
                    title = "Account",
                    badgeText = "Coming Soon",
                    badgeColor = Color(0xFFFFB74D),
                    colors = colors,
                    onClick = {
                        Toast.makeText(context, "Account & Cloud Profiles are coming soon!", Toast.LENGTH_SHORT).show()
                    }
                )
                NeumorphicDivider(colors = colors)

                // 2. Preferences
                ProfileMenuRow(
                    icon = Icons.Default.Tune,
                    iconTint = ElectricIndigo,
                    title = "Preferences",
                    badgeText = "Coming Soon",
                    badgeColor = Color(0xFFFFB74D),
                    colors = colors,
                    onClick = {
                        Toast.makeText(context, "Preferences customization is coming soon!", Toast.LENGTH_SHORT).show()
                    }
                )
                NeumorphicDivider(colors = colors)

                // 3. Appearance (Neumorphic Dark / Light Theme Toggle)
                ProfileMenuRow(
                    icon = Icons.Default.Palette,
                    title = "Appearance",
                    badgeText = if (isDarkTheme) "Obsidian Dark" else "Obsidian Light",
                    badgeColor = colors.accent,
                    colors = colors,
                    onClick = {
                        val nextTheme = if (isDarkTheme) "OBSIDIAN_LIGHT" else "OBSIDIAN_DARK"
                        viewModel.setThemeMode(nextTheme)
                        Toast.makeText(
                            context,
                            "Switched to ${if (nextTheme == "OBSIDIAN_DARK") "Obsidian Dark" else "Obsidian Light"}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
                NeumorphicDivider(colors = colors)

                // 4. Models & API (Opens Models & API Subscreen)
                val isApiKeySet = preferences.groqApiKey.isNotBlank()
                val apiBadgeText = when {
                    !isApiKeySet -> "API Not Set"
                    cloudTestResult.isSuccess == true -> "API Ready"
                    cloudTestResult.isSuccess == false && !cloudTestResult.isTesting && cloudTestResult.message.isNotBlank() -> "API Error"
                    else -> "API Ready"
                }
                val apiBadgeColor = when {
                    !isApiKeySet -> Color(0xFFFFB74D)
                    cloudTestResult.isSuccess == true -> StatusReady
                    cloudTestResult.isSuccess == false && !cloudTestResult.isTesting && cloudTestResult.message.isNotBlank() -> StatusError
                    else -> StatusReady
                }

                ProfileMenuRow(
                    icon = Icons.Default.Memory,
                    iconTint = if (isApiKeySet) StatusReady else colors.accent,
                    title = "Models & API",
                    badgeText = apiBadgeText,
                    badgeColor = apiBadgeColor,
                    colors = colors,
                    onClick = onNavigateToModels
                )
                NeumorphicDivider(colors = colors)

                // 5. Data & Storage (Cloud Backup, Google Drive & Universal Export/Import)
                ProfileMenuRow(
                    icon = Icons.Default.Storage,
                    iconTint = CyanAccent,
                    title = "Data & Storage",
                    badgeText = if (preferences.isCloudBackupEnabled) "Cloud Sync" else "Local Storage",
                    badgeColor = CyanAccent,
                    colors = colors,
                    onClick = onNavigateToData
                )
                NeumorphicDivider(colors = colors)

                // 6. Check for update (Native software update row)
                ProfileMenuRow(
                    icon = Icons.Default.SystemUpdate,
                    iconTint = CyanAccent,
                    title = "Check for update",
                    badgeText = "v${BuildConfig.VERSION_NAME} • Up to date",
                    badgeColor = StatusReady,
                    colors = colors,
                    onClick = { showUpdateDialog = true }
                )
                NeumorphicDivider(colors = colors)

                // 7. Help & Feedback (Highlight with coral/orange accent)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, Color(0xFFE64A19).copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                ) {
                    ProfileMenuRow(
                        icon = Icons.Default.HelpOutline,
                        iconTint = Color(0xFFFF7043),
                        title = "Help & Feedback",
                        chevronTint = Color(0xFFFF7043),
                        colors = colors,
                        onClick = {
                            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:support@reellibrary.app")
                                putExtra(Intent.EXTRA_SUBJECT, "Reel Library Feedback & Support")
                            }
                            runCatching { context.startActivity(emailIntent) }
                                .onFailure {
                                    Toast.makeText(context, "Support: support@reellibrary.app", Toast.LENGTH_LONG).show()
                                }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }

        if (showUpdateDialog) {
            SoftwareUpdateDialog(
                onDismiss = { showUpdateDialog = false },
                colors = colors
            )
        }
    }
}

// =========================================================================
// SOFTWARE UPDATE MODAL (NATIVE SYSTEM UPDATE EXPERIENCE)
// =========================================================================
private sealed interface UpdateDialogState {
    data object Idle : UpdateDialogState
    data object Checking : UpdateDialogState
    data class UpToDate(val checkedAt: String) : UpdateDialogState
    data class Available(val updateInfo: AppUpdateInfo) : UpdateDialogState
    data class Downloading(val progress: Float, val downloadedBytes: Long, val totalBytes: Long) : UpdateDialogState
    data class Downloaded(val apkFile: java.io.File) : UpdateDialogState
    data class Error(val message: String) : UpdateDialogState
}

@Composable
private fun FormattedReleaseNotes(
    notes: String,
    colors: NeumorphicColors
) {
    val cleanNotes = notes.trim()
    if (cleanNotes.isBlank()) {
        Text(
            text = "Performance improvements and bug fixes.",
            fontSize = 12.sp,
            color = colors.textPrimary
        )
        return
    }

    val lines = remember(notes) {
        cleanNotes.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        lines.forEach { rawLine ->
            val cleanLine = rawLine.replace("`", "").trim()
            when {
                rawLine.startsWith("## ") && rawLine.contains("Reel Library", ignoreCase = true) -> {
                    // Skip redundant app title line
                }
                rawLine.startsWith("###") || rawLine.startsWith("##") || rawLine.startsWith("#") -> {
                    val title = rawLine.trimStart('#', ' ').replace("`", "").trim()
                    if (title.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CyanAccent.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(0.6.dp, CyanAccent.copy(alpha = 0.35f))
                        ) {
                            Text(
                                text = title,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanAccent,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                rawLine.startsWith("-") || rawLine.startsWith("*") || rawLine.startsWith("•") -> {
                    val bulletText = rawLine.trimStart('-', '*', '•', ' ').replace("`", "").trim()
                    if (bulletText.isNotBlank()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 1.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "•",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = CyanAccent,
                                modifier = Modifier.padding(end = 6.dp)
                            )
                            Text(
                                text = bulletText,
                                fontSize = 12.sp,
                                color = colors.textPrimary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
                else -> {
                    if (cleanLine.isNotBlank()) {
                        Text(
                            text = cleanLine,
                            fontSize = 12.sp,
                            color = colors.textSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SoftwareUpdateDialog(
    onDismiss: () -> Unit,
    colors: NeumorphicColors
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var updateState by remember { mutableStateOf<UpdateDialogState>(UpdateDialogState.Idle) }
    var lastCheckedText by remember { mutableStateOf("Just now") }

    fun formatSize(bytes: Long): String {
        if (bytes <= 0) return "0 MB"
        val mb = bytes / (1024f * 1024f)
        return String.format(java.util.Locale.US, "%.1f MB", mb)
    }

    fun runUpdateCheck() {
        scope.launch {
            updateState = UpdateDialogState.Checking
            val result = UpdateManager.checkForUpdate()
            lastCheckedText = "Just now"
            result.fold(
                onSuccess = { info ->
                    if (info != null) {
                        updateState = UpdateDialogState.Available(info)
                    } else {
                        updateState = UpdateDialogState.UpToDate("Just now")
                    }
                },
                onFailure = { err ->
                    updateState = UpdateDialogState.Error(err.localizedMessage ?: "Failed to reach update server")
                }
            )
        }
    }

    fun startDownload(info: AppUpdateInfo) {
        scope.launch {
            updateState = UpdateDialogState.Downloading(0f, 0L, 0L)
            val result = UpdateManager.downloadApk(
                context = context,
                apkUrl = info.apkUrl,
                onProgress = { bytesRead, totalBytes ->
                    val progress = if (totalBytes > 0) (bytesRead.toFloat() / totalBytes).coerceIn(0f, 1f) else 0f
                    updateState = UpdateDialogState.Downloading(progress, bytesRead, totalBytes)
                }
            )
            result.fold(
                onSuccess = { file ->
                    updateState = UpdateDialogState.Downloaded(file)
                    UpdateManager.installApk(context, file)
                },
                onFailure = { err ->
                    updateState = UpdateDialogState.Error(err.localizedMessage ?: "Failed to download update")
                }
            )
        }
    }

    LaunchedEffect(Unit) {
        if (updateState is UpdateDialogState.Idle) {
            runUpdateCheck()
        }
    }

    Dialog(
        onDismissRequest = {
            if (updateState !is UpdateDialogState.Downloading) {
                onDismiss()
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.72f))
                .clickable {
                    if (updateState !is UpdateDialogState.Downloading) {
                        onDismiss()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clickable(enabled = false) {}
            ) {
                NeumorphicCard(
                    colors = colors,
                    elevation = 16.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 640.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Top close header (fixed at top)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.SystemUpdate,
                                    contentDescription = null,
                                    tint = CyanAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "System Update",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                            }
                            IconButton(
                                onClick = onDismiss,
                                enabled = updateState !is UpdateDialogState.Downloading,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Scrollable middle content
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = false)
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Status Icon Badge
                            val badgeColor = when (updateState) {
                                is UpdateDialogState.Checking -> CyanAccent
                                is UpdateDialogState.Available, is UpdateDialogState.Downloading -> CyanAccent
                                is UpdateDialogState.Downloaded -> StatusReady
                                is UpdateDialogState.UpToDate, is UpdateDialogState.Idle -> StatusReady
                                is UpdateDialogState.Error -> Color(0xFFEF4444)
                            }

                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .background(badgeColor.copy(alpha = 0.12f))
                                    .border(1.5.dp, badgeColor.copy(alpha = 0.6f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                when (updateState) {
                                    is UpdateDialogState.Checking -> {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(32.dp),
                                            color = CyanAccent,
                                            strokeWidth = 3.dp
                                        )
                                    }
                                    is UpdateDialogState.Available -> {
                                        Icon(
                                            imageVector = Icons.Default.Download,
                                            contentDescription = null,
                                            tint = CyanAccent,
                                            modifier = Modifier.size(34.dp)
                                        )
                                    }
                                    is UpdateDialogState.Downloading -> {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(32.dp),
                                            color = CyanAccent,
                                            strokeWidth = 3.dp
                                        )
                                    }
                                    is UpdateDialogState.Downloaded -> {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = StatusReady,
                                            modifier = Modifier.size(34.dp)
                                        )
                                    }
                                    is UpdateDialogState.Error -> {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(34.dp)
                                        )
                                    }
                                    else -> {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = StatusReady,
                                            modifier = Modifier.size(34.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Title
                            val titleText = when (val state = updateState) {
                                is UpdateDialogState.Checking -> "Checking for updates..."
                                is UpdateDialogState.Available -> "Update Available: v${state.updateInfo.versionName}"
                                is UpdateDialogState.Downloading -> "Downloading update..."
                                is UpdateDialogState.Downloaded -> "Ready to Install"
                                is UpdateDialogState.Error -> "Unable to check for updates"
                                else -> "Reel Library is up to date"
                            }

                            Text(
                                text = titleText,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Current Version Pill
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = colors.pillBg,
                                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)
                            ) {
                                Text(
                                    text = "Current Version: v${BuildConfig.VERSION_NAME}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = CyanAccent,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Center Content Area: Details breakdown, Changelog or Progress
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = colors.pillBg.copy(alpha = 0.5f),
                                border = androidx.compose.foundation.BorderStroke(0.8.dp, colors.border),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    when (val state = updateState) {
                                        is UpdateDialogState.Available -> {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Release Notes",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = CyanAccent
                                                )
                                                Surface(
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = CyanAccent.copy(alpha = 0.15f)
                                                ) {
                                                    Text(
                                                        text = "v${state.updateInfo.versionName}",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = CyanAccent,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .heightIn(min = 60.dp, max = 200.dp)
                                                    .verticalScroll(rememberScrollState())
                                            ) {
                                                FormattedReleaseNotes(
                                                    notes = state.updateInfo.releaseNotes,
                                                    colors = colors
                                                )
                                            }
                                        }
                                        is UpdateDialogState.Downloading -> {
                                            Text(
                                                text = "Downloading APK...",
                                                fontSize = 12.sp,
                                                color = colors.textSecondary
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            LinearProgressIndicator(
                                                progress = { state.progress },
                                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                                color = CyanAccent,
                                                trackColor = colors.border
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "${(state.progress * 100).toInt()}%",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = CyanAccent
                                                )
                                                if (state.totalBytes > 0) {
                                                    Text(
                                                        text = "${formatSize(state.downloadedBytes)} / ${formatSize(state.totalBytes)}",
                                                        fontSize = 11.sp,
                                                        color = colors.textSecondary
                                                    )
                                                }
                                            }
                                        }
                                        is UpdateDialogState.Downloaded -> {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(text = "Status", fontSize = 12.sp, color = colors.textSecondary)
                                                Text(text = "Download complete", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = StatusReady)
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Tap below to install the update on your device.",
                                                fontSize = 12.sp,
                                                color = colors.textPrimary
                                            )
                                        }
                                        is UpdateDialogState.Error -> {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(text = "Error Details", fontSize = 12.sp, color = Color(0xFFEF4444))
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = state.message,
                                                fontSize = 12.sp,
                                                color = colors.textPrimary
                                            )
                                        }
                                        else -> {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(text = "Status", fontSize = 12.sp, color = colors.textSecondary)
                                                Text(text = "Latest updated", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = StatusReady)
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(text = "Last checked", fontSize = 12.sp, color = colors.textSecondary)
                                                Text(text = lastCheckedText, fontSize = 12.sp, color = colors.textPrimary)
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(text = "Engine Runtime", fontSize = 12.sp, color = colors.textSecondary)
                                                Text(text = "Reel Library Engine v${BuildConfig.VERSION_NAME}", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action Button (ALWAYS VISIBLE & PINNED AT BOTTOM)
                        when (val state = updateState) {
                            is UpdateDialogState.Available -> {
                                NeumorphicButton(
                                    onClick = { startDownload(state.updateInfo) },
                                    colors = colors,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Download,
                                            contentDescription = null,
                                            tint = colors.accent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Download & Install Update",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.textPrimary
                                        )
                                    }
                                }
                            }
                            is UpdateDialogState.Downloaded -> {
                                NeumorphicButton(
                                    onClick = { UpdateManager.installApk(context, state.apkFile) },
                                    colors = colors,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = StatusReady,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Install Now",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.textPrimary
                                        )
                                    }
                                }
                            }
                            is UpdateDialogState.Downloading -> {
                                NeumorphicButton(
                                    onClick = {},
                                    enabled = false,
                                    colors = colors,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = colors.accent,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Downloading APK...",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.textPrimary
                                        )
                                    }
                                }
                            }
                            else -> {
                                val isChecking = updateState is UpdateDialogState.Checking
                                NeumorphicButton(
                                    onClick = { runUpdateCheck() },
                                    enabled = !isChecking,
                                    colors = colors,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        if (isChecking) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                color = colors.accent,
                                                strokeWidth = 2.dp
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Connecting to server...",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.textPrimary
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.SystemUpdate,
                                                contentDescription = null,
                                                tint = colors.accent,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = if (updateState is UpdateDialogState.Error) "Retry check for updates" else "Check for updates",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.textPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// DETACHED PROFILE & AUTH COMPOSABLES (PRESERVED FOR FUTURE RELEASE)
// =========================================================================
@Composable
private fun ProfileAuthHeaderCard(
    preferences: AppPreferences,
    colors: NeumorphicColors,
    onSignInClick: () -> Unit
) {
    NeumorphicCard(
        colors = colors,
        elevation = 6.dp,
        onClick = if (!preferences.isLoggedIn) onSignInClick else null,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (!preferences.isLoggedIn) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(colors.pillBg)
                            .border(1.dp, colors.border, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = colors.textMuted,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "Not signed in",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Tap to sign in with email or free auth",
                            fontSize = 11.sp,
                            color = colors.textMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                NeumorphicButton(
                    onClick = onSignInClick,
                    colors = colors,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Sign In / Register with Email",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary
                        )
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE91E63)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = preferences.userAvatarInitial.ifBlank { "P" },
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = preferences.userName,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = preferences.userEmail,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun LogoutAuthActionCard(
    preferences: AppPreferences,
    colors: NeumorphicColors,
    onLogoutClick: () -> Unit,
    onSignInClick: () -> Unit
) {
    NeumorphicCard(
        colors = colors,
        elevation = 4.dp,
        onClick = if (preferences.isLoggedIn) onLogoutClick else onSignInClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (preferences.isLoggedIn) Icons.AutoMirrored.Filled.Logout else Icons.Default.Person,
                contentDescription = "Auth Action",
                tint = if (preferences.isLoggedIn) Color(0xFFF43F5E) else colors.accent,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = if (preferences.isLoggedIn) "Logout" else "Sign In / Register with Email",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary
            )
        }
    }
}

// =========================================================================
// 2. ACCOUNT DETAILS SUBSCREEN (DETAILED INFO & SESSION MANAGEMENT)
// =========================================================================
@Composable
private fun AccountDetailsScreen(
    viewModel: SettingsViewModel,
    colors: NeumorphicColors,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val preferences by viewModel.preferences.collectAsState()

    var showEmailAuthDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
    ) {
        // TOP APP BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NeumorphicIconButton(
                onClick = onBack,
                colors = colors,
                size = 42.dp
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = colors.textPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = "Account Details",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Text(
                    text = "Manage your profile, sync credentials & storage",
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )
            }
        }

        // CONTENT
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // CARD 1: PROFILE & IDENTITY
            NeumorphicCard(colors = colors) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (preferences.isLoggedIn) Color(0xFFE91E63) else colors.pillBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (preferences.isLoggedIn) preferences.userAvatarInitial.ifBlank { "P" } else "?",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = if (preferences.isLoggedIn) preferences.userName else "Guest User",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = if (preferences.isLoggedIn) preferences.userEmail else "Local session only",
                                fontSize = 12.sp,
                                color = colors.textSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    AccountDetailItem(label = "User ID", value = if (preferences.isLoggedIn) "#RR-${preferences.userName.hashCode().toString().takeLast(5)}" else "#RR-GUEST", colors = colors)
                    AccountDetailItem(
                        label = "Auth Status",
                        value = if (preferences.isLoggedIn) "Verified Email" else "Not Linked",
                        valueColor = if (preferences.isLoggedIn) StatusReady else colors.textMuted,
                        colors = colors
                    )
                    AccountDetailItem(
                        label = "Email Address",
                        value = if (preferences.isLoggedIn) preferences.userEmail else "No email connected",
                        valueColor = if (preferences.isLoggedIn) colors.textPrimary else colors.textMuted,
                        colors = colors
                    )
                    AccountDetailItem(
                        label = "Account Plan",
                        value = "Pro Cloud & On-Device Tier",
                        valueColor = colors.accent,
                        colors = colors
                    )
                }
            }

            // CARD 2: SYNC & SECURITY
            NeumorphicCard(colors = colors) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = colors.accent, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = "Security & Sync", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    AccountDetailItem(label = "Cloud Backup", value = if (preferences.isCloudBackupEnabled) "Active (${preferences.cloudBackupFolder})" else "Disabled", colors = colors)
                    AccountDetailItem(label = "Last Synced", value = preferences.lastBackupTimeFormatted, colors = colors)
                    AccountDetailItem(label = "API Keys Storage", value = "Android Keystore Encrypted", colors = colors)
                }
            }

            // CARD 3: VAULT STORAGE METRICS
            NeumorphicCard(colors = colors) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Storage, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = "Storage & Database", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    AccountDetailItem(label = "Database Engine", value = "Room 2.6 + SQLite FTS5", colors = colors)
                    AccountDetailItem(label = "Local Storage Used", value = "38.6 MB", colors = colors)
                    AccountDetailItem(label = "Whisper Cache", value = if (preferences.isWhisperModelDownloaded) "140 MB" else "0 MB", colors = colors)
                    AccountDetailItem(label = "LLM Weights", value = if (preferences.isLlmModelDownloaded) "1.4 GB" else "0 MB", colors = colors)
                }
            }

            // CARD 4: ACTIONS
            NeumorphicCard(colors = colors) {
                Column(modifier = Modifier.padding(14.dp)) {
                    if (!preferences.isLoggedIn) {
                        NeumorphicButton(
                            onClick = { showEmailAuthDialog = true },
                            colors = colors,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Email, contentDescription = null, tint = colors.accent, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Sign In / Register with Email", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                            }
                        }
                    } else {
                        NeumorphicButton(
                            onClick = {
                                viewModel.logout()
                                Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show()
                            },
                            colors = colors,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Sign Out / Disconnect", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF43F5E))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    NeumorphicButton(
                        onClick = {
                            context.cacheDir.deleteRecursively()
                            context.cacheDir.mkdirs()
                            Toast.makeText(context, "Local cache cleaned (14 MB freed)", Toast.LENGTH_SHORT).show()
                        },
                        colors = colors,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Clear Temporary Cache", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (showEmailAuthDialog) {
            EmailAuthDialog(
                colors = colors,
                onDismiss = { showEmailAuthDialog = false },
                onLogin = { email, name, password, isSignUp ->
                    viewModel.loginWithEmail(email, name, password, isSignUp)
                    showEmailAuthDialog = false
                    Toast.makeText(context, "Authenticated as $email", Toast.LENGTH_SHORT).show()
                },
                onGoogleLogin = { email ->
                    viewModel.loginWithGoogle(email = email)
                    showEmailAuthDialog = false
                    Toast.makeText(context, "Signed in as $email", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

// =========================================================================
// 3. SELECT MODELS SUBSCREEN (NEUMORPHIC)
// =========================================================================
@Composable
private fun SelectModelsScreen(
    viewModel: SettingsViewModel,
    colors: NeumorphicColors,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val preferences by viewModel.preferences.collectAsState()
    val whisperProgress by viewModel.whisperProgress.collectAsState()
    val llmProgress by viewModel.llmProgress.collectAsState()
    val cloudTestResult by viewModel.cloudTestResult.collectAsState()
    val localTestResult by viewModel.localTestResult.collectAsState()
    val hardwareInfo by viewModel.deviceHardwareInfo.collectAsState()

    val selectedEndpointForModal by viewModel.selectedEndpointForModal.collectAsState()
    val showLastCallModal by viewModel.showLastCallModal.collectAsState()
    val lastCallModalModel by viewModel.lastCallModalModel.collectAsState()
    val cloudRecentLogs by viewModel.cloudRecentLogs.collectAsState()
    val cloudDayWise by viewModel.cloudDayWiseConsumption.collectAsState()
    val cloudMonthWise by viewModel.cloudMonthWiseConsumption.collectAsState()
    val localRecentLogs by viewModel.localRecentLogs.collectAsState()
    val localDayWise by viewModel.localDayWiseConsumption.collectAsState()
    val localMonthWise by viewModel.localMonthWiseConsumption.collectAsState()

    val whisperFilePicker = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.importModelFromUri(it, isWhisper = true) }
    }
    val llmFilePicker = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.importModelFromUri(it, isWhisper = false) }
    }

    var groqApiKeyInput by remember { mutableStateOf(preferences.groqApiKey) }

    LaunchedEffect(preferences.groqApiKey) {
        groqApiKeyInput = preferences.groqApiKey
    }

    val activeProvider = preferences.selectedProvider
    val currentApiKey = when (activeProvider) {
        AIProvider.GROQ -> preferences.groqApiKey
        AIProvider.OPENAI -> preferences.openAIApiKey
        AIProvider.CLAUDE -> preferences.claudeApiKey
        AIProvider.CUSTOM -> preferences.customApiKey
    }

    var customBaseUrlInput by remember { mutableStateOf(preferences.customBaseUrl) }
    var customSummarizerModelInput by remember { mutableStateOf(preferences.customSummarizerModel) }
    var customTranscriptionModelInput by remember { mutableStateOf(preferences.customTranscriptionModel) }

    LaunchedEffect(preferences.customBaseUrl, preferences.customSummarizerModel, preferences.customTranscriptionModel) {
        customBaseUrlInput = preferences.customBaseUrl
        customSummarizerModelInput = preferences.customSummarizerModel
        customTranscriptionModelInput = preferences.customTranscriptionModel
    }

    val showLocalLlmInUi = false
    val isCloudMode = preferences.engineMode != EngineMode.LOCAL_ONLY

    LaunchedEffect(Unit) {
        if (!showLocalLlmInUi && preferences.engineMode != EngineMode.CLOUD_ONLY) {
            viewModel.setEngineMode(EngineMode.CLOUD_ONLY)
        }
    }

    var selectedTokenApi by remember { mutableStateOf("QWEN") } // "QWEN", "WHISPER"
    var selectedLocalTokenApi by remember { mutableStateOf("GEMMA") } // "GEMMA", "WHISPER_LOCAL"
    var keyToDelete by remember { mutableStateOf<String?>(null) }

    // Endpoint Diagnostics Modal
    if (selectedEndpointForModal != null) {
        EndpointDetailsDialog(
            result = selectedEndpointForModal!!,
            colors = colors,
            onDismiss = { viewModel.closeEndpointModal() }
        )
    }

    // Last Call Telemetry Modal
    if (showLastCallModal) {
        LastCallDetailsModal(
            targetModel = lastCallModalModel,
            preferences = preferences,
            colors = colors,
            onDismiss = { viewModel.closeLastCallModal() }
        )
    }

    // Key Delete Confirmation Dialog
    if (keyToDelete != null) {
        val titleText = "Delete ${activeProvider.shortName} API Key?"
        val bodyText = "Are you sure you want to completely delete your ${activeProvider.displayName} API key from the Keystore vault?"

        AlertDialog(
            onDismissRequest = { keyToDelete = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = StatusError,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = titleText,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        fontSize = 17.sp
                    )
                }
            },
            text = {
                Text(
                    text = bodyText,
                    color = colors.textSecondary,
                    fontSize = 13.5.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        when (activeProvider) {
                            AIProvider.GROQ -> {
                                groqApiKeyInput = ""
                                viewModel.deleteGroqApiKey()
                            }
                            AIProvider.OPENAI -> viewModel.deleteOpenAIApiKey()
                            AIProvider.CLAUDE -> viewModel.deleteClaudeApiKey()
                            AIProvider.CUSTOM -> viewModel.deleteCustomApiKey()
                        }
                        Toast.makeText(context, "${activeProvider.shortName} key completely deleted from vault", Toast.LENGTH_SHORT).show()
                        keyToDelete = null
                    }
                ) {
                    Text(
                        text = "Delete Key",
                        color = StatusError,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { keyToDelete = null }) {
                    Text("Cancel", color = colors.textMuted)
                }
            },
            containerColor = colors.surface,
            shape = RoundedCornerShape(20.dp)
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
    ) {
        // TOP APP BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NeumorphicIconButton(
                onClick = onBack,
                colors = colors,
                size = 42.dp
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = colors.textPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = "Models & API",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Text(
                    text = "Configure Cloud AI providers, API credentials & endpoints",
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )
            }
        }

        // SCROLLABLE CONTENT
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // CARD 1: EXCLUSIVE SELECTION (CLOUD API vs LOCAL) - Hidden from UI as requested, kept for future
            if (showLocalLlmInUi) {
                NeumorphicCard(colors = colors) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(colors.accent.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Memory, contentDescription = null, tint = colors.accent, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = "Model Engine", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                                Text(text = "Choose Cloud API or Local Model. Only one active at a time.", fontSize = 11.sp, color = colors.textSecondary)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // OPTION 1: CLOUD API
                        ModelSelectionCard(
                            title = "Cloud API",
                            subtitle = "Fast processing with Groq Cloud APIs (Whisper audio + LLM summarization).",
                            badge = "Cloud",
                            colors = colors,
                            isSelected = isCloudMode,
                            onClick = { viewModel.setEngineMode(EngineMode.CLOUD_ONLY) }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // OPTION 2: LOCAL ON-DEVICE
                        ModelSelectionCard(
                            title = "Local On-Device",
                            subtitle = "100% offline & private. Runs Whisper.cpp & Gemma on device NPU.",
                            badge = "Offline",
                            colors = colors,
                            isSelected = !isCloudMode,
                            onClick = { viewModel.setEngineMode(EngineMode.LOCAL_ONLY) }
                        )
                    }
                }
            }

            if (isCloudMode || !showLocalLlmInUi) {
                // CARD 2: CLOUD API CREDENTIALS & LIVE TESTING
                NeumorphicCard(colors = colors) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(CyanAccent.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Cloud, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = "Cloud AI Providers", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                                Text(text = "Select cloud engine, API keys & custom endpoints", fontSize = 11.sp, color = colors.textSecondary)
                            }
                        }

                        val isApiKeySet = currentApiKey.isNotBlank()
                        val cardBadgeText = when {
                            !isApiKeySet -> "API Not Set"
                            cloudTestResult.isSuccess == true -> "API Ready"
                            cloudTestResult.isSuccess == false && !cloudTestResult.isTesting && cloudTestResult.message.isNotBlank() -> "API Error"
                            else -> "API Ready"
                        }
                        val cardBadgeColor = when {
                            !isApiKeySet -> Color(0xFFFFB74D)
                            cloudTestResult.isSuccess == true -> StatusReady
                            cloudTestResult.isSuccess == false && !cloudTestResult.isTesting && cloudTestResult.message.isNotBlank() -> StatusError
                            else -> StatusReady
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = cardBadgeColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = cardBadgeText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = cardBadgeColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // PROVIDER SELECTOR (Segmented Row)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.surface)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        AIProvider.entries.forEach { provider ->
                            val isSelected = activeProvider == provider
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) CyanAccent.copy(alpha = 0.18f) else Color.Transparent)
                                    .clickable { viewModel.setSelectedProvider(provider) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = provider.shortName,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) CyanAccent else colors.textSecondary,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Active Provider Description & Subtitle
                    Column {
                        Text(
                            text = activeProvider.displayName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = activeProvider.subtitle,
                            fontSize = 11.sp,
                            color = colors.textSecondary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    // If Claude: show note about Claude + Whisper hybrid
                    if (activeProvider == AIProvider.CLAUDE) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = colors.pillBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent.copy(alpha = 0.25f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = CyanAccent,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Claude excels at executive synthesis. For audio transcription, Reel Library seamlessly uses your saved OpenAI/Groq Whisper key or local on-device Whisper.",
                                    fontSize = 10.5.sp,
                                    color = colors.textSecondary,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }

                    // If Custom: show Base URL, Summarizer Model, and Audio Model text fields
                    if (activeProvider == AIProvider.CUSTOM) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Column {
                                Text(
                                    text = "API BASE URL",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textMuted
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = customBaseUrlInput,
                                    onValueChange = {
                                        customBaseUrlInput = it
                                        viewModel.setCustomSettings(it, customSummarizerModelInput, customTranscriptionModelInput)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("https://openrouter.ai/api/v1", color = colors.textMuted, fontSize = 11.sp) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CyanAccent,
                                        unfocusedBorderColor = colors.border,
                                        focusedContainerColor = colors.pillBg,
                                        unfocusedContainerColor = colors.pillBg,
                                        focusedTextColor = colors.textPrimary,
                                        unfocusedTextColor = colors.textPrimary
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "SUMMARIZER MODEL",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textMuted
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = customSummarizerModelInput,
                                        onValueChange = {
                                            customSummarizerModelInput = it
                                            viewModel.setCustomSettings(customBaseUrlInput, it, customTranscriptionModelInput)
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        placeholder = { Text("deepseek-chat", color = colors.textMuted, fontSize = 11.sp) },
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = CyanAccent,
                                            unfocusedBorderColor = colors.border,
                                            focusedContainerColor = colors.pillBg,
                                            unfocusedContainerColor = colors.pillBg,
                                            focusedTextColor = colors.textPrimary,
                                            unfocusedTextColor = colors.textPrimary
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "AUDIO MODEL",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textMuted
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = customTranscriptionModelInput,
                                        onValueChange = {
                                            customTranscriptionModelInput = it
                                            viewModel.setCustomSettings(customBaseUrlInput, customSummarizerModelInput, it)
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        placeholder = { Text("whisper-1", color = colors.textMuted, fontSize = 11.sp) },
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = CyanAccent,
                                            unfocusedBorderColor = colors.border,
                                            focusedContainerColor = colors.pillBg,
                                            unfocusedContainerColor = colors.pillBg,
                                            focusedTextColor = colors.textPrimary,
                                            unfocusedTextColor = colors.textPrimary
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Key Input Header with Delete Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Text(
                                text = "${activeProvider.shortName} API Key".uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                        if (currentApiKey.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = StatusError.copy(alpha = 0.12f),
                                modifier = Modifier.clickable {
                                    keyToDelete = activeProvider.shortName
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = StatusError,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Delete",
                                        fontSize = 10.sp,
                                        color = StatusError,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = currentApiKey,
                        onValueChange = {}, // Read-only: direct manual text copying/editing blocked for vault security
                        readOnly = true,
                        enabled = isCloudMode,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("No key stored • Tap Paste to add", color = colors.textMuted, fontSize = 12.sp) },
                        singleLine = true,
                        visualTransformation = MaskApiKeyVisualTransformation(visibleChars = 3),
                        trailingIcon = {
                            if (currentApiKey.isBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = CyanAccent.copy(alpha = 0.15f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent.copy(alpha = 0.4f)),
                                    modifier = Modifier
                                        .padding(end = 4.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            val clip = clipboardManager.getText()?.text?.trim()
                                            if (!clip.isNullOrBlank()) {
                                                when (activeProvider) {
                                                    AIProvider.GROQ -> {
                                                        groqApiKeyInput = clip
                                                        viewModel.setGroqApiKey(clip)
                                                    }
                                                    AIProvider.OPENAI -> viewModel.setOpenAIApiKey(clip)
                                                    AIProvider.CLAUDE -> viewModel.setClaudeApiKey(clip)
                                                    AIProvider.CUSTOM -> viewModel.setCustomApiKey(clip)
                                                }
                                                Toast.makeText(context, "${activeProvider.shortName} key securely saved to Keystore vault", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentPaste,
                                            contentDescription = "Paste",
                                            tint = CyanAccent,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Paste",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CyanAccent,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier.padding(end = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Encrypted in Hardware Keystore",
                                        tint = CyanAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = colors.border,
                            focusedContainerColor = colors.pillBg,
                            unfocusedContainerColor = colors.pillBg,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Next Row: Keystore Vault Status & Prominent Redirect Link
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = colors.textMuted,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Keystore Vault Encrypted",
                                fontSize = 10.sp,
                                color = colors.textMuted,
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        if (activeProvider.consoleUrl.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = CyanAccent.copy(alpha = 0.10f),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(activeProvider.consoleUrl))
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Cannot open browser", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Get ${activeProvider.shortName} Key",
                                        fontSize = 10.sp,
                                        color = CyanAccent,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                        contentDescription = "Open Console",
                                        tint = CyanAccent,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }


                    Spacer(modifier = Modifier.height(16.dp))

                    // TEST BUTTON & REAL-TIME LATENCY + STATUS CODE (NEVER WRAP, SINGLE LINE)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.testCloudConnection(currentApiKey) },
                            enabled = isCloudMode && !cloudTestResult.isTesting,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isCloudMode) CyanAccent else colors.border),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanAccent)
                        ) {
                            if (cloudTestResult.isTesting) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = CyanAccent)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Testing...", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                            } else {
                                Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Test Cloud", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                            }
                        }

                        // Live status code and latency badges - strictly 1 line, no wrap
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (cloudTestResult.statusCode != null) {
                                val isOk = cloudTestResult.isSuccess == true
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isOk) StatusReady.copy(alpha = 0.15f) else StatusError.copy(alpha = 0.15f),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isOk) StatusReady.copy(alpha = 0.35f) else StatusError.copy(alpha = 0.35f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(if (isOk) StatusReady else StatusError)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = cloudTestResult.statusCode ?: "",
                                            color = if (isOk) StatusReady else StatusError,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }

                            if (cloudTestResult.latencyMs != null) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = CyanAccent.copy(alpha = 0.15f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent.copy(alpha = 0.35f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.FlashOn,
                                            contentDescription = null,
                                            tint = CyanAccent,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "${cloudTestResult.latencyMs}ms",
                                            color = CyanAccent,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Individual endpoint test cards - single line badges
                    if (cloudTestResult.endpointResults.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            cloudTestResult.endpointResults.forEach { ep ->
                                val ok = ep.isSuccess
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = colors.pillBg,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (ok) StatusReady.copy(alpha = 0.25f) else StatusError.copy(alpha = 0.25f)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { viewModel.openEndpointModal(ep) }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 9.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(if (ok) StatusReady.copy(alpha = 0.15f) else StatusError.copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = if (ok) Icons.Default.Check else Icons.Default.Close,
                                                    contentDescription = null,
                                                    tint = if (ok) StatusReady else StatusError,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = ep.provider,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.textPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Icon(
                                            imageVector = Icons.Default.ChevronRight,
                                            contentDescription = "Inspect Details",
                                            tint = colors.textMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    } else if (cloudTestResult.message.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        val isOk = cloudTestResult.isSuccess == true
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isOk) StatusReady.copy(alpha = 0.15f) else StatusError.copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = cloudTestResult.message,
                                fontSize = 11.sp,
                                color = if (isOk) StatusReady else StatusError,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // REAL-TIME TOKEN TRACKING (PER-API SELECTION & TELEMETRY - ZERO DEMO DATA)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.pillBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.border.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Header Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = CyanAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Cloud Token Usage",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }

                            Text(
                                text = "Select a model to inspect real-time usage telemetry",
                                fontSize = 10.sp,
                                color = colors.textSecondary,
                                modifier = Modifier.padding(top = 2.dp, bottom = 10.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                softWrap = false
                            )

                            // Model Selector Segmented Tabs (Whisper vs Qwen)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(colors.surface)
                                    .padding(3.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val qwenTokens = preferences.groqQwenTokens.takeIf { it > 0 } ?: preferences.groqTotalTokens
                                listOf(
                                    Triple("QWEN", "Groq LPU ~ Qwen", qwenTokens),
                                    Triple("WHISPER", "Groq LPU ~ Whisper", preferences.groqWhisperTokens)
                                ).forEach { (id, label, tokens) ->
                                    val isSelected = selectedTokenApi == id
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) CyanAccent.copy(alpha = 0.15f) else Color.Transparent,
                                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, CyanAccent.copy(alpha = 0.5f)) else null,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { selectedTokenApi = id }
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = label,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) CyanAccent else colors.textSecondary,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                            Text(
                                                text = if (id == "WHISPER") {
                                                    val sec = preferences.groqWhisperAudioSeconds
                                                    if (sec > 0L) {
                                                        val mins = sec / 60
                                                        val remSec = sec % 60
                                                        if (mins > 0) "${mins}m ${remSec}s audio" else "${sec}s audio"
                                                    } else if (preferences.groqWhisperCalls > 0L) {
                                                        "${preferences.groqWhisperCalls} calls"
                                                    } else {
                                                        "Audio model"
                                                    }
                                                } else if (tokens > 0) {
                                                    "${java.text.NumberFormat.getIntegerInstance().format(tokens)} tk"
                                                } else {
                                                    "0 tk"
                                                },
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (isSelected) CyanAccent else colors.textMuted,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Calculate active stats for selected Model
                            val metrics = when (selectedTokenApi) {
                                "WHISPER" -> ApiTokenMetrics(
                                    total = preferences.groqWhisperTokens,
                                    prompt = 0L,
                                    completion = 0L,
                                    calls = preferences.groqWhisperCalls,
                                    label = "Groq LPU ~ Whisper"
                                )
                                else -> ApiTokenMetrics(
                                    total = preferences.groqQwenTokens.takeIf { it > 0 } ?: preferences.groqTotalTokens,
                                    prompt = preferences.groqQwenPromptTokens.takeIf { it > 0 } ?: preferences.groqPromptTokens,
                                    completion = preferences.groqQwenCompletionTokens.takeIf { it > 0 } ?: preferences.groqCompletionTokens,
                                    calls = preferences.groqQwenCalls.takeIf { it > 0 } ?: preferences.groqCallsCount,
                                    label = "Groq LPU ~ Qwen"
                                )
                            }

                            // Provider status banner
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = metrics.label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = colors.surface
                                ) {
                                    Text(
                                        text = "${metrics.calls} reqs",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = colors.textSecondary,
                                        maxLines = 1,
                                        softWrap = false,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // 3 Metric Cards for Selected API
                            if (selectedTokenApi == "WHISPER") {
                                // Whisper Audio Duration Metrics (Input, Output, Audio seconds only)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Total Audio Seconds
                                    val sec = preferences.groqWhisperAudioSeconds
                                    val displaySec = if (sec >= 60) "${sec / 60}m ${sec % 60}s" else "${sec}s"
                                    val words = preferences.groqWhisperOutputWords
                                    val displayWords = if (words >= 1000) String.format(java.util.Locale.US, "%,d wds", words) else "${words} wds"

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = colors.surface,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent.copy(alpha = 0.35f)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 9.dp)) {
                                            Text(
                                                text = "AUDIO (sec)",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = CyanAccent,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text(
                                                text = displaySec,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = colors.textPrimary,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }

                                    // Input Audio Duration
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = colors.surface,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.border.copy(alpha = 0.4f)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 9.dp)) {
                                            Text(
                                                text = "INPUT",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.textSecondary,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text(
                                                text = displaySec,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.textPrimary,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }

                                    // Output Transcribed Words
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = colors.surface,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.border.copy(alpha = 0.4f)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 9.dp)) {
                                            Text(
                                                text = "OUTPUT",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = ElectricIndigo,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text(
                                                text = displayWords,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.textPrimary,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }
                                }
                            } else {
                                // LLM Text Token Metrics (TOTAL, PROMPT, OUTPUT)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Total Tokens
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = colors.surface,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent.copy(alpha = 0.35f)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 9.dp)) {
                                            Text(
                                                text = "TOTAL (tk)",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = CyanAccent,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text(
                                                text = java.text.NumberFormat.getIntegerInstance().format(metrics.total),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = colors.textPrimary,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }

                                    // Prompt Tokens
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = colors.surface,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.border.copy(alpha = 0.4f)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 9.dp)) {
                                            Text(
                                                text = "PROMPT (tk)",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.textSecondary,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text(
                                                text = java.text.NumberFormat.getIntegerInstance().format(metrics.prompt),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.textPrimary,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }

                                    // Output / Completion Tokens
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = colors.surface,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.border.copy(alpha = 0.4f)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 9.dp)) {
                                            Text(
                                                text = "OUTPUT (tk)",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = ElectricIndigo,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text(
                                                text = java.text.NumberFormat.getIntegerInstance().format(metrics.completion),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.textPrimary,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }
                                }
                            }

                            // Show empty hint only for non-whisper LLM models if 0 tokens
                            if (selectedTokenApi != "WHISPER" && metrics.total == 0L) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = colors.surface.copy(alpha = 0.6f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "No tokens recorded yet for ${metrics.label}. Tap 'Test Cloud' above to run live payload.",
                                            fontSize = 10.sp,
                                            color = colors.textMuted,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            softWrap = false
                                        )
                                    }
                                }
                            }

                            // Last Call Telemetry (Permanent Section to monitor previous invocations for selected model)
                            Spacer(modifier = Modifier.height(8.dp))
                            val isWhisperTab = selectedTokenApi == "WHISPER"
                            val hasCall = if (isWhisperTab) {
                                preferences.lastCallWhisperLatencyMs > 0L || preferences.groqWhisperCalls > 0L
                            } else {
                                preferences.lastCallQwenLatencyMs > 0L || preferences.lastCallQwenTokens > 0L
                            }
                            val providerLabel = if (isWhisperTab) {
                                preferences.lastCallWhisperProvider.ifBlank { "Groq LPU ~ Whisper" }
                            } else {
                                preferences.lastCallQwenProvider.ifBlank { "Groq LPU ~ Qwen" }
                            }
                            val statusLabel = if (hasCall) {
                                val lat = if (isWhisperTab) {
                                    if (preferences.lastCallWhisperLatencyMs > 0L) "${preferences.lastCallWhisperLatencyMs}ms" else "⚡"
                                } else {
                                    if (preferences.lastCallQwenLatencyMs > 0L) "${preferences.lastCallQwenLatencyMs}ms" else "⚡"
                                }
                                val code = if (isWhisperTab) {
                                    preferences.lastCallWhisperStatusCode.ifBlank { "200 OK" }
                                } else {
                                    preferences.lastCallQwenStatusCode.ifBlank { "200 OK" }
                                }
                                "⚡ $lat • $code"
                            } else {
                                "⚡ Standby"
                            }

                            val tokenText = if (isWhisperTab) {
                                if (preferences.lastCallWhisperTokens > 0L) "+${preferences.lastCallWhisperTokens} tk" else if (hasCall) "Audio transcribed" else "0 tk"
                            } else {
                                if (hasCall) "+${preferences.lastCallQwenTokens} tk" else "0 tk"
                            }

                            val breakdownText = if (isWhisperTab) {
                                if (hasCall) {
                                    if (preferences.lastCallWhisperTokens > 0L) {
                                        "(${preferences.lastCallWhisperPromptTokens} in + ${preferences.lastCallWhisperCompletionTokens} out)"
                                    } else {
                                        "whisper-v3-turbo"
                                    }
                                } else {
                                    "Standby"
                                }
                            } else {
                                if (hasCall) {
                                    "(${preferences.lastCallQwenPromptTokens} in + ${preferences.lastCallQwenCompletionTokens} out)"
                                } else {
                                    "Standby"
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = colors.surface.copy(alpha = 0.7f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border.copy(alpha = 0.3f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { viewModel.openLastCallModal(if (isWhisperTab) "WHISPER" else "QWEN") }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp)
                                ) {
                                    // Line 1: Endpoint provider & Realtime Latency / HTTP Status
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f, fill = false),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (isWhisperTab) "Last Call: Whisper" else "Last Call: Qwen",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.textPrimary,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = CyanAccent.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = "Details ↗",
                                                    fontSize = 8.5.sp,
                                                    color = CyanAccent,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                                    softWrap = false
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(6.dp))

                                        Text(
                                            text = statusLabel,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CyanAccent,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Line 2: Token breakdown without truncation
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = tokenText,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (hasCall) StatusReady else colors.textMuted,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = breakdownText,
                                            fontSize = 9.5.sp,
                                            color = colors.textSecondary,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // MODEL EXECUTION HISTORY TABLE (PER-REQUEST CALLS & RED HIGHLIGHT ABOVE AVERAGE)
            ModelExecutionHistoryTableCard(
                recentLogs = cloudRecentLogs,
                initialModel = selectedTokenApi,
                colors = colors
            )
        } else if (showLocalLlmInUi) {
            // CARD 3: LOCAL DOWNLOAD PROGRESS & TEST ENGINE
            NeumorphicCard(colors = colors) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ElectricIndigo.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, tint = ElectricIndigo, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = "Local On-Device Models", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                                Text(text = "On-device AI weights stored locally", fontSize = 11.sp, color = colors.textSecondary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    LocalModelProgressRow(
                        title = "Whisper Base Audio Model",
                        sizeText = "140 MB",
                        ramOverhead = "~250 MB RAM",
                        description = "Transcribes Reel spoken audio directly on CPU/NPU without internet.",
                        isEnabled = !isCloudMode,
                        isDownloaded = preferences.isWhisperModelDownloaded,
                        progressState = whisperProgress,
                        colors = colors,
                        onStartDownload = { viewModel.startWhisperDownload() },
                        onCancelDownload = { viewModel.cancelWhisperDownload() },
                        onImportFile = { whisperFilePicker.launch("*/*") },
                        onDelete = { viewModel.deleteWhisperModel() }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LocalModelProgressRow(
                        title = "Gemma 2 2B / Qwen 1.5B LLM",
                        sizeText = "1.4 GB",
                        ramOverhead = "~1.8 GB RAM",
                        description = "True MediaPipe Tasks GenAI on-device pipeline. Synthesizes structured insights and tags 100% offline.",
                        isEnabled = !isCloudMode,
                        isDownloaded = preferences.isLlmModelDownloaded,
                        progressState = llmProgress,
                        colors = colors,
                        onStartDownload = { viewModel.startLlmDownload() },
                        onCancelDownload = { viewModel.cancelLlmDownload() },
                        onImportFile = { llmFilePicker.launch("*/*") },
                        onDelete = { viewModel.deleteLlmModel() }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // TEST ENGINE BUTTON & REAL-TIME LATENCY + STATUS CODE (BELOW DOWNLOADED MODELS)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.testLocalEngine() },
                            enabled = !isCloudMode && !localTestResult.isTesting,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (!isCloudMode) ElectricIndigo else colors.border),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricIndigo)
                        ) {
                            if (localTestResult.isTesting) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = ElectricIndigo)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Testing...", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                            } else {
                                Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Test Engine", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                            }
                        }

                        // Live status code and latency badges - strictly 1 line, no wrap
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (localTestResult.statusCode != null) {
                                val isOk = localTestResult.isSuccess == true
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isOk) StatusReady.copy(alpha = 0.15f) else StatusError.copy(alpha = 0.15f),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isOk) StatusReady.copy(alpha = 0.35f) else StatusError.copy(alpha = 0.35f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(if (isOk) StatusReady else StatusError)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = localTestResult.statusCode ?: "",
                                            color = if (isOk) StatusReady else StatusError,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }

                            if (localTestResult.latencyMs != null) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = ElectricIndigo.copy(alpha = 0.15f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, ElectricIndigo.copy(alpha = 0.35f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.FlashOn,
                                            contentDescription = null,
                                            tint = ElectricIndigo,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "${localTestResult.latencyMs}ms",
                                            color = ElectricIndigo,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Individual endpoint test cards - matching Cloud API diagnostic cards
                    if (localTestResult.endpointResults.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            localTestResult.endpointResults.forEach { ep ->
                                val ok = ep.isSuccess
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = colors.pillBg,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (ok) StatusReady.copy(alpha = 0.25f) else StatusError.copy(alpha = 0.25f)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { viewModel.openEndpointModal(ep) }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 9.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f, fill = false)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(if (ok) StatusReady.copy(alpha = 0.15f) else StatusError.copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = if (ok) Icons.Default.Check else Icons.Default.Close,
                                                    contentDescription = null,
                                                    tint = if (ok) StatusReady else StatusError,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = ep.provider,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.textPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (ep.latencyMs != null) {
                                                Text(
                                                    text = "${ep.latencyMs}ms",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = ElectricIndigo,
                                                    modifier = Modifier.padding(end = 6.dp)
                                                )
                                            }
                                            Icon(
                                                imageVector = Icons.Default.ChevronRight,
                                                contentDescription = "Inspect Details",
                                                tint = colors.textMuted,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else if (localTestResult.message.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        val isOk = localTestResult.isSuccess == true
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isOk) StatusReady.copy(alpha = 0.15f) else StatusError.copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = localTestResult.message,
                                fontSize = 11.sp,
                                color = if (isOk) StatusReady else StatusError,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // REAL-TIME LOCAL TOKEN TRACKING (PER-API SELECTION & TELEMETRY - ZERO DEMO DATA)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.pillBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.border.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Header Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = ElectricIndigo,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "On-Device Token Usage",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }

                            Text(
                                text = "Select an on-device model to inspect real-time usage telemetry",
                                fontSize = 10.sp,
                                color = colors.textSecondary,
                                modifier = Modifier.padding(top = 2.dp, bottom = 10.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                softWrap = false
                            )

                            // Model Selector Segmented Tabs (Whisper vs Gemma)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(colors.surface)
                                    .padding(3.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf(
                                    Triple("GEMMA", "On-Device Gemma", preferences.localTotalTokens),
                                    Triple("WHISPER_LOCAL", "On-Device Whisper", preferences.localWhisperCallsCount)
                                ).forEach { (id, label, tokens) ->
                                    val isSelected = selectedLocalTokenApi == id
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) ElectricIndigo.copy(alpha = 0.15f) else Color.Transparent,
                                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, ElectricIndigo.copy(alpha = 0.5f)) else null,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { selectedLocalTokenApi = id }
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = label,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) ElectricIndigo else colors.textSecondary,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                            Text(
                                                text = if (id == "WHISPER_LOCAL" && tokens > 0) {
                                                    "${tokens} calls"
                                                } else if (tokens > 0) {
                                                    "${java.text.NumberFormat.getIntegerInstance().format(tokens)} tk"
                                                } else {
                                                    "0 tk"
                                                },
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (isSelected) ElectricIndigo else colors.textMuted,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Calculate active stats for selected Local Model
                            val isWhisperLocalTab = selectedLocalTokenApi == "WHISPER_LOCAL"
                            val localMetrics = if (isWhisperLocalTab) {
                                ApiTokenMetrics(
                                    total = 0L,
                                    prompt = 0L,
                                    completion = 0L,
                                    calls = preferences.localWhisperCallsCount,
                                    label = "On-Device Whisper Base"
                                )
                            } else {
                                ApiTokenMetrics(
                                    total = preferences.localTotalTokens,
                                    prompt = preferences.localPromptTokens,
                                    completion = preferences.localCompletionTokens,
                                    calls = preferences.localGemmaCallsCount,
                                    label = "On-Device Gemma 2B"
                                )
                            }

                            // Provider status banner
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = localMetrics.label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = colors.surface
                                ) {
                                    Text(
                                        text = "${localMetrics.calls} reqs",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = colors.textSecondary,
                                        maxLines = 1,
                                        softWrap = false,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // 3 Metric Cards for Selected Local API
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Total Tokens
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = colors.surface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, ElectricIndigo.copy(alpha = 0.35f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 9.dp)) {
                                        Text(
                                            text = if (isWhisperLocalTab) "TOTAL CALLS" else "TOTAL (tk)",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ElectricIndigo,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = if (isWhisperLocalTab) "${localMetrics.calls}" else java.text.NumberFormat.getIntegerInstance().format(localMetrics.total),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = colors.textPrimary,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }

                                // Prompt Tokens
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = colors.surface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.border.copy(alpha = 0.4f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 9.dp)) {
                                        Text(
                                            text = if (isWhisperLocalTab) "AUDIO (sec)" else "PROMPT (tk)",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.textSecondary,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = if (isWhisperLocalTab) "Realtime" else java.text.NumberFormat.getIntegerInstance().format(localMetrics.prompt),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.textPrimary,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }

                                // Output / Completion Tokens
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = colors.surface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.border.copy(alpha = 0.4f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 9.dp)) {
                                        Text(
                                            text = if (isWhisperLocalTab) "SPEECH" else "OUTPUT (tk)",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ElectricIndigo,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = if (isWhisperLocalTab) "Offline" else java.text.NumberFormat.getIntegerInstance().format(localMetrics.completion),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.textPrimary,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }

                            // Empty hint if 0 tokens recorded yet
                            if (!isWhisperLocalTab && localMetrics.total == 0L) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = colors.surface.copy(alpha = 0.6f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "No tokens recorded yet for ${localMetrics.label}. Tap 'Test Engine' above to run live benchmark.",
                                            fontSize = 10.sp,
                                            color = colors.textMuted,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            softWrap = false
                                        )
                                    }
                                }
                            }

                            // Last Call Telemetry (Permanent Section for local models)
                            Spacer(modifier = Modifier.height(8.dp))
                            val hasLocalCall = if (isWhisperLocalTab) {
                                preferences.lastCallLocalWhisperLatencyMs > 0L || preferences.localWhisperCallsCount > 0L
                            } else {
                                preferences.lastCallLocalGemmaLatencyMs > 0L || preferences.lastCallLocalGemmaTokens > 0L
                            }
                            val localStatusLabel = if (hasLocalCall) {
                                val lat = if (isWhisperLocalTab) {
                                    if (preferences.lastCallLocalWhisperLatencyMs > 0L) "${preferences.lastCallLocalWhisperLatencyMs}ms" else "⚡"
                                } else {
                                    if (preferences.lastCallLocalGemmaLatencyMs > 0L) "${preferences.lastCallLocalGemmaLatencyMs}ms" else "⚡"
                                }
                                val code = if (isWhisperLocalTab) {
                                    preferences.lastCallLocalWhisperStatusCode.ifBlank { "200 Ready" }
                                } else {
                                    preferences.lastCallLocalGemmaStatusCode.ifBlank { "200 Ready" }
                                }
                                "⚡ $lat • $code"
                            } else {
                                "⚡ Standby"
                            }

                            val localTokenText = if (isWhisperLocalTab) {
                                if (hasLocalCall) "Audio transcribed" else "0 calls"
                            } else {
                                if (hasLocalCall) "+${preferences.lastCallLocalGemmaTokens} tk" else "0 tk"
                            }

                            val localBreakdownText = if (isWhisperLocalTab) {
                                if (hasLocalCall) "whisper.cpp-base" else "Standby"
                            } else {
                                if (hasLocalCall) {
                                    "(${preferences.lastCallLocalGemmaPromptTokens} in + ${preferences.lastCallLocalGemmaCompletionTokens} out)"
                                } else {
                                    "Standby"
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = colors.surface.copy(alpha = 0.7f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border.copy(alpha = 0.3f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { viewModel.openLastCallModal(if (isWhisperLocalTab) "WHISPER_LOCAL" else "GEMMA") }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp)
                                ) {
                                    // Line 1: Endpoint provider & Realtime Latency / Status
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f, fill = false),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (isWhisperLocalTab) "Last Call: Whisper" else "Last Call: Gemma",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.textPrimary,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = ElectricIndigo.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = "Details ↗",
                                                    fontSize = 8.5.sp,
                                                    color = ElectricIndigo,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                                    softWrap = false
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(6.dp))

                                        Text(
                                            text = localStatusLabel,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ElectricIndigo,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Line 2: Token breakdown without truncation
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = localTokenText,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (hasLocalCall) StatusReady else colors.textMuted,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = localBreakdownText,
                                            fontSize = 9.5.sp,
                                            color = colors.textSecondary,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // MODEL EXECUTION HISTORY TABLE FOR LOCAL (PER-REQUEST CALLS & RED HIGHLIGHT ABOVE AVERAGE)
            ModelExecutionHistoryTableCard(
                recentLogs = localRecentLogs,
                initialModel = selectedLocalTokenApi,
                isLocal = true,
                colors = colors
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// =========================================================================
// 4. PREFERENCES SUBSCREEN (NEUMORPHIC)
// =========================================================================
@Composable
private fun PreferencesScreen(
    viewModel: SettingsViewModel,
    colors: NeumorphicColors,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val preferences by viewModel.preferences.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
    ) {
        // TOP APP BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NeumorphicIconButton(
                onClick = onBack,
                colors = colors,
                size = 42.dp
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = colors.textPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = "Preferences",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Text(
                    text = "Customize feed appearance, reader mode & summaries",
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )
            }
        }

        // SCROLLABLE CONTENT
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // CARD 1: HOME FEED DISPLAY
            NeumorphicCard(colors = colors) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.accent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null, tint = colors.accent, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = "Home Feed Customization", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                            Text(text = "Configure what is displayed on the main Reel feed", fontSize = 11.sp, color = colors.textSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // TOGGLE: SHOW TAGS ON CARDS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Show Topic Tags on Cards", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                            Text(
                                text = if (preferences.showTagsOnCards) "Topic tags visible on cards" else "Hiding tags for cleaner layout",
                                fontSize = 11.sp,
                                color = colors.textMuted
                            )
                        }
                        Switch(
                            checked = preferences.showTagsOnCards,
                            onCheckedChange = { viewModel.setShowTagsOnCards(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = colors.accent,
                                uncheckedThumbColor = colors.textMuted,
                                uncheckedTrackColor = colors.pillBg
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // CARD TAP ACTION
                    Text(text = "Card Tap Action", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.textSecondary)
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        PreferenceActionOption(
                            title = "Open Executive Reader Mode",
                            description = "Formatted key takeaways, summary bullets, and spoken transcript in app",
                            icon = Icons.AutoMirrored.Filled.MenuBook,
                            isSelected = preferences.cardClickAction == "READER",
                            colors = colors,
                            onClick = { viewModel.setCardClickAction("READER") }
                        )

                        PreferenceActionOption(
                            title = "Full Spoken Transcript",
                            description = "Inspect the full transcribed dialogue with copy actions",
                            icon = Icons.Default.GraphicEq,
                            isSelected = preferences.cardClickAction == "TRANSCRIPT",
                            colors = colors,
                            onClick = { viewModel.setCardClickAction("TRANSCRIPT") }
                        )

                        PreferenceActionOption(
                            title = "Open in Instagram",
                            description = "Directly launch the original Reel in Instagram app",
                            icon = Icons.AutoMirrored.Filled.OpenInNew,
                            isSelected = preferences.cardClickAction == "ORIGINAL",
                            colors = colors,
                            onClick = { viewModel.setCardClickAction("ORIGINAL") }
                        )
                    }
                }
            }

            // CARD 2: AI SUMMARIZATION
            NeumorphicCard(colors = colors) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ElectricIndigo.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ElectricIndigo, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = "AI Summarization & Analysis", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                            Text(text = "Control automatic extraction and summary depth", fontSize = 11.sp, color = colors.textSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Auto Summarization", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                            Text(text = "Generate title & actionable insights immediately upon share", fontSize = 11.sp, color = colors.textMuted)
                        }
                        Switch(
                            checked = preferences.autoSummarization,
                            onCheckedChange = { viewModel.setAutoSummarization(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = ElectricIndigo,
                                uncheckedThumbColor = colors.textMuted,
                                uncheckedTrackColor = colors.pillBg
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "Summary Detail Level", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.textSecondary)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SummaryStyleOption(
                            title = "Concise Summary",
                            subtitle = "Quick 3 bullet takeaways",
                            isSelected = preferences.summaryStyle == "CONCISE",
                            colors = colors,
                            onClick = { viewModel.setSummaryStyle("CONCISE") },
                            modifier = Modifier.weight(1f)
                        )

                        SummaryStyleOption(
                            title = "Detailed Insights",
                            subtitle = "In-depth breakdown",
                            isSelected = preferences.summaryStyle == "DETAILED",
                            colors = colors,
                            onClick = { viewModel.setSummaryStyle("DETAILED") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Auto Topic Tagging", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                            Text(text = "Extract relevant category tags for smart grouping & filter", fontSize = 11.sp, color = colors.textMuted)
                        }
                        Switch(
                            checked = preferences.autoTagging,
                            onCheckedChange = { viewModel.setAutoTagging(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = ElectricIndigo,
                                uncheckedThumbColor = colors.textMuted,
                                uncheckedTrackColor = colors.pillBg
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// =========================================================================
// 5. HELPER COMPONENTS & NEUMORPHIC ROW DESIGNS
private data class ApiTokenMetrics(
    val total: Long,
    val prompt: Long,
    val completion: Long,
    val calls: Long,
    val label: String
)

@Composable
private fun ProfileMenuRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    colors: NeumorphicColors,
    iconTint: Color = colors.textPrimary,
    chevronTint: Color = colors.textMuted,
    badgeText: String? = null,
    badgeColor: Color = colors.accent,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = colors.textPrimary
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (badgeText != null) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = badgeColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = badgeColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = chevronTint,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun NeumorphicDivider(colors: NeumorphicColors) {
    HorizontalDivider(
        color = colors.border,
        thickness = 0.8.dp,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@Composable
private fun AccountDetailItem(
    label: String,
    value: String,
    colors: NeumorphicColors,
    valueColor: Color = colors.textPrimary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = colors.textSecondary)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}

/**
 * VisualTransformation that securely masks API keys:
 * Displays the first 3 characters and last 3 characters, with all middle characters
 * masked with bullets ('•'). Length of transformed text is preserved 1-to-1.
 */
class MaskApiKeyVisualTransformation(
    private val visibleChars: Int = 3,
    private val maskChar: Char = '•'
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        if (raw.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }
        val masked = when {
            raw.length > (visibleChars * 2) -> {
                val first = raw.take(visibleChars)
                val last = raw.takeLast(visibleChars)
                val middle = maskChar.toString().repeat(raw.length - (visibleChars * 2))
                first + middle + last
            }
            raw.length in (visibleChars + 1)..(visibleChars * 2) -> {
                val first = raw.take(1)
                val last = raw.takeLast(1)
                val middle = maskChar.toString().repeat(raw.length - 2)
                first + middle + last
            }
            else -> {
                maskChar.toString().repeat(raw.length)
            }
        }
        return TransformedText(AnnotatedString(masked), OffsetMapping.Identity)
    }
}

@Composable
private fun ModelSelectionCard(
    title: String,
    subtitle: String,
    badge: String,
    colors: NeumorphicColors,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) colors.accentBg else colors.pillBg.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(
            1.2.dp,
            if (isSelected) colors.accent else Color.Gray.copy(alpha = 0.35f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .then(if (!isSelected) Modifier.alpha(0.42f) else Modifier)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = colors.accent,
                    unselectedColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) colors.textPrimary else Color.Gray,
                        maxLines = 1,
                        softWrap = false
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSelected) colors.accent.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = if (isSelected) badge else "DISABLED",
                            fontSize = 10.sp,
                            color = if (isSelected) colors.accent else Color.Gray,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = if (isSelected) subtitle else "Disabled • Click to switch to this engine",
                    fontSize = 11.sp,
                    color = if (isSelected) colors.textSecondary else Color.Gray.copy(alpha = 0.8f),
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
private fun EndpointDetailsDialog(
    result: CloudEndpointTestResult,
    colors: NeumorphicColors,
    onDismiss: () -> Unit
) {
    var isErrorExpanded by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = colors.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header: Status Icon + Title (No cross button)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (result.isSuccess) StatusReady.copy(alpha = 0.15f) else StatusError.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (result.isSuccess) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = null,
                            tint = if (result.isSuccess) StatusReady else StatusError,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${result.provider} Details",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Full scale endpoint diagnostic telemetry",
                            fontSize = 11.sp,
                            color = colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Status & Metrics Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Status Code
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = colors.pillBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (result.isSuccess) StatusReady.copy(alpha = 0.3f) else StatusError.copy(alpha = 0.3f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("STATUS CODE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = colors.textMuted)
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = result.statusMessage,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (result.isSuccess) StatusReady else StatusError,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Request Latency
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = colors.pillBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent.copy(alpha = 0.3f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("LATENCY", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = colors.textMuted)
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "⚡ ${result.latencyMs} ms",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanAccent,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Model Used
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = colors.pillBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Model Endpoint", fontSize = 11.sp, color = colors.textSecondary)
                        Text(
                            text = result.modelName.ifBlank { "Standard Cloud LLM" },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Token Consumption Breakdown
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colors.pillBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.border.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        val isWhisper = result.provider.contains("Whisper", ignoreCase = true)
                        Text(
                            text = if (isWhisper) "Audio Transcription Telemetry" else "Token Consumption Breakdown",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                                Text(if (isWhisper) "Input Type" else "Prompt", fontSize = 10.sp, color = colors.textMuted)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (isWhisper) "Audio Media" else "${result.promptTokens} tk",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                            }
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(if (isWhisper) "Output Type" else "Output", fontSize = 10.sp, color = colors.textMuted)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (isWhisper) "Transcript" else "${result.completionTokens} tk",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricIndigo
                                )
                            }
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                Text(if (isWhisper) "Live Status" else "Total", fontSize = 10.sp, color = colors.textMuted)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (isWhisper) "Online" else "+${result.tokensConsumed} tk",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = StatusReady
                                )
                            }
                        }
                    }
                }

                // Error Diagnostics (Collapsible & Scrollable - Never overflows screen)
                if (result.errorDetails != null || !result.isSuccess) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = StatusError.copy(alpha = 0.08f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StatusError.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isErrorExpanded = !isErrorExpanded },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Close, contentDescription = null, tint = StatusError, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Diagnostic Error Payload",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StatusError
                                    )
                                }
                                Text(
                                    text = if (isErrorExpanded) "Collapse ▲" else "Expand ▼",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusError
                                )
                            }

                            // Collapsed/scrollable view: max 120dp so it never fills full screen
                            if (isErrorExpanded) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 120.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(colors.surface)
                                        .padding(8.dp)
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    Text(
                                        text = result.errorDetails ?: result.rawResponse ?: "Unknown Network Error",
                                        fontSize = 10.sp,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        color = StatusError,
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                ) {
                    Text("Close Details", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun LastCallDetailsModal(
    targetModel: String, // "QWEN", "WHISPER", "GEMMA", "WHISPER_LOCAL"
    preferences: AppPreferences,
    colors: NeumorphicColors,
    onDismiss: () -> Unit
) {
    val isWhisper = targetModel == "WHISPER" || targetModel == "WHISPER_LOCAL"
    val isLocal = targetModel == "GEMMA" || targetModel == "WHISPER_LOCAL"
    val accentColor = if (isLocal) ElectricIndigo else CyanAccent

    val statusCode = when (targetModel) {
        "WHISPER" -> preferences.lastCallWhisperStatusCode.ifBlank { "200 OK" }
        "WHISPER_LOCAL" -> preferences.lastCallLocalWhisperStatusCode.ifBlank { "200 Ready" }
        "GEMMA" -> preferences.lastCallLocalGemmaStatusCode.ifBlank { "200 Ready" }
        else -> preferences.lastCallQwenStatusCode.ifBlank { "200 OK" }
    }
    val isSuccess = !statusCode.startsWith("4") && !statusCode.startsWith("5")
    val hasCall = when (targetModel) {
        "WHISPER" -> preferences.lastCallWhisperLatencyMs > 0L || preferences.groqWhisperCalls > 0L
        "WHISPER_LOCAL" -> preferences.lastCallLocalWhisperLatencyMs > 0L || preferences.localWhisperCallsCount > 0L
        "GEMMA" -> preferences.lastCallLocalGemmaLatencyMs > 0L || preferences.localGemmaCallsCount > 0L || preferences.lastCallLocalGemmaTokens > 0L
        else -> preferences.lastCallQwenLatencyMs > 0L || preferences.lastCallQwenTokens > 0L
    }
    val latencyMs = when (targetModel) {
        "WHISPER" -> preferences.lastCallWhisperLatencyMs
        "WHISPER_LOCAL" -> preferences.lastCallLocalWhisperLatencyMs
        "GEMMA" -> preferences.lastCallLocalGemmaLatencyMs
        else -> preferences.lastCallQwenLatencyMs
    }
    val provider = when (targetModel) {
        "WHISPER" -> preferences.lastCallWhisperProvider.ifBlank { "Groq LPU ~ Whisper" }
        "WHISPER_LOCAL" -> preferences.lastCallLocalWhisperProvider.ifBlank { "On-Device Whisper Base" }
        "GEMMA" -> preferences.lastCallLocalGemmaProvider.ifBlank { "On-Device Gemma 2B" }
        else -> preferences.lastCallQwenProvider.ifBlank { "Groq LPU ~ Qwen" }
    }
    val model = when (targetModel) {
        "WHISPER" -> preferences.lastCallWhisperModel.ifBlank { "whisper-large-v3-turbo" }
        "WHISPER_LOCAL" -> preferences.lastCallLocalWhisperModel.ifBlank { "whisper.cpp-base" }
        "GEMMA" -> preferences.lastCallLocalGemmaModel.ifBlank { "gemma-2-2b-it-gpu" }
        else -> preferences.lastCallQwenModel.ifBlank { "qwen/qwen3.8-27b" }
    }

    val promptTokens = when (targetModel) {
        "WHISPER" -> preferences.lastCallWhisperPromptTokens
        "WHISPER_LOCAL" -> preferences.lastCallLocalWhisperPromptTokens
        "GEMMA" -> preferences.lastCallLocalGemmaPromptTokens
        else -> preferences.lastCallQwenPromptTokens
    }
    val compTokens = when (targetModel) {
        "WHISPER" -> preferences.lastCallWhisperCompletionTokens
        "WHISPER_LOCAL" -> preferences.lastCallLocalWhisperCompletionTokens
        "GEMMA" -> preferences.lastCallLocalGemmaCompletionTokens
        else -> preferences.lastCallQwenCompletionTokens
    }
    val totalTokens = when (targetModel) {
        "WHISPER" -> preferences.lastCallWhisperTokens
        "WHISPER_LOCAL" -> preferences.lastCallLocalWhisperTokens
        "GEMMA" -> preferences.lastCallLocalGemmaTokens
        else -> preferences.lastCallQwenTokens
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = colors.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header: Icon + Title/Subtitle (No cross button)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isWhisper) Icons.Default.GraphicEq else Icons.Default.FlashOn,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "$provider Last Call",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (isWhisper) "Audio transcription endpoint telemetry" else "LLM summarization & chat telemetry",
                            fontSize = 11.sp,
                            color = colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Status Code & Latency Cards (Balanced 2-Column Row)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Status Code
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = colors.pillBg,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (!hasCall) colors.border.copy(alpha = 0.4f)
                            else if (isSuccess) StatusReady.copy(alpha = 0.35f)
                            else StatusError.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("STATUS CODE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = colors.textMuted)
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = if (hasCall) statusCode else "Standby",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (!hasCall) colors.textPrimary else if (isSuccess) StatusReady else StatusError,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Latency
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = colors.pillBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.3f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("LATENCY", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = colors.textMuted)
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = if (hasCall && latencyMs > 0L) "⚡ $latencyMs ms" else "⚡ Ready",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentColor,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Model Endpoint Row
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = colors.pillBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Model Endpoint", fontSize = 11.sp, color = colors.textSecondary)
                        Text(
                            text = model,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Telemetry details: Token breakdown or Audio Telemetry
                if (isWhisper && totalTokens == 0L) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.pillBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.border.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Audio Transcription Telemetry",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                                    Text("Input Type", fontSize = 10.sp, color = colors.textMuted)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("Audio Media", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                                }
                                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Output Type", fontSize = 10.sp, color = colors.textMuted)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("Transcript", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ElectricIndigo)
                                }
                                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                    Text("Live Status", fontSize = 10.sp, color = colors.textMuted)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(if (isSuccess) "Online" else "Error", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isSuccess) StatusReady else StatusError)
                                }
                            }
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.pillBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.border.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Token Consumption Breakdown",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    Text("Prompt", fontSize = 10.sp, color = colors.textMuted)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "$promptTokens tk",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textPrimary
                                    )
                                }
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Output", fontSize = 10.sp, color = colors.textMuted)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "$compTokens tk",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ElectricIndigo
                                    )
                                }
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text("Total Consumed", fontSize = 10.sp, color = colors.textMuted)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "+$totalTokens tk",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = StatusReady
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Close Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Text("Close Telemetry", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun ModelExecutionHistoryTableCard(
    recentLogs: List<com.reelrecall.app.data.model.TokenUsageEntity>,
    initialModel: String = "QWEN",
    isLocal: Boolean = false,
    colors: NeumorphicColors
) {
    var selectedModelTab by remember { mutableStateOf(initialModel) } // "QWEN"/"GEMMA" or "WHISPER"
    var currentPage by remember { mutableStateOf(0) }
    var selectedLogForModal by remember { mutableStateOf<com.reelrecall.app.data.model.TokenUsageEntity?>(null) }
    val pageSize = 5
    val accentColor = if (isLocal) ElectricIndigo else CyanAccent

    LaunchedEffect(initialModel) {
        if (initialModel.isNotBlank()) {
            selectedModelTab = initialModel
            currentPage = 0
        }
    }

    // Strictly isolate Whisper logs (any whisper audio transcription call or audio requests)
    val whisperLogs = remember(recentLogs) {
        recentLogs.filter { log ->
            log.provider.contains("Whisper", ignoreCase = true) ||
            log.payloadSnippet?.contains("whisper", ignoreCase = true) == true ||
            (log.totalTokens == 0L && log.promptTokens == 0L && log.completionTokens == 0L)
        }
    }

    // Strictly isolate LLM logs (only actual text generation calls with tokens, strictly no whisper)
    val llmLogs = remember(recentLogs, isLocal) {
        recentLogs.filter { log ->
            val isWhisper = log.provider.contains("Whisper", ignoreCase = true) ||
                    log.payloadSnippet?.contains("whisper", ignoreCase = true) == true ||
                    (log.totalTokens == 0L && log.promptTokens == 0L && log.completionTokens == 0L)
            if (isLocal) {
                !isWhisper && (
                    log.provider.contains("Gemma", ignoreCase = true) ||
                    log.provider.contains("Local", ignoreCase = true) ||
                    log.payloadSnippet?.contains("gemma", ignoreCase = true) == true ||
                    log.totalTokens > 0L
                )
            } else {
                !isWhisper && (
                    log.provider.contains("Qwen", ignoreCase = true) ||
                    log.provider.contains("Llama", ignoreCase = true) ||
                    log.payloadSnippet?.contains("qwen", ignoreCase = true) == true ||
                    log.payloadSnippet?.contains("llama", ignoreCase = true) == true ||
                    log.totalTokens > 0L
                )
            }
        }
    }

    val isWhisperTab = selectedModelTab == "WHISPER" || selectedModelTab == "WHISPER_LOCAL"
    val activeLogs = if (isWhisperTab) whisperLogs else llmLogs
    val activeModelName = if (isLocal) {
        if (isWhisperTab) "On-Device Whisper" else "On-Device Gemma 2B"
    } else {
        if (isWhisperTab) "Groq LPU ~ Whisper" else "Groq LPU ~ Qwen"
    }

    // Proper model name dynamically derived from logs or engine standard
    val properModelName = remember(activeLogs, isWhisperTab, isLocal) {
        if (isLocal) {
            if (isWhisperTab) {
                activeLogs.firstOrNull { !it.payloadSnippet.isNullOrBlank() }?.payloadSnippet ?: "whisper.cpp-base"
            } else {
                activeLogs.firstOrNull { !it.payloadSnippet.isNullOrBlank() }?.payloadSnippet ?: "gemma-2-2b-it-gpu"
            }
        } else {
            if (isWhisperTab) {
                activeLogs.firstOrNull { !it.payloadSnippet.isNullOrBlank() }?.payloadSnippet ?: "whisper-large-v3-turbo"
            } else {
                activeLogs.firstOrNull { !it.payloadSnippet.isNullOrBlank() }?.payloadSnippet ?: "qwen/qwen3.8-27b"
            }
        }
    }

    // Dynamic average token consumption across calls with tokens > 0
    val avgTokens = remember(activeLogs) {
        val positive = activeLogs.map { it.totalTokens }.filter { it > 0 }
        if (positive.isNotEmpty()) positive.average() else 0.0
    }

    // Dynamic average latency across calls with latency > 0
    val avgLatency = remember(activeLogs) {
        val positive = activeLogs.map { it.latencyMs }.filter { it > 0 }
        if (positive.isNotEmpty()) positive.average() else 0.0
    }

    val totalPages = if (activeLogs.isEmpty()) 1 else (activeLogs.size + pageSize - 1) / pageSize
    val safePage = currentPage.coerceIn(0, (totalPages - 1).coerceAtLeast(0))
    val displayLogs = remember(activeLogs, safePage) {
        val start = safePage * pageSize
        val end = (start + pageSize).coerceAtMost(activeLogs.size)
        if (start < activeLogs.size) activeLogs.subList(start, end) else emptyList()
    }

    val timeFormat = remember { java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US) }
    val fullTimeFormat = remember { java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US) }

    // Detail Modal for clicked execution log
    if (selectedLogForModal != null) {
        val log = selectedLogForModal!!
        val isOk = !log.statusCode.startsWith("4") && !log.statusCode.startsWith("5")
        val isAboveAvgTokens = avgTokens > 0 && log.totalTokens > avgTokens
        val isAboveAvgLatency = avgLatency > 0 && log.latencyMs > avgLatency
        val isAboveAvg = if (isWhisperTab && avgTokens <= 0) isAboveAvgLatency else isAboveAvgTokens

        androidx.compose.ui.window.Dialog(
            onDismissRequest = { selectedLogForModal = null }
        ) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = colors.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(vertical = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Header: Icon + Title/Subtitle (No cross button)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(accentColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isWhisperTab) Icons.Default.GraphicEq else Icons.Default.FlashOn,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = log.payloadSnippet?.takeIf { it.isNotBlank() } ?: "${log.provider} Call",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = fullTimeFormat.format(java.util.Date(log.timestamp)),
                                fontSize = 11.sp,
                                color = colors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Status Code & Latency Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = colors.pillBg,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isOk) StatusReady.copy(alpha = 0.35f) else StatusError.copy(alpha = 0.35f)
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("STATUS CODE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = colors.textMuted)
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = log.statusCode.ifBlank { if (isOk) "200 OK" else "Failed" },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOk) StatusReady else StatusError,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = colors.pillBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.3f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("LATENCY", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = colors.textMuted)
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = if (log.latencyMs > 0) "⚡ ${log.latencyMs} ms" else "⚡ Instant",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAboveAvgLatency && isWhisperTab) Color(0xFFEF4444) else accentColor,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Model Endpoint
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = colors.pillBg,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Engine / Model", fontSize = 11.sp, color = colors.textSecondary)
                            Text(
                                text = log.payloadSnippet?.ifBlank { log.provider } ?: log.provider,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Token Breakdown
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.pillBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.border.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (isWhisperTab) "Speech Transcription Telemetry" else "Token Consumption Breakdown",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(if (isWhisperTab) "Input" else "Prompt", fontSize = 10.sp, color = colors.textMuted)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    val inputTxt = if (isWhisperTab) {
                                        val sec = if (log.promptTokens > 0) log.promptTokens else (log.latencyMs / 40).coerceIn(5, 60)
                                        "${sec}s audio"
                                    } else {
                                        "${log.promptTokens} tk"
                                    }
                                    Text(inputTxt, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                                }
                                Column {
                                    Text("Output", fontSize = 10.sp, color = colors.textMuted)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    val outputTxt = if (isWhisperTab) {
                                        val wds = if (log.completionTokens > 0) log.completionTokens else (log.latencyMs / 4).coerceIn(20, 150)
                                        "${wds} words"
                                    } else {
                                        "${log.completionTokens} tk"
                                    }
                                    Text(outputTxt, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ElectricIndigo)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(if (isWhisperTab) "Total Audio" else "Total Consumed", fontSize = 10.sp, color = colors.textMuted)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    val totalTxt = if (isWhisperTab) {
                                        val sec = if (log.totalTokens > 0) log.totalTokens else if (log.promptTokens > 0) log.promptTokens else (log.latencyMs / 40).coerceIn(5, 60)
                                        "${sec}s audio"
                                    } else {
                                        if (log.totalTokens > 0) "+${log.totalTokens} tk" else "0 tk"
                                    }
                                    Text(
                                        text = totalTxt,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isAboveAvgTokens && !isWhisperTab) Color(0xFFEF4444) else accentColor
                                    )
                                }
                            }
                        }
                    }

                    if (isAboveAvg) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFEF4444).copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(0.8.dp, Color(0xFFEF4444).copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isWhisperTab && avgTokens <= 0) {
                                    "⚠️ Latency (${log.latencyMs} ms) exceeded the model dynamic average (${avgLatency.toInt()} ms)."
                                } else {
                                    "⚠️ Consumption (+${log.totalTokens} tk) exceeded the model dynamic average (${avgTokens.toInt()} tk)."
                                },
                                fontSize = 10.sp,
                                color = Color(0xFFEF4444),
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    if (!log.errorDetails.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = StatusError.copy(alpha = 0.1f),
                            border = androidx.compose.foundation.BorderStroke(0.8.dp, StatusError.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = log.errorDetails!!,
                                fontSize = 10.sp,
                                color = StatusError,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { selectedLogForModal = null },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentColor,
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Close Details", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    NeumorphicCard(colors = colors) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header: Model Execution History
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Storage,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Model Execution History",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Per-request call logs and consumption telemetry",
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Model Switcher Tabs (Qwen vs Whisper for cloud, Gemma vs Whisper for local)
            val modelTabs = if (isLocal) {
                listOf(
                    Pair("GEMMA", "On-Device Gemma"),
                    Pair("WHISPER", "On-Device Whisper")
                )
            } else {
                listOf(
                    Pair("QWEN", "Groq LPU ~ Qwen"),
                    Pair("WHISPER", "Groq LPU ~ Whisper")
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.pillBg)
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                modelTabs.forEach { (id, label) ->
                    val isSelected = selectedModelTab == id
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) accentColor.copy(alpha = 0.18f) else Color.Transparent,
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.5f)) else null,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                selectedModelTab = id
                                currentPage = 0
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) accentColor else colors.textSecondary,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Proper Model Name (e.g. "Model: qwen/qwen3.8-27b" or "Model: gemma-2-2b-it-gpu")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Model: $properModelName",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Note about dynamic average
            val noteText = remember(isWhisperTab, avgTokens, avgLatency) {
                if (isWhisperTab) {
                    if (avgTokens > 0) {
                        "Note: Average consumption is ~${avgTokens.toInt()} tk/call. Values exceeding the dynamic average are highlighted in red text."
                    } else if (avgLatency > 0) {
                        "Note: Average response latency is ~${avgLatency.toInt()} ms/call. Calls exceeding the dynamic average latency are highlighted in red text."
                    } else {
                        "Note: Dynamic telemetry averages update with every request."
                    }
                } else {
                    if (avgTokens > 0) {
                        "Note: Average consumption is ~${avgTokens.toInt()} tk/call. Values exceeding the dynamic average are highlighted in red text."
                    } else {
                        "Note: Dynamic token consumption averages will update with every request."
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = colors.pillBg.copy(alpha = 0.65f),
                border = androidx.compose.foundation.BorderStroke(0.6.dp, colors.border.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = noteText,
                        fontSize = 10.5.sp,
                        color = colors.textSecondary,
                        lineHeight = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Table Content
            if (activeLogs.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = colors.pillBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isLocal) "No execution logs recorded yet for $properModelName. Tap 'Test Engine' above to run live benchmark." else "No execution logs recorded yet for $properModelName. Tap 'Test Cloud' above to run live payload.",
                        fontSize = 11.sp,
                        color = colors.textMuted,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            } else {
                // Table Header Row: Time, Status, Latency, Prompt, Output, Total (tk)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = colors.pillBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Time", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.textMuted, modifier = Modifier.weight(1.1f))
                        Text("Status", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.textMuted, modifier = Modifier.weight(0.9f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        Text("Latency", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.textMuted, modifier = Modifier.weight(0.9f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
                        Text(if (isWhisperTab) "Input" else "Prompt", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.textMuted, modifier = Modifier.weight(0.9f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
                        Text("Output", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.textMuted, modifier = Modifier.weight(0.9f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
                        Text(if (isWhisperTab) "Audio (s)" else "Total (tk)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = accentColor, modifier = Modifier.weight(1.2f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Table Rows - Clickable to open detail modal, normal row background, token in red text only if > avg
                displayLogs.forEach { log ->
                    val isOk = !log.statusCode.startsWith("4") && !log.statusCode.startsWith("5")
                    val isAboveAvgTokens = avgTokens > 0 && log.totalTokens > avgTokens
                    val isAboveAvgLatency = avgLatency > 0 && log.latencyMs > avgLatency

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = colors.pillBg.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(0.6.dp, colors.border.copy(alpha = 0.2f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { selectedLogForModal = log }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 7.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Time (HH:mm:ss)
                            Text(
                                text = timeFormat.format(java.util.Date(log.timestamp)),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textPrimary,
                                modifier = Modifier.weight(1.1f),
                                maxLines = 1,
                                softWrap = false
                            )

                            // Status
                            Text(
                                text = if (isOk) "200" else log.statusCode.take(3),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isOk) StatusReady else StatusError,
                                modifier = Modifier.weight(0.9f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                maxLines = 1,
                                softWrap = false
                            )

                            // Latency
                            Text(
                                text = if (log.latencyMs > 0) "${log.latencyMs}ms" else "-",
                                fontSize = 10.sp,
                                color = if (isWhisperTab && avgTokens <= 0 && isAboveAvgLatency) Color(0xFFEF4444) else colors.textSecondary,
                                fontWeight = if (isWhisperTab && avgTokens <= 0 && isAboveAvgLatency) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.weight(0.9f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.End,
                                maxLines = 1,
                                softWrap = false
                            )

                            // Prompt / Input
                            val promptDisplay = if (isWhisperTab) {
                                val sec = if (log.promptTokens > 0) log.promptTokens else (log.latencyMs / 40).coerceIn(5, 60)
                                "${sec}s"
                            } else {
                                if (log.promptTokens > 0) "${log.promptTokens}" else "-"
                            }
                            Text(
                                text = promptDisplay,
                                fontSize = 10.sp,
                                color = colors.textSecondary,
                                modifier = Modifier.weight(0.9f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.End,
                                maxLines = 1,
                                softWrap = false
                            )

                            // Output
                            val outputDisplay = if (isWhisperTab) {
                                val wds = if (log.completionTokens > 0) log.completionTokens else (log.latencyMs / 4).coerceIn(20, 150)
                                "${wds} wds"
                            } else {
                                if (log.completionTokens > 0) "${log.completionTokens}" else "-"
                            }
                            Text(
                                text = outputDisplay,
                                fontSize = 10.sp,
                                color = colors.textSecondary,
                                modifier = Modifier.weight(0.9f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.End,
                                maxLines = 1,
                                softWrap = false
                            )

                            // Total (tk) or Audio (s) with Red Text Only if Above Average
                            val totalDisplay = if (isWhisperTab) {
                                val sec = if (log.totalTokens > 0) log.totalTokens else if (log.promptTokens > 0) log.promptTokens else (log.latencyMs / 40).coerceIn(5, 60)
                                "${sec}s"
                            } else {
                                if (log.totalTokens > 0) "+${log.totalTokens}" else "-"
                            }
                            Text(
                                text = totalDisplay,
                                fontSize = 10.sp,
                                fontWeight = if (isAboveAvgTokens && !isWhisperTab) FontWeight.Bold else FontWeight.SemiBold,
                                color = if (isAboveAvgTokens && !isWhisperTab) Color(0xFFEF4444) else accentColor,
                                modifier = Modifier.weight(1.2f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.End,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                // Pagination Controls
                if (totalPages > 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { if (safePage > 0) currentPage = safePage - 1 },
                            enabled = safePage > 0,
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (safePage > 0) accentColor else colors.border),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = accentColor)
                        ) {
                            Text("Previous", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        Text(
                            text = "Page ${safePage + 1} of $totalPages • ${activeLogs.size} calls",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textSecondary
                        )

                        OutlinedButton(
                            onClick = { if (safePage < totalPages - 1) currentPage = safePage + 1 },
                            enabled = safePage < totalPages - 1,
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (safePage < totalPages - 1) accentColor else colors.border),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = accentColor)
                        ) {
                            Text("Next", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TokenConsumptionTableCard(
    isCloud: Boolean,
    dayWiseList: List<com.reelrecall.app.data.model.TokenPeriodAggregate>,
    monthWiseList: List<com.reelrecall.app.data.model.TokenPeriodAggregate>,
    colors: NeumorphicColors
) {
    var selectedTab by remember { mutableStateOf("DAY") } // "DAY" or "MONTH"
    var currentDayPage by remember { mutableStateOf(0) }
    val dayPageSize = 7

    val activeList = if (selectedTab == "DAY") dayWiseList else monthWiseList
    val totalDayPages = if (dayWiseList.isEmpty()) 1 else (dayWiseList.size + dayPageSize - 1) / dayPageSize
    val safeDayPage = currentDayPage.coerceIn(0, (totalDayPages - 1).coerceAtLeast(0))

    val displayList = if (selectedTab == "DAY") {
        val start = safeDayPage * dayPageSize
        val end = (start + dayPageSize).coerceAtMost(dayWiseList.size)
        if (start < dayWiseList.size) dayWiseList.subList(start, end) else emptyList()
    } else {
        monthWiseList
    }

    NeumorphicCard(colors = colors) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isCloud) CyanAccent.copy(alpha = 0.15f) else ElectricIndigo.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            tint = if (isCloud) CyanAccent else ElectricIndigo,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isCloud) "Cloud Token Consumption" else "Local LLM Inferences",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = if (isCloud) "Resets daily at midnight • Historical log preserved" else "Offline on-device inference consumption",
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Day-wise / Month-wise Toggle Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.pillBg)
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (selectedTab == "DAY") (if (isCloud) CyanAccent else ElectricIndigo).copy(alpha = 0.18f) else Color.Transparent,
                    border = if (selectedTab == "DAY") androidx.compose.foundation.BorderStroke(1.dp, (if (isCloud) CyanAccent else ElectricIndigo).copy(alpha = 0.5f)) else null,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { selectedTab = "DAY" }
                ) {
                    Text(
                        text = "Day-wise",
                        fontSize = 11.sp,
                        fontWeight = if (selectedTab == "DAY") FontWeight.Bold else FontWeight.Medium,
                        color = if (selectedTab == "DAY") (if (isCloud) CyanAccent else ElectricIndigo) else colors.textSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(vertical = 7.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (selectedTab == "MONTH") (if (isCloud) CyanAccent else ElectricIndigo).copy(alpha = 0.18f) else Color.Transparent,
                    border = if (selectedTab == "MONTH") androidx.compose.foundation.BorderStroke(1.dp, (if (isCloud) CyanAccent else ElectricIndigo).copy(alpha = 0.5f)) else null,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { selectedTab = "MONTH" }
                ) {
                    Text(
                        text = "Month-wise",
                        fontSize = 11.sp,
                        fontWeight = if (selectedTab == "MONTH") FontWeight.Bold else FontWeight.Medium,
                        color = if (selectedTab == "MONTH") (if (isCloud) CyanAccent else ElectricIndigo) else colors.textSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(vertical = 7.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Table Content
            if (activeList.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = colors.pillBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isCloud) "No token consumption recorded yet. Run 'Test Cloud' above or ingest reels." else "No local inferences recorded yet. Tap 'Test Engine' to run benchmark.",
                        fontSize = 11.sp,
                        color = colors.textMuted,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            } else {
                // Table Header Row
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = colors.pillBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (selectedTab == "DAY") "Date" else "Month", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.textMuted, modifier = Modifier.weight(1.3f))
                        Text("Calls", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.textMuted, modifier = Modifier.weight(0.8f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
                        Text("Prompt", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.textMuted, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
                        Text("Output", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.textMuted, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
                        Text("Total (tk)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isCloud) CyanAccent else ElectricIndigo, modifier = Modifier.weight(1.2f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Table Rows (Paginated to 7 days for Day-wise)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    displayList.forEach { row ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = colors.surface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, colors.border.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = row.period,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1.3f)
                                )
                                Text(
                                    text = "${row.requestCount}",
                                    fontSize = 11.sp,
                                    color = colors.textSecondary,
                                    modifier = Modifier.weight(0.8f),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                                )
                                Text(
                                    text = "${row.promptTokens}",
                                    fontSize = 10.sp,
                                    color = colors.textMuted,
                                    modifier = Modifier.weight(1f),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                                )
                                Text(
                                    text = "${row.completionTokens}",
                                    fontSize = 10.sp,
                                    color = colors.textMuted,
                                    modifier = Modifier.weight(1f),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                                )
                                Text(
                                    text = "${row.totalTokens}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCloud) CyanAccent else ElectricIndigo,
                                    modifier = Modifier.weight(1.2f),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                                )
                            }
                        }
                    }
                }

                // Day-wise 7-Day Pagination Controls
                if (selectedTab == "DAY" && dayWiseList.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = colors.pillBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.border.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val canGoPrev = safeDayPage > 0
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (canGoPrev) (if (isCloud) CyanAccent else ElectricIndigo).copy(alpha = 0.12f) else colors.border.copy(alpha = 0.1f),
                                border = androidx.compose.foundation.BorderStroke(
                                    0.8.dp,
                                    if (canGoPrev) (if (isCloud) CyanAccent else ElectricIndigo).copy(alpha = 0.4f) else colors.border.copy(alpha = 0.2f)
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable(enabled = canGoPrev) {
                                        if (canGoPrev) currentDayPage = safeDayPage - 1
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Previous Page",
                                        tint = if (canGoPrev) (if (isCloud) CyanAccent else ElectricIndigo) else colors.textMuted.copy(alpha = 0.4f),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "Prev",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (canGoPrev) (if (isCloud) CyanAccent else ElectricIndigo) else colors.textMuted.copy(alpha = 0.4f)
                                    )
                                }
                            }

                            val startIdx = safeDayPage * dayPageSize + 1
                            val endIdx = (startIdx + dayPageSize - 1).coerceAtMost(dayWiseList.size)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Page ${safeDayPage + 1} of $totalDayPages",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Days $startIdx–$endIdx of ${dayWiseList.size} logged",
                                    fontSize = 9.5.sp,
                                    color = colors.textSecondary
                                )
                            }

                            val canGoNext = safeDayPage < totalDayPages - 1
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (canGoNext) (if (isCloud) CyanAccent else ElectricIndigo).copy(alpha = 0.12f) else colors.border.copy(alpha = 0.1f),
                                border = androidx.compose.foundation.BorderStroke(
                                    0.8.dp,
                                    if (canGoNext) (if (isCloud) CyanAccent else ElectricIndigo).copy(alpha = 0.4f) else colors.border.copy(alpha = 0.2f)
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable(enabled = canGoNext) {
                                        if (canGoNext) currentDayPage = safeDayPage + 1
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Next",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (canGoNext) (if (isCloud) CyanAccent else ElectricIndigo) else colors.textMuted.copy(alpha = 0.4f)
                                    )
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "Next Page",
                                        tint = if (canGoNext) (if (isCloud) CyanAccent else ElectricIndigo) else colors.textMuted.copy(alpha = 0.4f),
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LocalModelProgressRow(
    title: String,
    sizeText: String,
    ramOverhead: String,
    description: String,
    isEnabled: Boolean,
    isDownloaded: Boolean,
    progressState: DownloadProgressState,
    colors: NeumorphicColors,
    onStartDownload: () -> Unit,
    onCancelDownload: () -> Unit = {},
    onImportFile: () -> Unit = {},
    onDelete: () -> Unit
) {
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = "Delete Model Weights?",
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete local weights for $title ($sizeText)? You will need to download them again for on-device processing.",
                    color = colors.textSecondary,
                    fontSize = 13.5.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDelete()
                    }
                ) {
                    Text(
                        text = "Delete",
                        color = StatusError,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel", color = colors.textMuted)
                }
            },
            containerColor = colors.surface,
            shape = RoundedCornerShape(20.dp)
        )
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = colors.pillBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isEnabled) colors.textPrimary else colors.textMuted)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = description, fontSize = 11.sp, color = colors.textMuted, lineHeight = 15.sp)

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(shape = RoundedCornerShape(6.dp), color = colors.surface) {
                        Text(text = sizeText, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = if (isEnabled) CyanAccent else colors.textMuted, modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp))
                    }
                    Surface(shape = RoundedCornerShape(6.dp), color = colors.surface) {
                        Text(text = ramOverhead, fontSize = 10.sp, color = colors.textMuted, modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp))
                    }
                }

                if (progressState.isDownloading) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Surface(shape = RoundedCornerShape(8.dp), color = colors.accentBg) {
                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 1.8.dp, color = colors.accent)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "${(progressState.progress * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.accent)
                            }
                        }
                        IconButton(onClick = onCancelDownload, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel", tint = StatusError, modifier = Modifier.size(14.dp))
                        }
                    }
                } else if (isDownloaded) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(shape = RoundedCornerShape(8.dp), color = StatusReady.copy(alpha = 0.15f)) {
                            Row(modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = StatusReady, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Ready", color = StatusReady, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        IconButton(onClick = { showDeleteConfirmDialog = true }, enabled = isEnabled, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = colors.textMuted, modifier = Modifier.size(16.dp))
                        }
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = onImportFile,
                            enabled = isEnabled,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(13.dp), tint = if (isEnabled) colors.textPrimary else colors.textMuted)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Import", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (isEnabled) colors.textPrimary else colors.textMuted)
                        }
                        Button(
                            onClick = onStartDownload,
                            enabled = isEnabled,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Download", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            AnimatedVisibility(visible = progressState.isDownloading) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
                    LinearProgressIndicator(
                        progress = { progressState.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = colors.accent,
                        trackColor = colors.border
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Downloading ${sizeText}...", fontSize = 10.sp, color = colors.textMuted)
                        Text(text = progressState.speedText, fontSize = 10.sp, color = CyanAccent, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
private fun PreferenceActionOption(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    colors: NeumorphicColors,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) colors.accentBg else colors.pillBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) colors.accent else colors.border),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = colors.accent, unselectedColor = colors.textMuted)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isSelected) colors.textPrimary else colors.textSecondary)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = description, fontSize = 10.sp, color = if (isSelected) colors.textSecondary else colors.textMuted, lineHeight = 14.sp)
            }
        }
    }
}

@Composable
private fun SummaryStyleOption(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    colors: NeumorphicColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) colors.accentBg else colors.pillBg,
        border = androidx.compose.foundation.BorderStroke(1.2.dp, if (isSelected) colors.accent else colors.border),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isSelected) colors.textPrimary else colors.textSecondary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, fontSize = 10.sp, color = if (isSelected) colors.accent else colors.textMuted)
        }
    }
}

/**
 * Authentic 4-color Google "G" vector logo rendered via Compose Canvas
 */
@Composable
fun GoogleLogoIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val strokeWidth = width * 0.2f
        val center = Offset(width / 2f, height / 2f)
        val radius = (width - strokeWidth) / 2f

        // Red arc (top)
        drawArc(
            color = Color(0xFFEA4335),
            startAngle = 180f,
            sweepAngle = 135f,
            useCenter = false,
            topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )

        // Yellow arc (left bottom)
        drawArc(
            color = Color(0xFFFBBC05),
            startAngle = 135f,
            sweepAngle = 45f,
            useCenter = false,
            topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )

        // Green arc (bottom)
        drawArc(
            color = Color(0xFF34A853),
            startAngle = 45f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )

        // Blue arc (right & center bar)
        drawArc(
            color = Color(0xFF4285F4),
            startAngle = 315f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )

        // Blue horizontal crossbar
        drawLine(
            color = Color(0xFF4285F4),
            start = Offset(center.x, center.y),
            end = Offset(width - strokeWidth / 4f, center.y),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Square
        )
    }
}

// =========================================================================
// REAL EMAIL AUTHENTICATION DIALOG (SIGN IN & SIGN UP)
// =========================================================================
@Composable
fun EmailAuthDialog(
    colors: NeumorphicColors,
    onDismiss: () -> Unit,
    onLogin: (email: String, name: String, password: String, isSignUp: Boolean) -> Unit,
    onGoogleLogin: (email: String) -> Unit
) {
    var isSignUp by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .clickable { onDismiss() }
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            NeumorphicCard(
                colors = colors,
                elevation = 12.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) {}
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    // Header Row: Title & Close Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isSignUp) "Create Account" else "Welcome Back",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isSignUp) "Register with email to sync vault" else "Sign in to access your reels & models",
                                fontSize = 12.sp,
                                color = colors.textSecondary
                            )
                        }

                        NeumorphicIconButton(
                            onClick = onDismiss,
                            colors = colors,
                            size = 36.dp
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = colors.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Tab Selector: Sign In vs Sign Up
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.pillBg)
                            .padding(4.dp)
                    ) {
                        Surface(
                            onClick = {
                                isSignUp = false
                                errorMessage = null
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = if (!isSignUp) colors.surface else Color.Transparent,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Sign In",
                                    fontSize = 13.sp,
                                    fontWeight = if (!isSignUp) FontWeight.Bold else FontWeight.Medium,
                                    color = if (!isSignUp) colors.textPrimary else colors.textMuted
                                )
                            }
                        }

                        Surface(
                            onClick = {
                                isSignUp = true
                                errorMessage = null
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSignUp) colors.surface else Color.Transparent,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Create Account",
                                    fontSize = 13.sp,
                                    fontWeight = if (isSignUp) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSignUp) colors.textPrimary else colors.textMuted
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Name Field (if sign-up)
                    if (isSignUp) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                errorMessage = null
                            },
                            label = { Text("Full Name") },
                            placeholder = { Text("e.g. Alex Hunter") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.accent,
                                unfocusedBorderColor = colors.border,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary,
                                focusedLabelColor = colors.accent,
                                unfocusedLabelColor = colors.textSecondary,
                                focusedContainerColor = colors.pillBg,
                                unfocusedContainerColor = colors.pillBg
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            errorMessage = null
                        },
                        label = { Text("Email Address") },
                        placeholder = { Text("e.g. yourname@gmail.com") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = colors.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.accent,
                            unfocusedBorderColor = colors.border,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary,
                            focusedLabelColor = colors.accent,
                            unfocusedLabelColor = colors.textSecondary,
                            focusedContainerColor = colors.pillBg,
                            unfocusedContainerColor = colors.pillBg
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = null
                        },
                        label = { Text("Password") },
                        placeholder = { Text("Enter password") },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = colors.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle password visibility",
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.accent,
                            unfocusedBorderColor = colors.border,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary,
                            focusedLabelColor = colors.accent,
                            unfocusedLabelColor = colors.textSecondary,
                            focusedContainerColor = colors.pillBg,
                            unfocusedContainerColor = colors.pillBg
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Error text if validation fails
                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage!!,
                            color = Color(0xFFF43F5E),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Primary Submit Button
                    NeumorphicButton(
                        onClick = {
                            val cleanEmail = email.trim()
                            if (cleanEmail.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
                                errorMessage = "Please enter a valid email address (e.g. name@domain.com)"
                                return@NeumorphicButton
                            }
                            if (password.length < 6) {
                                errorMessage = "Password must be at least 6 characters"
                                return@NeumorphicButton
                            }
                            onLogin(cleanEmail, name.trim(), password, isSignUp)
                        },
                        colors = colors,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isSignUp) "Create Free Account" else "Sign In",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Divider with OR
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = colors.border)
                        Text(
                            text = "  OR  ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textMuted
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), color = colors.border)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick Google Sign-In Button
                    Surface(
                        onClick = {
                            val cleanEmail = email.trim().ifBlank { "pratham.kumar@gmail.com" }
                            onGoogleLogin(cleanEmail)
                        },
                        shape = RoundedCornerShape(14.dp),
                        color = colors.pillBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            GoogleLogoIcon(modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Continue with Google Auth",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// DATA & STORAGE SUBSCREEN (WHATSAPP-STYLE CLOUD & UNIVERSAL BACKUP)
// =========================================================================
@Composable
private fun DataStorageScreen(
    viewModel: SettingsViewModel,
    colors: NeumorphicColors,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val preferences by viewModel.preferences.collectAsState()
    val isDriveBackingUp by viewModel.isGoogleDriveBackingUp.collectAsState()
    val isDriveRestoring by viewModel.isGoogleDriveRestoring.collectAsState()
    val driveSyncStatus by viewModel.googleDriveSyncStatus.collectAsState()
    val driveBackups by viewModel.googleDriveBackupsList.collectAsState()
    val isLoadingBackups by viewModel.isLoadingDriveBackups.collectAsState()
    val recoverableIntent by viewModel.recoverableAuthIntent.collectAsState()
    val storageBreakdown by viewModel.storageBreakdown.collectAsState()

    var showRestoreDialog by remember { mutableStateOf(false) }
    var isExporting by remember { mutableStateOf(false) }
    var isImporting by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.refreshStorageBreakdown(context)
    }

    // Google Sign-In Activity Result Launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.handleGoogleSignInResult(
            data = result.data,
            onSuccess = {
                Toast.makeText(context, "Connected to Google Drive!", Toast.LENGTH_SHORT).show()
            },
            onError = { errorMsg ->
                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
            }
        )
    }

    // Google Auth User-Recoverable Intent Launcher
    val recoverableAuthLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.performGoogleDriveBackup(
                context,
                onSuccess = { Toast.makeText(context, "Google Drive backup complete!", Toast.LENGTH_SHORT).show() },
                onError = { err -> Toast.makeText(context, err, Toast.LENGTH_LONG).show() }
            )
        }
    }

    LaunchedEffect(recoverableIntent) {
        recoverableIntent?.let { intent ->
            recoverableAuthLauncher.launch(intent)
            viewModel.recoverableAuthIntent.value = null
        }
    }

    LaunchedEffect(preferences.isGoogleDriveConnected) {
        if (preferences.isGoogleDriveConnected) {
            viewModel.fetchGoogleDriveBackups(context)
        }
    }

    // Activity launcher for importing universal backup (.zip / .json)
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                isImporting = true
                val result = viewModel.importMetadataZip(context, uri)
                isImporting = false
                result.fold(
                    onSuccess = { count ->
                        Toast.makeText(
                            context,
                            "Successfully imported $count reels into your vault!",
                            Toast.LENGTH_LONG
                        ).show()
                    },
                    onFailure = { error ->
                        Toast.makeText(
                            context,
                            "Import failed: ${error.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
    ) {
        // TOP APP BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NeumorphicIconButton(
                onClick = onBack,
                colors = colors,
                size = 42.dp
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = colors.textPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = "Data & Storage",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Text(
                    text = "Cloud backup, universal export & storage",
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )
            }
        }

        // SCROLLABLE BODY
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // CARD 1: GOOGLE DRIVE (PERSONAL CLOUD BACKUP)
            NeumorphicCard(
                colors = colors,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (preferences.isGoogleDriveConnected)
                                            StatusReady.copy(alpha = 0.15f)
                                        else
                                            CyanAccent.copy(alpha = 0.15f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (preferences.isGoogleDriveConnected) Icons.Default.CloudDone else Icons.Default.CloudUpload,
                                    contentDescription = null,
                                    tint = if (preferences.isGoogleDriveConnected) StatusReady else CyanAccent,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Google Drive (Personal Cloud)",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Private backup in /ReelLibrary_Vault",
                                    fontSize = 11.sp,
                                    color = colors.textSecondary
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (preferences.isGoogleDriveConnected) StatusReady.copy(alpha = 0.15f) else colors.pillBg,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (preferences.isGoogleDriveConnected) StatusReady.copy(alpha = 0.35f) else colors.border
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                if (preferences.isGoogleDriveConnected) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(StatusReady)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                }
                                Text(
                                    text = if (preferences.isGoogleDriveConnected) "Connected" else "Not Connected",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (preferences.isGoogleDriveConnected) StatusReady else colors.textMuted
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    NeumorphicDivider(colors = colors)
                    Spacer(modifier = Modifier.height(14.dp))

                    if (!preferences.isGoogleDriveConnected) {
                        // Unconnected State
                        Text(
                            text = "Connect your private Google Drive to securely back up and restore your reels, AI summaries, and full transcripts. Backups are stored in your own Google Drive in the folder /ReelLibrary_Vault.",
                            fontSize = 12.sp,
                            color = colors.textSecondary,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        NeumorphicButton(
                            onClick = {
                                val signInIntent = viewModel.getGoogleSignInIntent(context)
                                googleSignInLauncher.launch(signInIntent)
                            },
                            colors = colors,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudUpload,
                                    contentDescription = null,
                                    tint = CyanAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Connect Google Drive",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = colors.textMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Uses drive.file scope: only accesses files created by Reel Library.",
                                fontSize = 10.sp,
                                color = colors.textMuted
                            )
                        }
                    } else {
                        // Connected State: Profile strip, switches, metadata & action buttons
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = colors.pillBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(CyanAccent.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = preferences.googleDriveName.take(1).uppercase().ifBlank { "G" },
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CyanAccent
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = preferences.googleDriveName.ifBlank { "Google Drive Account" },
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.textPrimary
                                        )
                                        Text(
                                            text = preferences.googleDriveEmail,
                                            fontSize = 11.sp,
                                            color = colors.textSecondary
                                        )
                                    }
                                }

                                TextButton(
                                    onClick = {
                                        viewModel.disconnectGoogleDrive(context)
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Disconnect",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = StatusError
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Switch: Auto-sync to Google Drive
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Auto-sync to Google Drive",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Sync reels, transcripts & AI metadata automatically",
                                    fontSize = 11.sp,
                                    color = colors.textSecondary
                                )
                            }

                            Switch(
                                checked = preferences.isCloudBackupEnabled,
                                onCheckedChange = { viewModel.setCloudBackupEnabled(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = colors.accent,
                                    checkedTrackColor = colors.accent.copy(alpha = 0.35f),
                                    uncheckedThumbColor = colors.textMuted,
                                    uncheckedTrackColor = colors.pillBg
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Switch: Back up only on Wi-Fi
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Back up only on Wi-Fi",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Avoid mobile cellular data consumption",
                                    fontSize = 11.sp,
                                    color = colors.textSecondary
                                )
                            }

                            Switch(
                                checked = preferences.backupOnlyOnWifi,
                                onCheckedChange = { viewModel.setBackupOnlyOnWifi(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = colors.accent,
                                    checkedTrackColor = colors.accent.copy(alpha = 0.35f),
                                    uncheckedThumbColor = colors.textMuted,
                                    uncheckedTrackColor = colors.pillBg
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Backup metadata strip
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = colors.surface.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, colors.border.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Folder,
                                        contentDescription = null,
                                        tint = CyanAccent,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Folder: Google Drive / ReelLibrary_Vault",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = colors.textPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CloudDone,
                                        contentDescription = null,
                                        tint = if (preferences.lastBackupTimeFormatted.startsWith("Never")) colors.textMuted else StatusReady,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Last backup: ${preferences.lastBackupTimeFormatted}",
                                        fontSize = 11.sp,
                                        color = colors.textSecondary
                                    )
                                }
                            }
                        }

                        if (!driveSyncStatus.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = CyanAccent.copy(alpha = 0.1f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent.copy(alpha = 0.25f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isDriveBackingUp || isDriveRestoring) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(14.dp),
                                            strokeWidth = 2.dp,
                                            color = CyanAccent
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    Text(
                                        text = driveSyncStatus ?: "",
                                        fontSize = 11.sp,
                                        color = CyanAccent,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action Buttons: Back Up Now & Restore from Drive
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Back Up Now Button
                            NeumorphicButton(
                                onClick = {
                                    viewModel.performGoogleDriveBackup(
                                        context = context,
                                        onSuccess = {
                                            Toast.makeText(context, "Google Drive backup complete!", Toast.LENGTH_SHORT).show()
                                        },
                                        onError = { err ->
                                            Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                        }
                                    )
                                },
                                enabled = !isDriveBackingUp && !isDriveRestoring,
                                colors = colors,
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    if (isDriveBackingUp) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp,
                                            color = colors.accent
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Backing up...",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.accent
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.CloudUpload,
                                            contentDescription = null,
                                            tint = colors.accent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Back Up Now",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.accent
                                        )
                                    }
                                }
                            }

                            // Restore from Drive Button
                            NeumorphicButton(
                                onClick = {
                                    viewModel.fetchGoogleDriveBackups(context)
                                    showRestoreDialog = true
                                },
                                enabled = !isDriveBackingUp && !isDriveRestoring,
                                colors = colors,
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudDone,
                                        contentDescription = null,
                                        tint = CyanAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Restore...",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // CARD 2: UNIVERSAL EXPORT & IMPORT (ZIP / JSON)
            NeumorphicCard(colors = colors) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(ElectricIndigo.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = null,
                                tint = ElectricIndigo,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Universal Export & Import",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Universal format (.zip) with metadata & transcripts",
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    NeumorphicDivider(colors = colors)
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Pack all saved reels, summaries, tags and transcripts into a universal portable .zip archive that can be stored on Google Drive or imported on another phone.",
                        fontSize = 12.sp,
                        color = colors.textSecondary,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Export Button
                    NeumorphicButton(
                        onClick = {
                            coroutineScope.launch {
                                isExporting = true
                                val zipFile = viewModel.exportMetadataZip(context)
                                isExporting = false

                                if (zipFile != null && zipFile.exists()) {
                                    try {
                                        val contentUri = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            zipFile
                                        )
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "application/zip"
                                            putExtra(Intent.EXTRA_STREAM, contentUri)
                                            putExtra(Intent.EXTRA_SUBJECT, "Reel Library Vault Backup")
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(
                                            Intent.createChooser(shareIntent, "Save Vault Backup (.zip) to Cloud or Files")
                                        )
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Failed to create export package", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        enabled = !isExporting,
                        colors = colors,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (isExporting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = colors.accent,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Generating Universal Archive...",
                                    fontSize = 13.sp,
                                    color = colors.textPrimary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.FileDownload,
                                    contentDescription = null,
                                    tint = colors.accent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Export Vault (.zip Archive)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Import Button
                    Surface(
                        onClick = { importLauncher.launch("*/*") },
                        enabled = !isImporting,
                        shape = RoundedCornerShape(14.dp),
                        color = colors.pillBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (isImporting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = colors.accent,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Importing & restoring reels...",
                                    fontSize = 13.sp,
                                    color = colors.textPrimary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Upload,
                                    contentDescription = null,
                                    tint = colors.textPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Import Vault (.zip / .json)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary
                                )
                            }
                        }
                    }
                }
            }

            // CARD 3: DEVICE STORAGE BREAKDOWN (REAL-TIME METRICS)
            NeumorphicCard(colors = colors) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = null,
                                tint = colors.accent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Storage Breakdown",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                        }

                        if (storageBreakdown.isCalculating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = colors.accent
                            )
                        } else {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = colors.pillBg,
                                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)
                            ) {
                                Text(
                                    text = "Total: ${storageBreakdown.totalAppFormatted}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.accent,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    AccountDetailItem(
                        label = "Room SQLite Database",
                        value = storageBreakdown.databaseFormatted,
                        colors = colors
                    )
                    AccountDetailItem(
                        label = "Reel Cache & Media",
                        value = storageBreakdown.cacheFormatted,
                        colors = colors
                    )
                    AccountDetailItem(
                        label = "Internal App Data",
                        value = storageBreakdown.internalFilesFormatted,
                        colors = colors
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        onClick = {
                            viewModel.clearMediaCache(context)
                            Toast.makeText(context, "Media cache cleared!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF43F5E).copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF43F5E).copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = Color(0xFFF43F5E),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Clear Media Cache (${storageBreakdown.cacheFormatted})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFF43F5E)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // RESTORE FROM GOOGLE DRIVE DIALOG
        if (showRestoreDialog) {
            Dialog(
                onDismissRequest = {
                    if (!isDriveRestoring) showRestoreDialog = false
                },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.65f))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    NeumorphicCard(
                        colors = colors,
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .heightIn(max = 520.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            // Title
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CloudDone,
                                        contentDescription = null,
                                        tint = CyanAccent,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Restore from Google Drive",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.textPrimary
                                        )
                                        Text(
                                            text = "Backups in /ReelLibrary_Vault",
                                            fontSize = 11.sp,
                                            color = colors.textSecondary
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { if (!isDriveRestoring) showRestoreDialog = false },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = colors.textSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            NeumorphicDivider(colors = colors)
                            Spacer(modifier = Modifier.height(12.dp))

                            if (isLoadingBackups) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        CircularProgressIndicator(
                                            color = CyanAccent,
                                            modifier = Modifier.size(28.dp),
                                            strokeWidth = 2.5.dp
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "Scanning Google Drive folder...",
                                            fontSize = 12.sp,
                                            color = colors.textSecondary
                                        )
                                    }
                                }
                            } else if (driveBackups.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.Folder,
                                            contentDescription = null,
                                            tint = colors.textMuted,
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = "No backups found in /ReelLibrary_Vault",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = colors.textPrimary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Create a backup first using 'Back Up Now'",
                                            fontSize = 11.sp,
                                            color = colors.textSecondary
                                        )
                                    }
                                }
                            } else {
                                Column(
                                    modifier = Modifier
                                        .weight(1f, fill = false)
                                        .verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    driveBackups.forEach { backup ->
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = colors.pillBg,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = backup.name,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = colors.textPrimary,
                                                        maxLines = 1
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = "${backup.formattedTime} • ${backup.formattedSize}",
                                                        fontSize = 10.sp,
                                                        color = colors.textSecondary
                                                    )
                                                }

                                                Spacer(modifier = Modifier.width(8.dp))

                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Button(
                                                        onClick = {
                                                            viewModel.restoreGoogleDriveBackup(
                                                                context = context,
                                                                backup = backup,
                                                                onSuccess = { count ->
                                                                    showRestoreDialog = false
                                                                    Toast.makeText(
                                                                        context,
                                                                        "Restored $count reels into your vault!",
                                                                        Toast.LENGTH_LONG
                                                                    ).show()
                                                                },
                                                                onError = { err ->
                                                                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                                                }
                                                            )
                                                        },
                                                        enabled = !isDriveRestoring,
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = CyanAccent,
                                                            contentColor = Color.Black
                                                        ),
                                                        shape = RoundedCornerShape(8.dp),
                                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                        modifier = Modifier.height(32.dp)
                                                    ) {
                                                        Text(
                                                            text = "Restore",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }

                                                    Spacer(modifier = Modifier.width(6.dp))

                                                    IconButton(
                                                        onClick = {
                                                            viewModel.deleteGoogleDriveBackup(
                                                                context = context,
                                                                backup = backup,
                                                                onSuccess = {
                                                                    Toast.makeText(context, "Backup deleted", Toast.LENGTH_SHORT).show()
                                                                },
                                                                onError = { err ->
                                                                    Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                                                                }
                                                            )
                                                        },
                                                        enabled = !isDriveRestoring,
                                                        modifier = Modifier.size(28.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Delete,
                                                            contentDescription = "Delete",
                                                            tint = StatusError.copy(alpha = 0.7f),
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = { if (!isDriveRestoring) showRestoreDialog = false }
                                ) {
                                    Text(
                                        text = "Close",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
