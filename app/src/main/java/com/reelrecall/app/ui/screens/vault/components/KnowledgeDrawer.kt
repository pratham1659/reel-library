package com.reelrecall.app.ui.screens.vault.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.ViewSidebar
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.reelrecall.app.data.model.IngestionStatus
import com.reelrecall.app.data.model.ReelEntity
import com.reelrecall.app.ui.theme.ActiveBlue
import com.reelrecall.app.ui.theme.DarkNeumorphic
import com.reelrecall.app.ui.theme.NeumorphicColors
import com.reelrecall.app.ui.theme.StatusReady
import com.reelrecall.app.ui.theme.StatusError
import com.reelrecall.app.ui.theme.StatusProcessing
import com.reelrecall.app.ui.theme.TextMuted
import com.reelrecall.app.ui.theme.TextPrimary
import com.reelrecall.app.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class DrawerFilter {
    ALL,
    READY,
    PROCESSING,
    NOTES
}

enum class DrawerSortOrder {
    NEWEST,
    OLDEST,
    TITLE
}

@Composable
fun KnowledgeDrawerContent(
    reels: List<ReelEntity>,
    onSelectReel: (ReelEntity) -> Unit,
    onAddReel: () -> Unit = {},
    onStartMultiSelect: () -> Unit = {},
    onToggleViewMode: () -> Unit = {},
    onReprocessAll: () -> Unit = {},
    onClose: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }
    var isFilterRowVisible by remember { mutableStateOf(true) }
    var selectedFilter by remember { mutableStateOf(DrawerFilter.ALL) }
    var sortOrder by remember { mutableStateOf(DrawerSortOrder.NEWEST) }
    var isCategoryExpanded by remember { mutableStateOf(true) }
    var showMenu by remember { mutableStateOf(false) }

    // Counts for filters
    val totalCount = reels.size
    val readyCount = remember(reels) { reels.count { it.status == IngestionStatus.COMPLETED } }
    val processingCount = remember(reels) { reels.count { it.status != IngestionStatus.COMPLETED && it.status != IngestionStatus.FAILED } }
    val notesCount = remember(reels) { reels.count { !it.userNotes.isNullOrBlank() } }

    // Filter and Sort Pipeline
    val filteredReels = remember(reels, selectedFilter, searchQuery, sortOrder) {
        var list = reels

        // 1. Text Search Filter
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            list = list.filter {
                (it.title?.contains(q, ignoreCase = true) == true) ||
                (it.oneLineSummary?.contains(q, ignoreCase = true) == true) ||
                (it.creatorHandle?.contains(q, ignoreCase = true) == true) ||
                (it.tagsJson.contains(q, ignoreCase = true))
            }
        }

        // 2. Category / Status Filter
        list = when (selectedFilter) {
            DrawerFilter.ALL -> list
            DrawerFilter.READY -> list.filter { it.status == IngestionStatus.COMPLETED }
            DrawerFilter.PROCESSING -> list.filter { it.status != IngestionStatus.COMPLETED && it.status != IngestionStatus.FAILED }
            DrawerFilter.NOTES -> list.filter { !it.userNotes.isNullOrBlank() }
        }

        // 3. Sorting
        when (sortOrder) {
            DrawerSortOrder.NEWEST -> list.sortedByDescending { it.createdAt }
            DrawerSortOrder.OLDEST -> list.sortedBy { it.createdAt }
            DrawerSortOrder.TITLE -> list.sortedBy { it.title?.lowercase() ?: "" }
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .width(320.dp),
        color = Color(0xFF13141B)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            // TOP ACTION ROW (5 Fully Functional Neumorphic Buttons)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Add Reel Button
                NeumorphicDrawerButton(
                    icon = Icons.Default.Add,
                    contentDescription = "Add Reel",
                    onClick = {
                        onClose()
                        onAddReel()
                    }
                )

                // 2. Filter Toggle Button
                NeumorphicDrawerButton(
                    icon = Icons.Default.FilterList,
                    contentDescription = "Filter",
                    active = isFilterRowVisible,
                    onClick = { isFilterRowVisible = !isFilterRowVisible }
                )

                // 3. Sort Order Button
                NeumorphicDrawerButton(
                    icon = Icons.Default.SwapVert,
                    contentDescription = "Sort: ${sortOrder.name}",
                    active = sortOrder != DrawerSortOrder.NEWEST,
                    onClick = {
                        sortOrder = when (sortOrder) {
                            DrawerSortOrder.NEWEST -> DrawerSortOrder.OLDEST
                            DrawerSortOrder.OLDEST -> DrawerSortOrder.TITLE
                            DrawerSortOrder.TITLE -> DrawerSortOrder.NEWEST
                        }
                    }
                )

                // 4. View Mode / Sidebar Button
                NeumorphicDrawerButton(
                    icon = Icons.Default.GridView,
                    contentDescription = "Toggle Grid/List",
                    onClick = onToggleViewMode
                )

                // 5. Search Button
                NeumorphicDrawerButton(
                    icon = Icons.Default.Search,
                    contentDescription = "Search Drawer",
                    active = isSearchExpanded,
                    onClick = {
                        isSearchExpanded = !isSearchExpanded
                        if (!isSearchExpanded) searchQuery = ""
                    }
                )
            }

            // EXPANDABLE SEARCH INPUT (Neumorphic recessed field)
            AnimatedVisibility(
                visible = isSearchExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F1015))
                            .border(1.dp, Color(0xFF262838), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search in library...",
                                        color = TextMuted,
                                        fontSize = 12.sp
                                    )
                                }
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        color = TextPrimary,
                                        fontSize = 12.sp
                                    ),
                                    cursorBrush = SolidColor(ActiveBlue),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { searchQuery = "" },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = TextMuted,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // FILTER FUNCTIONS ABOVE "REEL LIBRARY" (Neumorphic Horizontal Filter Chips)
            AnimatedVisibility(
                visible = isFilterRowVisible,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DrawerFilterChip(
                            label = "All",
                            count = totalCount,
                            selected = selectedFilter == DrawerFilter.ALL,
                            onClick = { selectedFilter = DrawerFilter.ALL }
                        )
                        DrawerFilterChip(
                            label = "Ready",
                            count = readyCount,
                            selected = selectedFilter == DrawerFilter.READY,
                            onClick = { selectedFilter = DrawerFilter.READY }
                        )
                        DrawerFilterChip(
                            label = "Processing",
                            count = processingCount,
                            selected = selectedFilter == DrawerFilter.PROCESSING,
                            onClick = { selectedFilter = DrawerFilter.PROCESSING }
                        )
                        DrawerFilterChip(
                            label = "Notes",
                            count = notesCount,
                            selected = selectedFilter == DrawerFilter.NOTES,
                            onClick = { selectedFilter = DrawerFilter.NOTES }
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Divider(color = Color(0xFF222430), thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // CATEGORY HEADER: "Reel Library" with 3-Dot Options Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { isCategoryExpanded = !isCategoryExpanded }
                        .padding(vertical = 4.dp, horizontal = 2.dp)
                ) {
                    Icon(
                        imageVector = if (isCategoryExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                        contentDescription = "Expand/Collapse",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.VideoLibrary,
                        contentDescription = null,
                        tint = ActiveBlue,
                        modifier = Modifier.size(17.dp)
                    )
                    Text(
                        text = "Reel Library",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF222432),
                        border = androidx.compose.foundation.BorderStroke(0.6.dp, Color(0xFF2E3144))
                    ) {
                        Text(
                            text = "${filteredReels.size}",
                            fontSize = 11.sp,
                            color = ActiveBlue,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }

                // 3-DOT OPTIONS BUTTON (Opens Neumorphic Action Dropdown)
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1B1C26))
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Library Options",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier
                            .background(Color(0xFF1A1C27))
                            .border(1.dp, Color(0xFF2E3144), RoundedCornerShape(12.dp))
                    ) {
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp))
                                    Text("Select Multiple to Delete", color = Color(0xFFFF5252), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                            },
                            onClick = {
                                showMenu = false
                                onStartMultiSelect()
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                                    Text("Add New Reel", color = TextPrimary, fontSize = 13.sp)
                                }
                            },
                            onClick = {
                                showMenu = false
                                onClose()
                                onAddReel()
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                                    Text("Reprocess All Reels", color = TextPrimary, fontSize = 13.sp)
                                }
                            },
                            onClick = {
                                showMenu = false
                                onReprocessAll()
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.FilterList, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                                    Text("Reset Filters", color = TextSecondary, fontSize = 13.sp)
                                }
                            },
                            onClick = {
                                showMenu = false
                                selectedFilter = DrawerFilter.ALL
                                searchQuery = ""
                                sortOrder = DrawerSortOrder.NEWEST
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // REELS IN DRAWER LIST (Optimized Neumorphic Cards)
            if (isCategoryExpanded) {
                if (filteredReels.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No matching reels found" else "No reels in library",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    ) {
                        items(filteredReels, key = { it.id }) { reel ->
                            DrawerReelCard(
                                reel = reel,
                                onClick = {
                                    // Navigate directly to Reel Detail Page!
                                    onSelectReel(reel)
                                }
                            )
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            // Bottom drawer footer
            Divider(color = Color(0xFF222430), thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Reel Library Engine",
                    fontSize = 11.sp,
                    color = TextMuted
                )
                Text(
                    text = "${filteredReels.size} of $totalCount",
                    fontSize = 11.sp,
                    color = ActiveBlue,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun NeumorphicDrawerButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    active: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (active) Color(0xFF222536) else Color(0xFF191B24))
            .border(
                width = 1.dp,
                color = if (active) ActiveBlue.copy(alpha = 0.7f) else Color(0xFF2B2E3D),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (active) ActiveBlue else TextSecondary,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun DrawerFilterChip(
    label: String,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (selected) Color(0xFF202336) else Color(0xFF171822),
        border = androidx.compose.foundation.BorderStroke(
            0.8.dp,
            if (selected) ActiveBlue else Color(0xFF272A3A)
        ),
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) TextPrimary else TextSecondary
            )
            Text(
                text = "$count",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (selected) ActiveBlue else TextMuted
            )
        }
    }
}

@Composable
private fun DrawerReelCard(
    reel: ReelEntity,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF181A24))
            .border(0.8.dp, Color(0xFF242736), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Thumbnail with play badge
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0F1015))
        ) {
            if (!reel.thumbnailUrl.isNullOrBlank()) {
                AsyncImage(
                    model = reel.thumbnailUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Status indicator dot
            val dotColor = when (reel.status) {
                IngestionStatus.COMPLETED -> StatusReady
                IngestionStatus.FAILED -> StatusError
                else -> StatusProcessing
            }
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .align(Alignment.TopEnd)
                    .padding(1.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
        }

        // Text Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = reel.title ?: (reel.caption?.take(60) ?: "Reel #${reel.shortcode}"),
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (!reel.creatorHandle.isNullOrBlank()) {
                    val handle = reel.creatorHandle
                        .replace("&#064;", "@")
                        .replace("&#x2022;", "•")
                        .replace("&amp;", "&")
                    Text(
                        text = handle.take(20),
                        fontSize = 10.5.sp,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                val dateStr = remember(reel.createdAt) {
                    SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(reel.createdAt))
                }
                Text(
                    text = "• $dateStr",
                    fontSize = 10.5.sp,
                    color = Color(0xFF6B7280)
                )
            }
        }
    }
}
