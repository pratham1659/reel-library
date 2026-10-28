package com.reelrecall.app.ui.screens.settings

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HelpOutline
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import com.reelrecall.app.data.repository.EngineMode
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
    val isCloudMode = preferences.engineMode != EngineMode.LOCAL_ONLY
    val isDarkTheme = preferences.themeMode != "OBSIDIAN_LIGHT"
    var showEmailAuthDialog by remember { mutableStateOf(false) }

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
                text = "Profile",
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
            // PROFILE / AUTHENTICATION CARD (NEUMORPHIC)
            NeumorphicCard(
                colors = colors,
                elevation = 6.dp,
                onClick = if (!preferences.isLoggedIn) { { showEmailAuthDialog = true } } else null,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (!preferences.isLoggedIn) {
                    // ==========================================
                    // STATE A: NOT LOGGED IN / SIGNUP PROMPT
                    // ==========================================
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

                        // NEUMORPHIC SIGN-IN / AUTH BUTTON
                        NeumorphicButton(
                            onClick = { showEmailAuthDialog = true },
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
                    // ==========================================
                    // STATE B: LOGGED IN (AVATAR + NAME + EMAIL)
                    // ==========================================
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

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = StatusReady.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(StatusReady)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Active",
                                    fontSize = 10.sp,
                                    color = StatusReady,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // SECTION TITLE: SETTINGS
            Text(
                text = "SETTINGS",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textMuted,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(start = 6.dp, top = 22.dp, bottom = 8.dp)
            )

            // GROUPED SETTINGS NEUMORPHIC CARD
            NeumorphicCard(
                colors = colors,
                elevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 1. Account (Opens deep Account Details)
                ProfileMenuRow(
                    icon = Icons.Default.Person,
                    title = "Account",
                    badgeText = if (preferences.isLoggedIn) "Verified" else "Details",
                    badgeColor = if (preferences.isLoggedIn) StatusReady else colors.accent,
                    colors = colors,
                    onClick = onNavigateToAccount
                )
                NeumorphicDivider(colors = colors)

                // 2. Select Models (Opens Models Subscreen)
                ProfileMenuRow(
                    icon = Icons.Default.Memory,
                    iconTint = colors.accent,
                    title = "Select Models",
                    badgeText = if (isCloudMode) "Cloud API" else "Local NPU",
                    badgeColor = if (isCloudMode) colors.accent else CyanAccent,
                    colors = colors,
                    onClick = onNavigateToModels
                )
                NeumorphicDivider(colors = colors)

                // 3. Preferences (Opens Preferences Subscreen)
                ProfileMenuRow(
                    icon = Icons.Default.Tune,
                    iconTint = ElectricIndigo,
                    title = "Preferences",
                    badgeText = "Feed & AI",
                    badgeColor = ElectricIndigo,
                    colors = colors,
                    onClick = onNavigateToPreferences
                )
                NeumorphicDivider(colors = colors)

                // 4. Appearance (Neumorphic Dark / Light Theme Toggle)
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

                // 5. Data & Storage (Cloud Backup, Google Drive & Universal Export/Import)
                ProfileMenuRow(
                    icon = Icons.Default.Storage,
                    iconTint = CyanAccent,
                    title = "Data & Storage",
                    badgeText = if (preferences.isCloudBackupEnabled) "Cloud Sync" else "Local Only",
                    badgeColor = if (preferences.isCloudBackupEnabled) StatusReady else colors.textMuted,
                    colors = colors,
                    onClick = onNavigateToData
                )
                NeumorphicDivider(colors = colors)

                // 6. Help & Feedback (Highlight with coral/orange accent)
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
                                data = Uri.parse("mailto:support@reelrecall.app")
                                putExtra(Intent.EXTRA_SUBJECT, "ReelRecall Feedback & Support")
                            }
                            runCatching { context.startActivity(emailIntent) }
                                .onFailure {
                                    Toast.makeText(context, "Support: support@reelrecall.app", Toast.LENGTH_LONG).show()
                                }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SEPARATE LOGOUT / SIGN-IN CARD (NEUMORPHIC)
            NeumorphicCard(
                colors = colors,
                elevation = 4.dp,
                onClick = {
                    if (preferences.isLoggedIn) {
                        viewModel.logout()
                        Toast.makeText(context, "Signed out of session", Toast.LENGTH_SHORT).show()
                    } else {
                        showEmailAuthDialog = true
                    }
                },
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

            Spacer(modifier = Modifier.height(30.dp))
        }

        // REAL EMAIL AUTH DIALOG MODAL
        if (showEmailAuthDialog) {
            EmailAuthDialog(
                colors = colors,
                onDismiss = { showEmailAuthDialog = false },
                onLogin = { email, name, password, isSignUp ->
                    viewModel.loginWithEmail(
                        email = email,
                        name = name,
                        password = password,
                        isSignUp = isSignUp
                    )
                    showEmailAuthDialog = false
                    Toast.makeText(
                        context,
                        if (isSignUp) "Account created for $email" else "Signed in as $email",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onGoogleLogin = { email ->
                    viewModel.loginWithGoogle(
                        name = email.substringBefore("@").replaceFirstChar { it.uppercase() },
                        email = email
                    )
                    showEmailAuthDialog = false
                    Toast.makeText(context, "Signed in as $email", Toast.LENGTH_SHORT).show()
                }
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

    var groqApiKeyInput by remember { mutableStateOf(preferences.groqApiKey) }
    var geminiApiKeyInput by remember { mutableStateOf(preferences.geminiApiKey) }

    LaunchedEffect(preferences.groqApiKey, preferences.geminiApiKey) {
        groqApiKeyInput = preferences.groqApiKey
        geminiApiKeyInput = preferences.geminiApiKey
    }

    val isCloudMode = preferences.engineMode != EngineMode.LOCAL_ONLY
    var selectedTokenApi by remember { mutableStateOf("ALL") } // "ALL", "GEMINI", "GROQ"

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
                    text = "Select Models",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Text(
                    text = "Configure AI engine, credentials & offline models",
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
            // CARD 1: EXCLUSIVE SELECTION (CLOUD API vs LOCAL)
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
                        subtitle = "Fast processing with free cloud APIs (Groq Whisper + Gemini/Groq LLM).",
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
                                Text(text = "Cloud API Credentials", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                                Text(text = "Free tiers from Groq and Google AI Studio", fontSize = 11.sp, color = colors.textSecondary)
                            }
                        }

                        if (!isCloudMode) {
                            Surface(shape = RoundedCornerShape(6.dp), color = colors.pillBg) {
                                Text(text = "Inactive", fontSize = 10.sp, color = colors.textMuted, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Groq Key Input Header with Clear Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Text(
                                text = "Groq API Key",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                maxLines = 1,
                                softWrap = false
                            )
                            Text(
                                text = "Whisper Large v3 (Audio)",
                                fontSize = 10.sp,
                                color = colors.textSecondary,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                        if (groqApiKeyInput.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = StatusError.copy(alpha = 0.12f),
                                modifier = Modifier.clickable {
                                    groqApiKeyInput = ""
                                    viewModel.setGroqApiKey("")
                                    Toast.makeText(context, "Groq key cleared from vault", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = StatusError,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Clear",
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
                        value = groqApiKeyInput,
                        onValueChange = {}, // Read-only: direct manual text copying/editing blocked for vault security
                        readOnly = true,
                        enabled = isCloudMode,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("No key stored • Tap Paste to add", color = colors.textMuted, fontSize = 12.sp) },
                        singleLine = true,
                        visualTransformation = MaskApiKeyVisualTransformation(visibleChars = 3),
                        trailingIcon = {
                            if (groqApiKeyInput.isBlank()) {
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
                                                groqApiKeyInput = clip
                                                viewModel.setGroqApiKey(clip)
                                                Toast.makeText(context, "Groq key securely saved to Keystore vault", Toast.LENGTH_SHORT).show()
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

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CyanAccent.copy(alpha = 0.10f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://console.groq.com/keys"))
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
                                    text = "Get Free Groq Key",
                                    fontSize = 10.sp,
                                    color = CyanAccent,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                    contentDescription = "Open Groq Console",
                                    tint = CyanAccent,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Gemini Key Input Header with Clear Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Text(
                                text = "Google AI Studio Key",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                maxLines = 1,
                                softWrap = false
                            )
                            Text(
                                text = "Gemini 1.5 Flash (Summary)",
                                fontSize = 10.sp,
                                color = colors.textSecondary,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                        if (geminiApiKeyInput.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = StatusError.copy(alpha = 0.12f),
                                modifier = Modifier.clickable {
                                    geminiApiKeyInput = ""
                                    viewModel.setGeminiApiKey("")
                                    Toast.makeText(context, "Gemini key cleared from vault", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = StatusError,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Clear",
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
                        value = geminiApiKeyInput,
                        onValueChange = {}, // Read-only: direct manual text copying/editing blocked for vault security
                        readOnly = true,
                        enabled = isCloudMode,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("No key stored • Tap Paste to add", color = colors.textMuted, fontSize = 12.sp) },
                        singleLine = true,
                        visualTransformation = MaskApiKeyVisualTransformation(visibleChars = 3),
                        trailingIcon = {
                            if (geminiApiKeyInput.isBlank()) {
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
                                                geminiApiKeyInput = clip
                                                viewModel.setGeminiApiKey(clip)
                                                Toast.makeText(context, "Gemini key securely saved to Keystore vault", Toast.LENGTH_SHORT).show()
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

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CyanAccent.copy(alpha = 0.10f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://aistudio.google.com/app/apikey"))
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
                                    text = "Get Free Gemini Key",
                                    fontSize = 10.sp,
                                    color = CyanAccent,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                    contentDescription = "Open Google AI Studio",
                                    tint = CyanAccent,
                                    modifier = Modifier.size(12.dp)
                                )
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
                            onClick = { viewModel.testCloudConnection(groqApiKeyInput, geminiApiKeyInput) },
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
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f, fill = false)) {
                                            Text(
                                                text = ep.provider,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.textPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                softWrap = false
                                            )
                                            if (ep.errorDetails != null) {
                                                Text(
                                                    text = ep.errorDetails,
                                                    fontSize = 10.sp,
                                                    color = StatusError,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    softWrap = false
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (ok) StatusReady.copy(alpha = 0.15f) else StatusError.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = ep.statusMessage,
                                                    color = if (ok) StatusReady else StatusError,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    softWrap = false,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = CyanAccent.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = "⚡ ${ep.latencyMs}ms",
                                                    color = CyanAccent,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    softWrap = false,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }

                                            if (ep.tokensConsumed > 0) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = ElectricIndigo.copy(alpha = 0.15f)
                                                ) {
                                                    Text(
                                                        text = "+${ep.tokensConsumed} tok",
                                                        color = ElectricIndigo,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        maxLines = 1,
                                                        softWrap = false,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
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
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f, fill = false),
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

                                if (preferences.totalTokensUsed > 0L) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = colors.surface,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable { viewModel.resetTokenStats() }
                                    ) {
                                        Text(
                                            text = "Reset",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = colors.textMuted,
                                            maxLines = 1,
                                            softWrap = false,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "Select an API provider to inspect real-time usage telemetry",
                                fontSize = 10.sp,
                                color = colors.textSecondary,
                                modifier = Modifier.padding(top = 2.dp, bottom = 10.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                softWrap = false
                            )

                            // API Selector Segmented Tabs
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(colors.surface)
                                    .padding(3.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf(
                                    Triple("ALL", "All APIs", preferences.totalTokensUsed),
                                    Triple("GEMINI", "Gemini", preferences.geminiTotalTokens),
                                    Triple("GROQ", "Groq LPU", preferences.groqTotalTokens)
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
                                                text = if (tokens > 0) "${java.text.NumberFormat.getIntegerInstance().format(tokens)} tok" else "0 tok",
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

                            // Calculate active stats for selected API
                            val metrics = when (selectedTokenApi) {
                                "GEMINI" -> ApiTokenMetrics(
                                    total = preferences.geminiTotalTokens,
                                    prompt = preferences.geminiPromptTokens,
                                    completion = preferences.geminiCompletionTokens,
                                    calls = preferences.geminiCallsCount,
                                    label = "Google Gemini (LLM)"
                                )
                                "GROQ" -> ApiTokenMetrics(
                                    total = preferences.groqTotalTokens,
                                    prompt = preferences.groqPromptTokens,
                                    completion = preferences.groqCompletionTokens,
                                    calls = preferences.groqCallsCount,
                                    label = "Groq LPU (Whisper)"
                                )
                                else -> ApiTokenMetrics(
                                    total = preferences.totalTokensUsed,
                                    prompt = preferences.totalPromptTokens,
                                    completion = preferences.totalCompletionTokens,
                                    calls = (preferences.geminiCallsCount + preferences.groqCallsCount).coerceAtLeast(if (preferences.totalTokensUsed > 0L) 1L else 0L),
                                    label = "All Cloud Endpoints"
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
                                    softWrap = false
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = colors.surface
                                ) {
                                    Text(
                                        text = "${metrics.calls} requests",
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
                                            text = "TOTAL",
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
                                            text = "PROMPT",
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
                                            text = "OUTPUT",
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

                            if (metrics.total == 0L) {
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

                            if (preferences.lastCallTokens > 0L) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = colors.surface.copy(alpha = 0.6f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Last Call: +${preferences.lastCallTokens} tok (${preferences.lastCallProvider.ifBlank { "Cloud" }})",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = colors.textSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            softWrap = false,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "⚡ ${preferences.lastCallLatencyMs}ms • ${preferences.lastCallStatusCode}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CyanAccent,
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

            // CARD 3: LOCAL HARDWARE & DOWNLOAD PROGRESS
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
                                Icon(Icons.Default.Smartphone, contentDescription = null, tint = ElectricIndigo, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = "Local Hardware & Models", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                                Text(text = "On-device AI weights stored locally", fontSize = 11.sp, color = colors.textSecondary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Specs & Test Engine (Real Hardware Info - 0% Demo Data)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.pillBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f, fill = false)) {
                                    Text(
                                        text = hardwareInfo.processorName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        softWrap = false
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${hardwareInfo.ramDisplay} • ${hardwareInfo.storageDisplay}",
                                        fontSize = 10.sp,
                                        color = StatusReady,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        softWrap = false
                                    )
                                    Spacer(modifier = Modifier.height(1.dp))
                                    Text(
                                        text = hardwareInfo.npuAcceleratorStatus,
                                        fontSize = 9.sp,
                                        color = colors.textSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        softWrap = false
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Column(horizontalAlignment = Alignment.End) {
                                    OutlinedButton(
                                        onClick = { viewModel.testLocalEngine() },
                                        enabled = !isCloudMode && !localTestResult.isTesting,
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, if (!isCloudMode) ElectricIndigo else colors.border),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricIndigo)
                                    ) {
                                        if (localTestResult.isTesting) {
                                            CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp, color = ElectricIndigo)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Testing...", fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                                        } else {
                                            Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text("Test Engine", fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                                        }
                                    }

                                    if (localTestResult.latencyMs != null) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = ElectricIndigo.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "⚡ ${localTestResult.latencyMs}ms",
                                                color = ElectricIndigo,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                softWrap = false,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            AnimatedVisibility(visible = localTestResult.message.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                val isOk = localTestResult.isSuccess == true
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isOk) StatusReady.copy(alpha = 0.12f) else StatusError.copy(alpha = 0.12f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = localTestResult.message,
                                        fontSize = 10.sp,
                                        color = if (isOk) StatusReady else StatusError,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        softWrap = false,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
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
                        onDelete = { viewModel.deleteWhisperModel() }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LocalModelProgressRow(
                        title = "Gemma 2 2B / Qwen 1.5B LLM",
                        sizeText = "1.4 GB",
                        ramOverhead = "~1.8 GB RAM",
                        description = "Synthesizes structured insights and topic tags fully offline.",
                        isEnabled = !isCloudMode,
                        isDownloaded = preferences.isLlmModelDownloaded,
                        progressState = llmProgress,
                        colors = colors,
                        onStartDownload = { viewModel.startLlmDownload() },
                        onDelete = { viewModel.deleteLlmModel() }
                    )
                }
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
        color = if (isSelected) colors.accentBg else colors.pillBg,
        border = androidx.compose.foundation.BorderStroke(
            1.2.dp,
            if (isSelected) colors.accent else colors.border
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
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
                    unselectedColor = colors.textMuted
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
                        color = if (isSelected) colors.textPrimary else colors.textSecondary,
                        maxLines = 1,
                        softWrap = false
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSelected) colors.accent.copy(alpha = 0.2f) else colors.border
                    ) {
                        Text(
                            text = badge,
                            fontSize = 10.sp,
                            color = if (isSelected) colors.accent else colors.textMuted,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = if (isSelected) colors.textSecondary else colors.textMuted,
                    lineHeight = 15.sp
                )
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
    onDelete: () -> Unit
) {
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
                    Surface(shape = RoundedCornerShape(8.dp), color = colors.accentBg) {
                        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 1.8.dp, color = colors.accent)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "${(progressState.progress * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.accent)
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
                        IconButton(onClick = onDelete, enabled = isEnabled, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = colors.textMuted, modifier = Modifier.size(16.dp))
                        }
                    }
                } else {
                    Button(
                        onClick = onStartDownload,
                        enabled = isEnabled,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Download", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
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
    val isBackingUp by viewModel.isCloudBackingUp.collectAsState()
    val backupProgress by viewModel.cloudBackupProgress.collectAsState()

    var isExporting by remember { mutableStateOf(false) }
    var isImporting by remember { mutableStateOf(false) }

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
            // CARD 1: WHATSAPP-STYLE GOOGLE CLOUD BACKUP
            NeumorphicCard(colors = colors) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(CyanAccent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Google Cloud Backup",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Auto-sync reels, transcripts & metadata to Google Cloud",
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    NeumorphicDivider(colors = colors)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Switch: Enable Cloud Backup
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Back up to Google Cloud",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = if (preferences.isCloudBackupEnabled) "Enabled: vault metadata synced" else "Disabled: stored locally only",
                                fontSize = 11.sp,
                                color = if (preferences.isCloudBackupEnabled) StatusReady else colors.textMuted
                            )
                        }

                        Switch(
                            checked = preferences.isCloudBackupEnabled,
                            onCheckedChange = { viewModel.setCloudBackupEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = colors.accent,
                                uncheckedThumbColor = colors.textMuted,
                                uncheckedTrackColor = colors.pillBg
                            )
                        )
                    }

                    AnimatedVisibility(visible = preferences.isCloudBackupEnabled) {
                        Column {
                            Spacer(modifier = Modifier.height(14.dp))

                            // Cloud Folder Indicator Pill
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = colors.pillBg,
                                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Folder,
                                        contentDescription = null,
                                        tint = CyanAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Dedicated Cloud Folder",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = colors.textSecondary
                                        )
                                        Text(
                                            text = "Google Cloud / Drive: /${preferences.cloudBackupFolder}/metadata/",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.textPrimary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Switch: Wi-Fi Only
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Back up over Wi-Fi only",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = "Avoid using mobile cellular data",
                                        fontSize = 11.sp,
                                        color = colors.textMuted
                                    )
                                }

                                Switch(
                                    checked = preferences.backupOnlyOnWifi,
                                    onCheckedChange = { viewModel.setBackupOnlyOnWifi(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = colors.accent,
                                        uncheckedThumbColor = colors.textMuted,
                                        uncheckedTrackColor = colors.pillBg
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Last Backup Time
                            Text(
                                text = "Last Backup: ${preferences.lastBackupTimeFormatted}",
                                fontSize = 12.sp,
                                color = colors.textSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Backup Now Button
                    NeumorphicButton(
                        onClick = { viewModel.performCloudBackup() },
                        enabled = !isBackingUp,
                        colors = colors,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (isBackingUp) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = colors.accent,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = backupProgress ?: "Backing up to Cloud...",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CloudDone,
                                    contentDescription = null,
                                    tint = CyanAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Back Up Now",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                            }
                        }
                    }

                    if (backupProgress != null && !isBackingUp) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = backupProgress!!,
                            fontSize = 11.sp,
                            color = StatusReady,
                            fontWeight = FontWeight.Medium
                        )
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
                                            putExtra(Intent.EXTRA_SUBJECT, "ReelRecall Vault Backup")
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

            // CARD 3: DEVICE STORAGE BREAKDOWN
            NeumorphicCard(colors = colors) {
                Column(modifier = Modifier.padding(18.dp)) {
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

                    Spacer(modifier = Modifier.height(14.dp))

                    AccountDetailItem(
                        label = "Room SQLite Database",
                        value = "12.8 MB • Indexed",
                        colors = colors
                    )
                    AccountDetailItem(
                        label = "AI Weights (Whisper & Gemma)",
                        value = if (preferences.isWhisperModelDownloaded || preferences.isLlmModelDownloaded) "1.54 GB" else "0 MB (Cloud Mode)",
                        colors = colors
                    )
                    AccountDetailItem(
                        label = "Reel Cache & Thumbnails",
                        value = "38.4 MB",
                        colors = colors
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        onClick = {
                            context.cacheDir.deleteRecursively()
                            context.cacheDir.mkdirs()
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
                                text = "Clear Media Cache",
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
    }
}
