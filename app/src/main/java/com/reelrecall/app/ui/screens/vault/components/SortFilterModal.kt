package com.reelrecall.app.ui.screens.vault.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reelrecall.app.data.model.GroupBy
import com.reelrecall.app.data.model.OrderBy
import com.reelrecall.app.data.model.SortDirection
import com.reelrecall.app.data.model.ViewMode
import com.reelrecall.app.ui.theme.ObsidianCard
import com.reelrecall.app.ui.theme.ObsidianPill
import com.reelrecall.app.ui.theme.SparkleOrange
import com.reelrecall.app.ui.theme.TextMuted
import com.reelrecall.app.ui.theme.TextPrimary
import com.reelrecall.app.ui.theme.TextSecondary

import com.reelrecall.app.ui.theme.NeumorphicColors
import com.reelrecall.app.ui.theme.getNeumorphicColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortFilterBottomSheet(
    orderBy: OrderBy,
    sortDirection: SortDirection,
    groupBy: GroupBy,
    viewMode: ViewMode,
    colors: NeumorphicColors = getNeumorphicColors(true),
    onOrderByChange: (OrderBy) -> Unit,
    onSortDirectionChange: (SortDirection) -> Unit,
    onGroupByChange: (GroupBy) -> Unit,
    onViewModeChange: (ViewMode) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // ORDER BY SECTION
            SectionTitle(title = "ORDER BY", color = colors.textMuted)
            OrderBy.values().forEach { option ->
                RadioOptionRow(
                    label = option.label,
                    selected = orderBy == option,
                    colors = colors,
                    onClick = {
                        onOrderByChange(option)
                        onDismiss()
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = colors.border, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // DIRECTION SECTION
            SectionTitle(title = "DIRECTION", color = colors.textMuted)
            SortDirection.values().forEach { option ->
                RadioOptionRow(
                    label = option.label,
                    selected = sortDirection == option,
                    colors = colors,
                    onClick = {
                        onSortDirectionChange(option)
                        onDismiss()
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = colors.border, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // GROUP BY SECTION
            SectionTitle(title = "GROUP BY", color = colors.textMuted)
            GroupBy.values().forEach { option ->
                RadioOptionRow(
                    label = option.label,
                    selected = groupBy == option,
                    colors = colors,
                    onClick = {
                        onGroupByChange(option)
                        onDismiss()
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = colors.border, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // VIEW MODE SEGMENTED CONTROL
            SectionTitle(title = "VIEW", color = colors.textMuted)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.pillBg)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ViewModeSegment(
                    title = "Grid",
                    icon = Icons.Default.GridView,
                    selected = viewMode == ViewMode.GRID,
                    colors = colors,
                    onClick = {
                        onViewModeChange(ViewMode.GRID)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                )
                ViewModeSegment(
                    title = "List",
                    icon = Icons.Default.FormatListBulleted,
                    selected = viewMode == ViewMode.LIST,
                    colors = colors,
                    onClick = {
                        onViewModeChange(ViewMode.LIST)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun SectionTitle(title: String, color: Color) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        color = color,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun RadioOptionRow(
    label: String,
    selected: Boolean,
    colors: NeumorphicColors,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = colors.accent,
                unselectedColor = colors.textMuted
            ),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            color = if (selected) colors.textPrimary else colors.textSecondary,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
        )
    }
}

@Composable
private fun ViewModeSegment(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    colors: NeumorphicColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (selected) colors.surface else Color.Transparent,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (selected) colors.accent else colors.textMuted,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) colors.textPrimary else colors.textMuted
            )
        }
    }
}
