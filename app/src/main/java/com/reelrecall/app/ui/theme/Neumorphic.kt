package com.reelrecall.app.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Neumorphic Palette Definition supporting Obsidian Dark & Obsidian Light Themes.
 */
data class NeumorphicColors(
    val background: Color,
    val surface: Color,
    val highlight: Color,
    val shadow: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val pillBg: Color,
    val accent: Color,
    val accentBg: Color,
    val isDark: Boolean
)

val DarkNeumorphic = NeumorphicColors(
    background = Color(0xFF0C0E14),
    surface = Color(0xFF131620),
    highlight = Color(0xFF242A3D),
    shadow = Color(0xFF040508),
    border = Color(0xFF1E2333),
    textPrimary = Color(0xFFF8FAFC),
    textSecondary = Color(0xFF94A3B8),
    textMuted = Color(0xFF64748B),
    pillBg = Color(0xFF181C28),
    accent = Color(0xFF3B82F6),
    accentBg = Color(0xFF1E3A8A),
    isDark = true
)

val LightNeumorphic = NeumorphicColors(
    background = Color(0xFFF6F8FC),
    surface = Color(0xFFFFFFFF),
    highlight = Color(0xFFFFFFFF),
    shadow = Color(0x180F172A),
    border = Color(0xFFE2E8F0),
    textPrimary = Color(0xFF0F172A),
    textSecondary = Color(0xFF475569),
    textMuted = Color(0xFF94A3B8),
    pillBg = Color(0xFFF1F5F9),
    accent = Color(0xFF2563EB),
    accentBg = Color(0xFFEFF6FF),
    isDark = false
)

fun getNeumorphicColors(isDark: Boolean): NeumorphicColors =
    if (isDark) DarkNeumorphic else LightNeumorphic

/**
 * Raised Neumorphic Card with extruded dual-direction shadows and top-left bevel highlight
 */
@Composable
fun NeumorphicCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    colors: NeumorphicColors,
    elevation: Dp = 6.dp,
    borderWidth: Dp = 1.dp,
    customBorder: BorderStroke? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else Modifier

    val borderStroke = customBorder ?: BorderStroke(
        width = borderWidth,
        brush = Brush.linearGradient(
            colors = listOf(
                colors.highlight.copy(alpha = if (colors.isDark) 0.7f else 0.95f),
                colors.border,
                colors.shadow.copy(alpha = 0.4f)
            )
        )
    )

    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                spotColor = colors.shadow,
                ambientColor = colors.highlight
            )
            .clip(shape)
            .background(colors.surface)
            .border(borderStroke, shape)
            .then(clickableModifier)
    ) {
        Column {
            content()
        }
    }
}

/**
 * Neumorphic Circular Icon Button with dual-lighting bevel
 */
@Composable
fun NeumorphicIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: NeumorphicColors,
    size: Dp = 42.dp,
    elevation: Dp = 4.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = elevation,
                shape = CircleShape,
                spotColor = colors.shadow,
                ambientColor = colors.highlight
            )
            .clip(CircleShape)
            .background(colors.surface)
            .border(
                BorderStroke(
                    1.dp,
                    Brush.linearGradient(
                        listOf(
                            colors.highlight.copy(alpha = if (colors.isDark) 0.6f else 0.9f),
                            colors.border
                        )
                    )
                ),
                CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

/**
 * Neumorphic Button with Soft Extrusion
 */
@Composable
fun NeumorphicButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(14.dp),
    colors: NeumorphicColors,
    elevation: Dp = 4.dp,
    content: @Composable () -> Unit
) {
    val alpha = if (enabled) 1f else 0.5f
    Box(
        modifier = modifier
            .shadow(
                elevation = if (enabled) elevation else 0.dp,
                shape = shape,
                spotColor = colors.shadow,
                ambientColor = colors.highlight
            )
            .clip(shape)
            .background(colors.surface.copy(alpha = alpha))
            .border(
                BorderStroke(
                    1.dp,
                    Brush.linearGradient(
                        listOf(
                            colors.highlight.copy(alpha = if (colors.isDark) 0.7f else 0.95f),
                            colors.border
                        )
                    )
                ),
                shape
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
