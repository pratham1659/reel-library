package com.reelrecall.app.ui.screens.vault.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.reelrecall.app.ui.theme.ObsidianPill
import com.reelrecall.app.ui.theme.SparkleOrange
import com.reelrecall.app.ui.theme.TextMuted
import com.reelrecall.app.ui.theme.TextPrimary

@Composable
fun FloatingBottomDock(
    currentTab: Int, // 0 = Home, 1 = Settings, 2 = Knowledge
    onTabSelected: (Int) -> Unit,
    onAddClick: () -> Unit,
    onSparkleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Navigation Pill Container
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = ObsidianPill,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E313E)),
            modifier = Modifier.height(56.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home Tab
                val isHome = currentTab == 0
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .then(
                            if (isHome) Modifier.border(1.5.dp, Color.White, CircleShape)
                            else Modifier
                        )
                        .clickable { onTabSelected(0) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home",
                        tint = if (isHome) TextPrimary else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Settings Tab
                val isSettings = currentTab == 1
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .then(
                            if (isSettings) Modifier.border(1.5.dp, Color.White, CircleShape)
                            else Modifier
                        )
                        .clickable { onTabSelected(1) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = if (isSettings) TextPrimary else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Knowledge/Vault Tab
                val isVault = currentTab == 2
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .then(
                            if (isVault) Modifier.border(1.5.dp, Color.White, CircleShape)
                            else Modifier
                        )
                        .clickable { onTabSelected(2) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "Knowledge",
                        tint = if (isVault) TextPrimary else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Action Buttons Row
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            // Middle '+' Button
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(ObsidianPill)
                    .border(1.dp, Color(0xFF2E313E), CircleShape)
                    .clickable(onClick = onAddClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Reel",
                    tint = TextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Right Coral Sparkle AI Button
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(SparkleOrange)
                    .clickable(onClick = onSparkleClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "AI Assistant",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}
