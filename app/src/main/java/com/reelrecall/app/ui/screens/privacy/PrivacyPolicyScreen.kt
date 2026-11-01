package com.reelrecall.app.ui.screens.privacy

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NoAccounts
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reelrecall.app.ui.theme.BrandRoseEnd
import com.reelrecall.app.ui.theme.BrandRoseMiddle
import com.reelrecall.app.ui.theme.BrandRoseStart
import com.reelrecall.app.ui.theme.CyanAccent
import com.reelrecall.app.ui.theme.NeumorphicButton
import com.reelrecall.app.ui.theme.NeumorphicColors
import com.reelrecall.app.ui.theme.ObsidianBg
import com.reelrecall.app.ui.theme.ObsidianCard
import com.reelrecall.app.ui.theme.ObsidianPill
import com.reelrecall.app.ui.theme.TextMuted
import com.reelrecall.app.ui.theme.TextPrimary
import com.reelrecall.app.ui.theme.TextSecondary

@Composable
fun PrivacyPolicyScreen(
    colors: NeumorphicColors,
    onAccept: () -> Unit
) {
    var hasAgreed by remember { mutableStateOf(false) }
    var showFullPolicyDialog by remember { mutableStateOf(false) }

    val brandGradient = remember {
        Brush.linearGradient(
            listOf(BrandRoseStart, BrandRoseMiddle, BrandRoseEnd)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // SCROLLABLE CONTENT BODY
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // TOP BRAND LOGO / SHIELD BADGE
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(brandGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Privacy Shield",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Welcome to Reel Library",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Your Private, Local AI Video Knowledge Vault",
                    fontSize = 13.5.sp,
                    color = colors.accent,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Before you begin, please review and accept our privacy policy. Reel Library is built from the ground up for strict privacy, local data storage, and zero data tracking.",
                    fontSize = 12.5.sp,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(22.dp))

                // PRIVACY COMMITMENT HIGHLIGHT CARDS
                PrivacyCommitmentCard(
                    icon = Icons.Default.NoAccounts,
                    title = "Zero Phone Permissions",
                    description = "We do NOT look for or request any invasive phone access. No contacts, photos, camera, location, or microphone. Reel Library operates solely via Android link sharing.",
                    colors = colors
                )

                Spacer(modifier = Modifier.height(12.dp))

                PrivacyCommitmentCard(
                    icon = Icons.Default.Storage,
                    title = "Stored Locally in Your Vault",
                    description = "All saved reels, audio transcripts, takeaways, and notes are stored strictly on your phone in an encrypted SQLite vault backed by Android Keystore hardware security.",
                    colors = colors
                )

                Spacer(modifier = Modifier.height(12.dp))

                PrivacyCommitmentCard(
                    icon = Icons.Default.Key,
                    title = "Bring Your Own Key (BYOK)",
                    description = "Your API keys (Groq, OpenAI, Claude) are saved in hardware-backed storage. Inference calls connect directly to your chosen provider via HTTPS with zero intermediary servers.",
                    colors = colors
                )

                Spacer(modifier = Modifier.height(12.dp))

                PrivacyCommitmentCard(
                    icon = Icons.Default.Security,
                    title = "No Ads & Zero Telemetry Tracking",
                    description = "Reel Library never tracks your usage, profiles your interests, or sells your content. You have complete data ownership and can export or delete your vault anytime.",
                    colors = colors
                )

                Spacer(modifier = Modifier.height(16.dp))

                // "READ MORE" BUTTON
                Surface(
                    onClick = { showFullPolicyDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    color = colors.pillBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Read Full Privacy Policy & Terms",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.accent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // BOTTOM ACTION BAR: AGREEMENT CHECKBOX & ENTER BUTTON
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Checkbox Agreement Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { hasAgreed = !hasAgreed }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = hasAgreed,
                        onCheckedChange = { hasAgreed = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = colors.accent,
                            uncheckedColor = colors.textMuted,
                            checkmarkColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "I have read and agree to the Privacy Policy and local vault data storage terms.",
                        fontSize = 12.sp,
                        color = colors.textPrimary,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Enter Application Button
                NeumorphicButton(
                    onClick = {
                        if (hasAgreed) {
                            onAccept()
                        }
                    },
                    colors = colors,
                    enabled = hasAgreed,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Enter Reel Library",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (hasAgreed) Color.White else colors.textMuted
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = if (hasAgreed) Color.White else colors.textMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Acceptance is required to initialize your encrypted local vault.",
                    fontSize = 11.sp,
                    color = colors.textMuted
                )
            }
        }

        // FULL PRIVACY POLICY MODAL DIALOG ("READ MORE")
        if (showFullPolicyDialog) {
            FullPrivacyPolicyDialog(
                colors = colors,
                onDismiss = { showFullPolicyDialog = false }
            )
        }
    }
}

@Composable
private fun PrivacyCommitmentCard(
    icon: ImageVector,
    title: String,
    description: String,
    colors: NeumorphicColors
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = colors.pillBg,
        border = androidx.compose.foundation.BorderStroke(0.8.dp, colors.border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(colors.accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = colors.textSecondary,
                    lineHeight = 17.sp
                )
            }
        }
    }
}

@Composable
private fun FullPrivacyPolicyDialog(
    colors: NeumorphicColors,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Full Privacy Policy",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .height(420.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                PolicySection(
                    title = "1. Zero Phone Access Guarantee",
                    content = "Reel Library does not seek, request, or access any sensitive hardware permissions on your phone. This includes contacts, photos/media storage, camera, audio microphone, SMS, call logs, and GPS location. Reel Library processes data solely when you explicitly share an Instagram Reel URL via Android's native share menu."
                )

                PolicySection(
                    title = "2. 100% Local Hardware-Backed Vault",
                    content = "Every reel link, title, transcript, note, and takeaway is stored strictly inside your phone's local Room SQLite database. Private credentials, such as AI API keys, are protected with hardware-level Android Keystore encryption (AES-256-GCM / MasterKey) and are never uploaded to any proprietary Reel Library server."
                )

                PolicySection(
                    title = "3. AI Inference & BYOK Direct Connect",
                    content = "Reel Library follows a Bring Your Own Key (BYOK) architecture. When running AI summarization or transcription, requests travel directly from your phone to your chosen provider's official API endpoint (Groq, OpenAI, Anthropic Claude, or custom OpenAI-compatible endpoints) over TLS/HTTPS. We operate zero intermediary analytics or telemetry relay proxies."
                )

                PolicySection(
                    title = "4. Optional Cloud Backups",
                    content = "Cloud backup is completely optional and disabled by default. If you choose to enable cloud backups, Reel Library connects directly to your personal Google Drive with restricted app-specific folder access (drive.file scope). No third party or developer has access to your backup archives."
                )

                PolicySection(
                    title = "5. No Advertisements, Tracking, or Profiling",
                    content = "Reel Library is completely ad-free and contains zero commercial tracking SDKs, device fingerprinting, or user behavioral profiling libraries. We never sell, monetize, or harvest your personal content or indexed Reels."
                )

                PolicySection(
                    title = "6. Data Ownership, Portability & Deletion",
                    content = "You maintain full ownership of all data in your vault. You may export your entire vault as JSON or ZIP archives at any time. You can also delete individual reels or purge the entire vault instantly from Settings."
                )

                PolicySection(
                    title = "7. Open Source & License",
                    content = "Reel Library is distributed under the open-source MIT License. Crafted with Pratham Builds. For inquiries or audits, users can review the codebase directly."
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", fontWeight = FontWeight.Bold, color = colors.accent)
            }
        }
    )
}

@Composable
private fun PolicySection(title: String, content: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = content,
            fontSize = 12.sp,
            color = TextSecondary,
            lineHeight = 17.sp
        )
    }
}
