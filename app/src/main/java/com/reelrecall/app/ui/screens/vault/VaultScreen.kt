package com.reelrecall.app.ui.screens.vault

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Info
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.shadow
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import com.reelrecall.app.data.model.ViewMode
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.reelrecall.app.data.model.IngestionStatus
import com.reelrecall.app.data.model.ReelEntity
import com.reelrecall.app.ui.screens.vault.components.SortFilterBottomSheet
import com.reelrecall.app.ui.theme.ActiveBlue
import com.reelrecall.app.ui.theme.ActiveBlueBg
import com.reelrecall.app.ui.theme.BrandRoseEnd
import com.reelrecall.app.ui.theme.BrandRoseMiddle
import com.reelrecall.app.ui.theme.BrandRoseStart
import com.reelrecall.app.ui.theme.CyanAccent
import com.reelrecall.app.ui.theme.InstagramOrange
import com.reelrecall.app.ui.theme.InstagramPink
import com.reelrecall.app.ui.theme.InstagramPurple
import com.reelrecall.app.ui.theme.WarningYellow
import com.reelrecall.app.ui.theme.ObsidianBg
import com.reelrecall.app.ui.theme.ObsidianCard
import com.reelrecall.app.ui.theme.ObsidianCardBorder
import com.reelrecall.app.ui.theme.ObsidianPill
import com.reelrecall.app.ui.theme.ObsidianPillBorder
import com.reelrecall.app.ui.theme.SparkleOrange
import com.reelrecall.app.ui.theme.StatusError
import com.reelrecall.app.ui.theme.StatusProcessing
import com.reelrecall.app.ui.theme.StatusReady
import com.reelrecall.app.ui.theme.NeumorphicCard
import com.reelrecall.app.ui.theme.NeumorphicColors
import com.reelrecall.app.ui.theme.NeumorphicIconButton
import com.reelrecall.app.ui.theme.NeumorphicButton
import com.reelrecall.app.ui.theme.getNeumorphicColors
import com.reelrecall.app.ui.theme.TextMuted
import com.reelrecall.app.ui.theme.TextPrimary
import com.reelrecall.app.ui.theme.TextSecondary
import kotlinx.serialization.json.Json

val LocalIsDarkTheme = androidx.compose.runtime.staticCompositionLocalOf { true }

@Composable
fun VaultScreen(
    viewModel: VaultViewModel,
    onOpenDrawer: () -> Unit,
    onOpenSettings: () -> Unit,
    onAddReel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isSearchActive by viewModel.isSearchActive.collectAsState()
    val isSortMenuOpen by viewModel.isSortMenuOpen.collectAsState()
    val groupedReels by viewModel.groupedReels.collectAsState()
    val hasMoreReels by viewModel.hasMoreReels.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val totalMatchingCount by viewModel.totalMatchingCount.collectAsState()

    val orderBy by viewModel.orderBy.collectAsState()
    val sortDirection by viewModel.sortDirection.collectAsState()
    val groupBy by viewModel.groupBy.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()

    val selectedTag by viewModel.selectedTag.collectAsState()
    val selectedSource by viewModel.selectedSource.collectAsState()

    val preferences by viewModel.preferences.collectAsState()
    val isDarkTheme = preferences.themeMode != "OBSIDIAN_LIGHT"
    val colors = getNeumorphicColors(isDarkTheme)

    var readerModeReel by remember { mutableStateOf<ReelEntity?>(null) }
    var fullTranscriptReel by remember { mutableStateOf<ReelEntity?>(null) }
    val selectedReelIdForDetail by viewModel.selectedReelIdForDetail.collectAsState()
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val selectedReelIds by viewModel.selectedReelIds.collectAsState()
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val searchQuery by viewModel.searchQuery.collectAsState()
    val aiSuggestions by viewModel.aiSearchSuggestions.collectAsState()
    val isAiSearchEnabled by viewModel.isAiSearchEnabled.collectAsState()

    BackHandler(enabled = isSearchActive) {
        viewModel.setSearchActive(false)
        viewModel.onSearchQueryChange("")
    }
    BackHandler(enabled = isSelectionMode) {
        viewModel.clearSelection()
    }
    BackHandler(enabled = selectedReelIdForDetail != null) {
        viewModel.selectReelForDetail(null)
    }

    androidx.compose.runtime.CompositionLocalProvider(LocalIsDarkTheme provides isDarkTheme) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(colors.background)
        ) {
        if (selectedReelIdForDetail != null) {
            ReelDetailScreen(
                reelId = selectedReelIdForDetail!!,
                viewModel = viewModel,
                colors = colors,
                onBack = { viewModel.selectReelForDetail(null) }
            )
        } else {
            // MAIN FEED VIEW (Search Bar expands right here on Home Screen!)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // TOP BAR CONTROLS: COLLAPSED VS EXPANDED SEARCH
                AnimatedVisibility(
                    visible = !isSearchActive,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Menu Button (Opens Knowledge Sidebar Drawer)
                        NeumorphicIconButton(
                            onClick = onOpenDrawer,
                            colors = colors,
                            size = 44.dp
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Open Navigation Menu",
                                tint = colors.textPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Center Sort Pill
                        Surface(
                            onClick = { viewModel.setSortMenuOpen(true) },
                            shape = RoundedCornerShape(24.dp),
                            color = if (isDarkTheme) ActiveBlueBg else colors.accentBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, colors.accent.copy(alpha = 0.6f)),
                            modifier = Modifier.height(44.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(colors.accent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FilterList,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Text(
                                    text = orderBy.label,
                                    color = colors.textPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Right Action Buttons: Search & Menu/Settings
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Search Button
                            NeumorphicIconButton(
                                onClick = { viewModel.setSearchActive(true) },
                                colors = colors,
                                size = 44.dp
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = colors.textPrimary,
                                    modifier = Modifier.size(19.dp)
                                )
                            }

                            // Menu / Settings Button
                            NeumorphicIconButton(
                                onClick = onOpenSettings,
                                colors = colors,
                                size = 44.dp
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings & Models",
                                    tint = colors.textPrimary,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }
                    }
                }

                // EXPANDED SEARCH BAR (Animated into top of Home Screen)
                AnimatedVisibility(
                    visible = isSearchActive,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Collapse / Back Button
                            NeumorphicIconButton(
                                onClick = {
                                    viewModel.setSearchActive(false)
                                    viewModel.onSearchQueryChange("")
                                },
                                colors = colors,
                                size = 44.dp
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Collapse Search",
                                    tint = colors.textPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Expanded Neumorphic Search Input with AI Sparkle Badge inside
                            NeumorphicCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                colors = colors,
                                shape = RoundedCornerShape(24.dp),
                                elevation = 4.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search",
                                        tint = if (searchQuery.isNotEmpty()) colors.accent else colors.textMuted,
                                        modifier = Modifier.size(18.dp)
                                    )

                                    BasicTextField(
                                        value = searchQuery,
                                        onValueChange = { viewModel.onSearchQueryChange(it) },
                                        singleLine = true,
                                        textStyle = TextStyle(
                                            color = colors.textPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        cursorBrush = SolidColor(colors.accent),
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 8.dp),
                                        decorationBox = { innerTextField ->
                                            if (searchQuery.isEmpty()) {
                                                Text(
                                                    text = "Search tags, context, notes...",
                                                    color = colors.textMuted,
                                                    fontSize = 12.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            innerTextField()
                                        }
                                    )

                                    if (searchQuery.isNotEmpty()) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear",
                                            tint = colors.textMuted,
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clickable { viewModel.onSearchQueryChange("") }
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }

                                    // AI BADGE / BUTTON INSIDE SEARCH BAR (Toggle AI Recommendation & Search)
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isAiSearchEnabled) colors.accentBg else colors.pillBg,
                                        border = BorderStroke(
                                            1.dp,
                                            if (isAiSearchEnabled) colors.accent else colors.border
                                        ),
                                        modifier = Modifier.clickable {
                                            viewModel.toggleAiSearch()
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = if (isAiSearchEnabled) "AI Search ON" else "AI Search OFF",
                                                tint = if (isAiSearchEnabled) SparkleOrange else colors.textMuted,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Text(
                                                text = if (isAiSearchEnabled) "AI ON" else "AI",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (isAiSearchEnabled) colors.accent else colors.textMuted
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // AI SEARCH SUGGESTIONS CHIPS (Recommends search tags when AI Search is active)
                        AnimatedVisibility(
                            visible = isAiSearchEnabled && aiSuggestions.isNotEmpty(),
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                                        modifier = Modifier.padding(end = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = SparkleOrange,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = if (searchQuery.isNotBlank()) "Recommended Tags:" else "AI Suggests:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SparkleOrange
                                        )
                                    }

                                    aiSuggestions.forEach { suggestion ->
                                        val clean = suggestion.removePrefix("#")
                                        val isSelected = searchQuery.equals(clean, ignoreCase = true) || searchQuery.equals(suggestion, ignoreCase = true)
                                        Surface(
                                            onClick = {
                                                if (isSelected) {
                                                    viewModel.onSearchQueryChange("")
                                                } else {
                                                    viewModel.onSearchQueryChange(clean)
                                                }
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (isSelected) colors.accent else colors.pillBg,
                                            border = BorderStroke(
                                                width = 0.8.dp,
                                                color = if (isSelected) colors.accent else colors.border
                                            )
                                        ) {
                                            Text(
                                                text = suggestion,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = if (isSelected) Color.White else colors.textPrimary,
                                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Active search result indicator
                if (isSearchActive && searchQuery.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val totalFound = groupedReels.sumOf { it.reels.size }
                        Text(
                            text = "Found $totalFound reel${if (totalFound == 1) "" else "s"} for \"$searchQuery\"",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.accent
                        )
                        Text(
                            text = "Clear filter",
                            fontSize = 11.sp,
                            color = colors.textMuted,
                            modifier = Modifier.clickable { viewModel.onSearchQueryChange("") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // REEL FEED OR EMPTY STATE
                if (groupedReels.isEmpty() || groupedReels.all { it.reels.isEmpty() }) {
                    if (isSearchActive && searchQuery.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 40.dp),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(colors.pillBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SearchOff,
                                        contentDescription = null,
                                        tint = colors.textMuted,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                                Text(
                                    text = "No reels match \"$searchQuery\"",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Search by tags, summary, caption, creator, or tap an AI suggestion above",
                                    fontSize = 12.sp,
                                    color = colors.textMuted,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 32.dp)
                                )
                            }
                        }
                    } else {
                        EmptyVaultView(
                            onOpenInstagram = { openInInstagram(context, "https://www.instagram.com/reels/") },
                            colors = colors
                        )
                    }
                } else {
                    val allReelIds = remember(groupedReels) { groupedReels.flatMap { it.reels }.map { it.id } }

                    val gridState = rememberLazyGridState()
                    val listState = rememberLazyListState()

                    val shouldLoadMoreGrid by remember {
                        derivedStateOf {
                            val layoutInfo = gridState.layoutInfo
                            val total = layoutInfo.totalItemsCount
                            val lastVisible = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1
                            total > 0 && lastVisible >= (total - 2)
                        }
                    }

                    val shouldLoadMoreList by remember {
                        derivedStateOf {
                            val layoutInfo = listState.layoutInfo
                            val total = layoutInfo.totalItemsCount
                            val lastVisible = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1
                            total > 0 && lastVisible >= (total - 2)
                        }
                    }

                    LaunchedEffect(shouldLoadMoreGrid) {
                        if (shouldLoadMoreGrid && hasMoreReels && !isLoadingMore) {
                            viewModel.loadNextPage()
                        }
                    }

                    LaunchedEffect(shouldLoadMoreList) {
                        if (shouldLoadMoreList && hasMoreReels && !isLoadingMore) {
                            viewModel.loadNextPage()
                        }
                    }

                    when (viewMode) {
                            ViewMode.GRID -> {
                                LazyVerticalGrid(
                                    state = gridState,
                                columns = GridCells.Fixed(2),
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                groupedReels.forEachIndexed { groupIndex, group ->
                                    if (group.reels.isNotEmpty()) {
                                        item(span = { GridItemSpan(2) }) {
                                            DateSectionHeader(
                                                primary = group.primaryHeader,
                                                secondary = group.secondaryHeader,
                                                colors = colors,
                                                showSelectButton = (groupIndex == 0),
                                                isSelectionMode = isSelectionMode,
                                                selectedCount = selectedReelIds.size,
                                                totalCount = allReelIds.size,
                                                onToggleSelectionMode = { viewModel.toggleSelectionMode() },
                                                onSelectAll = { viewModel.selectAllReels(allReelIds) },
                                                onDeleteSelected = { showDeleteConfirmDialog = true }
                                            )
                                        }

                                        items(group.reels, key = { it.id }) { reel ->
                                            RecallReelGridCard(
                                                reel = reel,
                                                colors = colors,
                                                isSelectionMode = isSelectionMode,
                                                isSelected = selectedReelIds.contains(reel.id),
                                                onClick = {
                                                    if (isSelectionMode) {
                                                        viewModel.toggleSelectReel(reel.id)
                                                    } else {
                                                        viewModel.selectReelForDetail(reel.id)
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }

                                if (isLoadingMore) {
                                    item(span = { GridItemSpan(2) }) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                modifier = Modifier
                                                    .background(colors.surface, RoundedCornerShape(20.dp))
                                                    .border(1.dp, colors.border, RoundedCornerShape(20.dp))
                                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                            ) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(14.dp),
                                                    strokeWidth = 2.dp,
                                                    color = colors.accent
                                                )
                                                Text(
                                                    text = "Loading more reels...",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = colors.textSecondary
                                                )
                                            }
                                        }
                                    }
                                } else if (!hasMoreReels && totalMatchingCount > VaultViewModel.INITIAL_PAGE_SIZE) {
                                    item(span = { GridItemSpan(2) }) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 14.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = colors.textMuted,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Text(
                                                    text = "All $totalMatchingCount reels loaded",
                                                    fontSize = 12.sp,
                                                    color = colors.textMuted
                                                )
                                            }
                                        }
                                    }
                                }

                                item(span = { GridItemSpan(2) }) {
                                    Spacer(modifier = Modifier.height(100.dp))
                                }
                            }
                        }
                        ViewMode.LIST -> {
                            LazyColumn(
                                state = listState,
                                verticalArrangement = Arrangement.spacedBy(22.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                groupedReels.forEachIndexed { groupIndex, group ->
                                    if (group.reels.isNotEmpty()) {
                                        item {
                                            DateSectionHeader(
                                                primary = group.primaryHeader,
                                                secondary = group.secondaryHeader,
                                                colors = colors,
                                                showSelectButton = (groupIndex == 0),
                                                isSelectionMode = isSelectionMode,
                                                selectedCount = selectedReelIds.size,
                                                totalCount = allReelIds.size,
                                                onToggleSelectionMode = { viewModel.toggleSelectionMode() },
                                                onSelectAll = { viewModel.selectAllReels(allReelIds) },
                                                onDeleteSelected = { showDeleteConfirmDialog = true }
                                            )
                                        }

                                        items(group.reels, key = { it.id }) { reel ->
                                            RecallReelCard(
                                                reel = reel,
                                                showTags = preferences.showTagsOnCards,
                                                colors = colors,
                                                isSelectionMode = isSelectionMode,
                                                isSelected = selectedReelIds.contains(reel.id),
                                                onOpenReel = {
                                                    if (isSelectionMode) {
                                                        viewModel.toggleSelectReel(reel.id)
                                                    } else {
                                                        viewModel.selectReelForDetail(reel.id)
                                                    }
                                                },
                                                onRetry = { viewModel.retryReel(reel) },
                                                onDelete = { viewModel.deleteReel(reel.id) }
                                            )
                                        }
                                    }
                                }

                                if (isLoadingMore) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                modifier = Modifier
                                                    .background(colors.surface, RoundedCornerShape(20.dp))
                                                    .border(1.dp, colors.border, RoundedCornerShape(20.dp))
                                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                            ) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(14.dp),
                                                    strokeWidth = 2.dp,
                                                    color = colors.accent
                                                )
                                                Text(
                                                    text = "Loading more reels...",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = colors.textSecondary
                                                )
                                            }
                                        }
                                    }
                                } else if (!hasMoreReels && totalMatchingCount > VaultViewModel.INITIAL_PAGE_SIZE) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 14.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = colors.textMuted,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Text(
                                                    text = "All $totalMatchingCount reels loaded",
                                                    fontSize = 12.sp,
                                                    color = colors.textMuted
                                                )
                                            }
                                        }
                                    }
                                }

                                item {
                                    Spacer(modifier = Modifier.height(100.dp)) // Clearance for floating dock
                                }
                            }
                        }
                    }
                }
            }
        }

        // FLOATING ADD REEL BUTTON IN BOTTOM RIGHT CORNER (Apple Glass Style)
        if (selectedReelIdForDetail == null && !isSearchActive && !isSelectionMode) {
            AppleGlassAddButton(
                onClick = onAddReel,
                isDarkTheme = isDarkTheme,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(end = 20.dp, bottom = 26.dp)
            )
        }

        // BATCH DELETE CONFIRMATION DIALOG
        if (showDeleteConfirmDialog && selectedReelIds.isNotEmpty()) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                title = {
                    Text(
                        text = "Delete Selected Reels?",
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                },
                text = {
                    Text(
                        text = "Are you sure you want to permanently delete ${selectedReelIds.size} reel(s) from Reel Library? This action cannot be undone.",
                        color = colors.textSecondary,
                        fontSize = 13.5.sp
                    )
                },
                confirmButton = {
                    androidx.compose.material3.TextButton(
                        onClick = {
                            showDeleteConfirmDialog = false
                            viewModel.deleteSelectedReels()
                        }
                    ) {
                        Text("Delete", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(onClick = { showDeleteConfirmDialog = false }) {
                        Text("Cancel", color = colors.textSecondary)
                    }
                },
                containerColor = Color(0xFF1E202B),
                shape = RoundedCornerShape(18.dp)
            )
        }

        // SORT / FILTER BOTTOM SHEET (Matching Reference Screenshot 2 & 3)
        if (isSortMenuOpen) {
            SortFilterBottomSheet(
                orderBy = orderBy,
                sortDirection = sortDirection,
                groupBy = groupBy,
                viewMode = viewMode,
                colors = colors,
                onOrderByChange = { viewModel.setOrderBy(it) },
                onSortDirectionChange = { viewModel.setSortDirection(it) },
                onGroupByChange = { viewModel.setGroupBy(it) },
                onViewModeChange = { viewModel.setViewMode(it) },
                onDismiss = { viewModel.setSortMenuOpen(false) }
            )
        }

        // READER MODE DIALOG
        readerModeReel?.let { reel ->
            ReaderModeDialog(
                reel = reel,
                onDismiss = { readerModeReel = null },
                onOpenInstagram = { openInInstagram(context, reel.canonicalUrl) }
            )
        }

        // FULL TRANSCRIPT DIALOG
        fullTranscriptReel?.let { reel ->
            FullTranscriptDialog(
                reel = reel,
                onDismiss = { fullTranscriptReel = null },
                onOpenInstagram = { openInInstagram(context, reel.canonicalUrl) },
                onRetryTranscription = { viewModel.reprocessReel(reel.id) }
            )
        }
    }
}
}

@Composable
fun DateSectionHeader(
    primary: String,
    secondary: String,
    colors: NeumorphicColors = getNeumorphicColors(true),
    showSelectButton: Boolean = false,
    isSelectionMode: Boolean = false,
    selectedCount: Int = 0,
    totalCount: Int = 0,
    onToggleSelectionMode: () -> Unit = {},
    onSelectAll: () -> Unit = {},
    onDeleteSelected: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = primary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Text(
                text = secondary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                color = colors.textMuted,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }

        // SELECT BUTTON ROW (In the same row as Today, right below the settings icon!)
        if (showSelectButton) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (isSelectionMode) {
                    if (selectedCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0x35FF5252),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.7f)),
                            modifier = Modifier
                                .height(32.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable(onClick = onDeleteSelected)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Selected",
                                    tint = Color(0xFFFF5252),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Delete ($selectedCount)",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF5252)
                                )
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = colors.accentBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.accent.copy(alpha = 0.8f)),
                        modifier = Modifier
                            .height(32.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(onClick = onToggleSelectionMode)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Done",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.accent
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = colors.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                        modifier = Modifier
                            .height(32.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(onClick = onToggleSelectionMode)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = colors.textSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Select",
                                fontSize = 12.sp,
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

@Composable
fun GlassTagChip(
    tag: String,
    colors: NeumorphicColors,
    fontSize: androidx.compose.ui.unit.TextUnit = 10.5.sp,
    paddingHorizontal: androidx.compose.ui.unit.Dp = 8.dp,
    paddingVertical: androidx.compose.ui.unit.Dp = 3.5.dp
) {
    val cleanTag = if (tag.startsWith("#")) tag else "#$tag"
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = colors.pillBg.copy(alpha = 0.85f),
        border = androidx.compose.foundation.BorderStroke(0.8.dp, colors.border.copy(alpha = 0.65f))
    ) {
        Text(
            text = cleanTag,
            fontSize = fontSize,
            color = colors.accent,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = paddingHorizontal, vertical = paddingVertical)
        )
    }
}

@Composable
fun AppleGlassAddButton(
    onClick: () -> Unit,
    isDarkTheme: Boolean = true,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(52.dp)
            .shadow(
                elevation = 12.dp,
                shape = CircleShape,
                ambientColor = Color.Black.copy(alpha = 0.55f),
                spotColor = Color.Black.copy(alpha = 0.7f)
            )
            .clip(CircleShape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF262938),
                        Color(0xFF13151F)
                    )
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0xFF5A617D),
                        Color(0xFF282C3D)
                    )
                ),
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add Reel",
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun FilterPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    colors: NeumorphicColors = getNeumorphicColors(true),
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (selected) colors.accent.copy(alpha = 0.15f) else colors.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) colors.accent else colors.border
        ),
        modifier = Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) colors.accent else colors.textSecondary,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = label,
                fontSize = 13.sp,
                color = if (selected) colors.textPrimary else colors.textSecondary,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

/**
 * Modern Recall-Style Reel Card (Neumorphic)
 */
@Composable
fun RecallReelCard(
    reel: ReelEntity,
    showTags: Boolean = true,
    colors: NeumorphicColors = getNeumorphicColors(true),
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onOpenReel: () -> Unit,
    onRetry: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tags = remember(reel.tagsJson) {
        try {
            Json.decodeFromString<List<String>>(reel.tagsJson)
        } catch (e: Exception) {
            emptyList()
        }
    }

    NeumorphicCard(
        modifier = modifier.fillMaxWidth(),
        colors = colors,
        elevation = 7.dp,
        customBorder = if (isSelectionMode && isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, colors.accent) else null,
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            // VIDEO / POSTER PREVIEW CONTAINER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surface)
                    .clickable(onClick = onOpenReel)
            ) {
                if (!reel.thumbnailUrl.isNullOrBlank()) {
                    // Blurred Background Letterbox (Exact Recall Effect)
                    AsyncImage(
                        model = reel.thumbnailUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .blur(20.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.45f))
                    )

                    // Central Vertical Portrait Video / Image
                    AsyncImage(
                        model = reel.thumbnailUrl,
                        contentDescription = "Reel Preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .width(135.dp)
                            .height(240.dp)
                            .align(Alignment.Center)
                            .clip(RoundedCornerShape(10.dp))
                    )

                    if (!isSelectionMode) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .align(Alignment.Center)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.55f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                } else {
                    // Fallback branded Tonal Rose gradient card with Instagram glyph
                    val isRestricted = reel.transcriptNote?.contains("restricted", ignoreCase = true) == true
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        BrandRoseStart.copy(alpha = 0.85f),
                                        BrandRoseMiddle.copy(alpha = 0.90f),
                                        BrandRoseEnd.copy(alpha = 0.95f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Dark scrim for contrast
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.35f))
                        )

                        // Central Frosted Information Card
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.Black.copy(alpha = 0.45f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Watch on Instagram",
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }

                                Text(
                                    text = if (!reel.creatorHandle.isNullOrBlank()) reel.creatorHandle!! else "Instagram Reel",
                                    color = Color.White,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isRestricted) WarningYellow.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.18f),
                                    border = androidx.compose.foundation.BorderStroke(0.8.dp, if (isRestricted) WarningYellow.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isRestricted) Icons.Default.Lock else Icons.AutoMirrored.Filled.OpenInNew,
                                            contentDescription = null,
                                            tint = if (isRestricted) WarningYellow else Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = if (isRestricted) "Restricted on Web • Tap to Watch" else "Tap to Watch on Instagram",
                                            color = if (isRestricted) WarningYellow else Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (isSelectionMode) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) colors.accent else Color.Black.copy(alpha = 0.55f))
                            .border(1.5.dp, if (isSelected) colors.accent else Color.White.copy(alpha = 0.7f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Notes Badge in Left Corner
                if (!reel.userNotes.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .align(if (isSelectionMode) Alignment.BottomStart else Alignment.TopStart)
                            .padding(8.dp)
                    ) {
                        NotePill()
                    }
                }

                // Status Badge in Top Right
                Box(modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)) {
                    RecallStatusPill(status = reel.status, transcript = reel.transcript, transcriptNote = reel.transcriptNote)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1. REEL CONCISE HEADER (Title)
            Text(
                text = reel.title ?: (reel.caption ?: "Instagram Reel #${reel.shortcode}"),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 22.sp
            )

            // 2. VIDEO SUMMARY (Executive summary)
            val summaryText = if (reel.status == IngestionStatus.FAILED) {
                null
            } else {
                reel.oneLineSummary ?: (if (reel.status == IngestionStatus.COMPLETED) reel.caption?.take(160) else "AI is extracting summary & takeaways...")
            }
            if (!summaryText.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = summaryText,
                    fontSize = 13.sp,
                    color = colors.textSecondary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. SOURCE: Instagram with Username
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Gradient Instagram Logo Badge
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(InstagramPurple, InstagramPink, InstagramOrange)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .border(1.2.dp, Color.White, RoundedCornerShape(3.dp))
                    )
                }

                Text(
                    text = "instagram.com",
                    fontSize = 13.sp,
                    color = colors.textSecondary,
                    fontWeight = FontWeight.Normal
                )

                if (!reel.creatorHandle.isNullOrBlank()) {
                    val cleanHandle = reel.creatorHandle
                        .replace("&#064;", "@")
                        .replace("&#x2022;", "•")
                        .replace("&amp;", "&")
                    val handleOnly = Regex("(@[a-zA-Z0-9._]+)").find(cleanHandle)?.value ?: cleanHandle.substringBefore(" •").trim()
                    Text(
                        text = "•  $handleOnly",
                        fontSize = 13.sp,
                        color = colors.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4. PROPER TAGS (Glassmorphic Chip)
            if (showTags && tags.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    tags.forEach { tag ->
                        GlassTagChip(tag = tag, colors = colors, fontSize = 11.sp)
                    }
                }
            }

            // Retry Banner if Failed
            if (reel.status == IngestionStatus.FAILED) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(StatusError.copy(alpha = 0.15f))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = reel.errorMessage ?: "Failed to process",
                        fontSize = 11.sp,
                        color = StatusError,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onRetry) {
                        Icon(Icons.Default.Refresh, contentDescription = "Retry", tint = StatusError)
                    }
                }
            }
        }
    }
}

private data class StatusPillStyle(
    val label: String,
    val bg: Color,
    val fg: Color,
    val border: Color
)

@Composable
fun RecallStatusPill(
    status: IngestionStatus,
    transcript: String? = null,
    transcriptNote: String? = null,
    isDark: Boolean = LocalIsDarkTheme.current
) {
    val isAudioUnavailable = status == IngestionStatus.NOT_SUPPORTED ||
        (status == IngestionStatus.COMPLETED && transcript.isNullOrBlank() && !transcriptNote.isNullOrBlank())

    val style = if (isDark) {
        if (isAudioUnavailable) {
            StatusPillStyle("Audio Not Available", Color(0xFF332712), Color(0xFFFFB74D), Color(0xFFFFB74D).copy(alpha = 0.35f))
        } else {
            when (status) {
                IngestionStatus.QUEUED -> StatusPillStyle("Queued", Color(0xFF332712), Color(0xFFFFB74D), Color(0xFFFFB74D).copy(alpha = 0.35f))
                IngestionStatus.EXTRACTING -> StatusPillStyle("Extracting", Color(0xFF0F2C3A), StatusProcessing, StatusProcessing.copy(alpha = 0.35f))
                IngestionStatus.TRANSCRIBING -> StatusPillStyle("Transcribing", Color(0xFF0F2C3A), StatusProcessing, StatusProcessing.copy(alpha = 0.35f))
                IngestionStatus.SUMMARIZING -> StatusPillStyle("Summarizing", Color(0xFF2E1C0A), SparkleOrange, SparkleOrange.copy(alpha = 0.35f))
                IngestionStatus.COMPLETED -> StatusPillStyle("Ready", Color(0xFF122815), StatusReady, StatusReady.copy(alpha = 0.35f))
                IngestionStatus.FAILED -> StatusPillStyle("Failed", Color(0xFF331515), StatusError, StatusError.copy(alpha = 0.35f))
                IngestionStatus.NOT_SUPPORTED -> StatusPillStyle("Audio Not Available", Color(0xFF332712), Color(0xFFFFB74D), Color(0xFFFFB74D).copy(alpha = 0.35f))
            }
        }
    } else {
        // Obsidian Light theme: clean, vibrant, modern light pastel badges with crisp high-contrast text and subtle border
        if (isAudioUnavailable) {
            StatusPillStyle("Audio Not Available", Color(0xFFFEF3C7), Color(0xFFB45309), Color(0xFFFDE68A))
        } else {
            when (status) {
                IngestionStatus.QUEUED -> StatusPillStyle("Queued", Color(0xFFFEF3C7), Color(0xFFB45309), Color(0xFFFDE68A))
                IngestionStatus.EXTRACTING -> StatusPillStyle("Extracting", Color(0xFFE0F2FE), Color(0xFF0284C7), Color(0xFFBAE6FD))
                IngestionStatus.TRANSCRIBING -> StatusPillStyle("Transcribing", Color(0xFFE0F2FE), Color(0xFF0284C7), Color(0xFFBAE6FD))
                IngestionStatus.SUMMARIZING -> StatusPillStyle("Summarizing", Color(0xFFFFEDD5), Color(0xFFEA580C), Color(0xFFFED7AA))
                IngestionStatus.COMPLETED -> StatusPillStyle("Ready", Color(0xFFDCFCE7), Color(0xFF15803D), Color(0xFF86EFAC))
                IngestionStatus.FAILED -> StatusPillStyle("Failed", Color(0xFFFEE2E2), Color(0xFFDC2626), Color(0xFFFCA5A5))
                IngestionStatus.NOT_SUPPORTED -> StatusPillStyle("Audio Not Available", Color(0xFFFEF3C7), Color(0xFFB45309), Color(0xFFFDE68A))
            }
        }
    }

    Surface(
        shape = RoundedCornerShape(3.dp),
        color = style.bg,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, style.border),
        modifier = Modifier.height(13.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 3.5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(1.5.dp)
        ) {
            if (status in listOf(IngestionStatus.EXTRACTING, IngestionStatus.TRANSCRIBING, IngestionStatus.SUMMARIZING)) {
                CircularProgressIndicator(
                    modifier = Modifier.size(5.dp),
                    strokeWidth = 0.8.dp,
                    color = style.fg
                )
            }
            Text(
                text = style.label,
                fontSize = 7.sp,
                fontWeight = FontWeight.Bold,
                color = style.fg,
                lineHeight = 7.sp
            )
        }
    }
}

@Composable
fun NotePill(
    isDark: Boolean = LocalIsDarkTheme.current
) {
    val bg = if (isDark) Color(0xFF2A200B) else Color(0xFFFEF3C7)
    val fg = if (isDark) Color(0xFFFFC107) else Color(0xFFB45309)
    val border = if (isDark) Color(0xFFFFC107).copy(alpha = 0.35f) else Color(0xFFFDE68A)

    Surface(
        shape = RoundedCornerShape(3.dp),
        color = bg,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, border),
        modifier = Modifier.height(13.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 3.5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(1.5.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Has Notes",
                tint = fg,
                modifier = Modifier.size(6.dp)
            )
            Text(
                text = "Notes",
                fontSize = 7.sp,
                fontWeight = FontWeight.Bold,
                color = fg,
                lineHeight = 7.sp
            )
        }
    }
}

/**
 * Full-Screen Search View (Matching Reference Screenshot 4)
 */
@Composable
fun SearchKnowledgeView(
    viewModel: VaultViewModel,
    onClose: () -> Unit
) {
    val query by viewModel.searchQuery.collectAsState()
    val isAiSearchEnabled by viewModel.isAiSearchEnabled.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Search Header Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Back Arrow Button
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(ObsidianPill)
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Search Input Pill
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::onSearchQueryChange,
                placeholder = { Text("Search Reel Library vault...", color = TextMuted, fontSize = 14.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(20.dp))
                },
                trailingIcon = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted)
                            }
                        }
                        IconButton(onClick = { viewModel.toggleAiSearch() }) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = if (isAiSearchEnabled) "AI Search ON" else "AI Search OFF",
                                tint = if (isAiSearchEnabled) SparkleOrange else TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(26.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = ObsidianPill,
                    unfocusedContainerColor = ObsidianPill,
                    focusedBorderColor = Color(0xFF383B4A),
                    unfocusedBorderColor = ObsidianPillBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Filter Pills Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FilterPill(icon = Icons.Default.DateRange, label = "Date", selected = false, onClick = {})
            FilterPill(icon = Icons.Default.Language, label = "Source", selected = false, onClick = {})
            FilterPill(icon = Icons.Default.LocalOffer, label = "Tags", selected = false, onClick = {})
        }

        // Empty Search Ripple Robot State (Screenshot 4)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Pulsing Robot Orb
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF14161E))
                        .border(1.dp, Color(0xFF252838), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1B1D28)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = null,
                            tint = Color(0xFFC0C4D6),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = if (query.isEmpty()) "Start typing to search..." else "Searching vault for \"$query\"...",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            }
        }
    }
}

@Composable
fun EmptyVaultView(
    onOpenInstagram: () -> Unit,
    colors: NeumorphicColors = getNeumorphicColors(true)
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(InstagramPurple, InstagramPink, InstagramOrange)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(38.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Your Reel Library Vault is Ready",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Open Instagram, tap Share on any Reel, and choose Reel Library to capture its takeaways, poster preview, and full transcript.",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        NeumorphicButton(
            onClick = onOpenInstagram,
            colors = colors,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.height(52.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Open Instagram App",
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary,
                    fontSize = 15.sp
                )
            }
        }
    }
}

private fun openInInstagram(context: Context, canonicalUrl: String) {
    val uri = Uri.parse(canonicalUrl)
    val appIntent = Intent(Intent.ACTION_VIEW, uri).apply {
        setPackage("com.instagram.android")
    }
    try {
        context.startActivity(appIntent)
    } catch (e: ActivityNotFoundException) {
        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
    }
}

/**
 * Reader Mode Sheet displaying AI Title, One-line summary, Actionable Takeaways & Transcript
 */
@Composable
fun ReaderModeDialog(
    reel: ReelEntity,
    onDismiss: () -> Unit,
    onOpenInstagram: () -> Unit
) {
    val takeaways = remember(reel.takeawaysJson) {
        try {
            Json.decodeFromString<List<String>>(reel.takeawaysJson)
        } catch (e: Exception) {
            emptyList()
        }
    }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianCardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(640.dp)
                    .clickable(enabled = false) {}
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(22.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Top Drag Indicator & Close
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ActiveBlueBg
                        ) {
                            Text(
                                text = "READER MODE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ActiveBlue,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Clear, contentDescription = "Close", tint = TextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Title
                    Text(
                        text = reel.title ?: (reel.caption ?: "Instagram Reel #${reel.shortcode}"),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        lineHeight = 26.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Source: instagram.com • ${reel.creatorHandle ?: "@creator"}",
                        fontSize = 12.sp,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // ONE-LINE SUMMARY BOX
                    if (!reel.oneLineSummary.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF101B2E),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ActiveBlue.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "💡 Executive Summary",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ActiveBlue
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = reel.oneLineSummary,
                                    fontSize = 13.sp,
                                    color = TextPrimary,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // ACTIONABLE TAKEAWAYS
                    if (takeaways.isNotEmpty()) {
                        Text(
                            text = "Key Takeaways",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        takeaways.forEach { takeaway ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 6.dp)
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(CyanAccent)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = takeaway,
                                    fontSize = 13.sp,
                                    color = TextSecondary,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // SPOKEN TRANSCRIPT PREVIEW
                    val hasSpoken = com.reelrecall.app.engine.Transcripts.hasSpokenTranscript(reel.transcript)
                    val spokenDisplay = com.reelrecall.app.engine.Transcripts.display(reel.transcript, reel.transcriptNote)
                    Text(
                        text = if (hasSpoken) "Audio Transcript" else "Audio Transcript (Unavailable)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (hasSpoken) TextPrimary else WarningYellow
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ObsidianPill,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = spokenDisplay,
                            fontSize = 12.sp,
                            color = if (hasSpoken) TextMuted else TextSecondary,
                            lineHeight = 17.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))

                    // Bottom Open in Instagram Button
                    Button(
                        onClick = onOpenInstagram,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ActiveBlue),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open Reel in Instagram", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Full Transcript Dialog
 */
@Composable
fun FullTranscriptDialog(
    reel: ReelEntity,
    onDismiss: () -> Unit,
    onOpenInstagram: () -> Unit,
    onRetryTranscription: (() -> Unit)? = null
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = ObsidianCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianCardBorder),
            modifier = Modifier.fillMaxWidth().height(520.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Spoken Audio Transcript",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Clear, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ObsidianPill)
                        .padding(14.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    val isAvailable = com.reelrecall.app.engine.Transcripts.hasSpokenTranscript(reel.transcript)
                    if (isAvailable) {
                        Column {
                            val wordCount = reel.transcript!!.split(Regex("""\s+""")).filter { it.isNotBlank() }.size
                            Text(
                                text = "Word count: ~$wordCount words",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = reel.transcript,
                                fontSize = 13.sp,
                                color = TextSecondary,
                                lineHeight = 19.5.sp
                            )
                        }
                    } else {
                        val reasonMessage = com.reelrecall.app.engine.Transcripts.display(reel.transcript, reel.transcriptNote)
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = WarningYellow,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Audio Not Available",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WarningYellow
                                )
                            }
                            Text(
                                text = reasonMessage,
                                fontSize = 13.sp,
                                color = TextSecondary,
                                lineHeight = 19.sp
                            )
                            if (!reel.caption.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Post Caption Reference:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanAccent
                                )
                                Text(
                                    text = reel.caption,
                                    fontSize = 12.5.sp,
                                    color = TextMuted,
                                    lineHeight = 18.sp
                                )
                            }
                            if (onRetryTranscription != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                TextButton(onClick = onRetryTranscription) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp), tint = ActiveBlue)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Retry transcription", color = ActiveBlue)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onOpenInstagram,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ActiveBlue),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Open Reel", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * 2-Column Grid Card for ViewMode.GRID
 */
@Composable
fun RecallReelGridCard(
    reel: ReelEntity,
    colors: NeumorphicColors,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    val tags = remember(reel.tagsJson) {
        try {
            Json.decodeFromString<List<String>>(reel.tagsJson)
        } catch (e: Exception) {
            emptyList()
        }
    }

    NeumorphicCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = colors,
        elevation = 5.dp,
        customBorder = if (isSelectionMode && isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, colors.accent) else null,
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Thumbnail container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(145.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.pillBg)
                    .clickable(onClick = onClick)
            ) {
                if (!reel.thumbnailUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = reel.thumbnailUrl,
                        contentDescription = "Thumbnail",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        BrandRoseStart.copy(alpha = 0.85f),
                                        BrandRoseMiddle.copy(alpha = 0.90f),
                                        BrandRoseEnd.copy(alpha = 0.95f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.35f))
                        )
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = if (!reel.creatorHandle.isNullOrBlank()) reel.creatorHandle!! else "#${reel.shortcode.take(6)}",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Play icon overlay or Selection Checkmark
                if (!isSelectionMode && !reel.thumbnailUrl.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .align(Alignment.Center)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(7.dp)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) colors.accent else Color.Black.copy(alpha = 0.55f))
                            .border(1.5.dp, if (isSelected) colors.accent else Color.White.copy(alpha = 0.7f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }

                // Notes Badge in Left Corner
                if (!reel.userNotes.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .align(if (isSelectionMode) Alignment.BottomStart else Alignment.TopStart)
                            .padding(6.dp)
                    ) {
                        NotePill()
                    }
                }

                // Status Badge in Top Right
                Box(modifier = Modifier.align(Alignment.TopEnd).padding(6.dp)) {
                    RecallStatusPill(status = reel.status, transcript = reel.transcript, transcriptNote = reel.transcriptNote)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Concise Header (Title)
            Text(
                text = reel.title ?: (reel.caption?.take(60) ?: "Reel #${reel.shortcode}"),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Video Summary Snippet
            val summaryText = reel.oneLineSummary ?: reel.caption?.take(90) ?: ""
            if (summaryText.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = summaryText,
                    fontSize = 11.sp,
                    color = colors.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Source (Instagram logo + username)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(13.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(InstagramPurple, InstagramPink, InstagramOrange)
                            )
                        )
                )
                val handle = reel.creatorHandle?.let { raw ->
                    val clean = raw.replace("&#064;", "@").replace("&#x2022;", "•").replace("&amp;", "&")
                    Regex("(@[a-zA-Z0-9._]+)").find(clean)?.value ?: clean.take(15)
                } ?: "Instagram"
                Text(
                    text = handle,
                    fontSize = 11.sp,
                    color = colors.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Tags (Exact Glassmorphic chip matching List view!)
            if (tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(7.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tags.forEach { tag ->
                        GlassTagChip(
                            tag = tag,
                            colors = colors,
                            fontSize = 9.5.sp,
                            paddingHorizontal = 6.dp,
                            paddingVertical = 2.dp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Structured Table for ViewMode.TABLE
 */
@Composable
fun RecallReelTable(
    reels: List<ReelEntity>,
    colors: NeumorphicColors,
    onSelectReel: (ReelEntity) -> Unit
) {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth(),
        colors = colors,
        elevation = 6.dp,
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            // Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.pillBg)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("STATUS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.textMuted, modifier = Modifier.width(62.dp))
                Text("SOURCE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.textMuted, modifier = Modifier.width(80.dp))
                Text("HEADER & SUMMARY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.textMuted, modifier = Modifier.weight(1f))
                Text("TAGS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.textMuted, modifier = Modifier.width(64.dp))
            }

            Spacer(modifier = Modifier.height(4.dp))

            reels.forEachIndexed { index, reel ->
                val tags = remember(reel.tagsJson) {
                    try { Json.decodeFromString<List<String>>(reel.tagsJson) } catch (e: Exception) { emptyList() }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSelectReel(reel) }
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status & Note
                    Row(
                        modifier = Modifier.width(82.dp),
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!reel.userNotes.isNullOrBlank()) {
                            NotePill()
                        }
                        RecallStatusPill(status = reel.status, transcript = reel.transcript, transcriptNote = reel.transcriptNote)
                    }

                    // Source
                    Column(modifier = Modifier.width(80.dp)) {
                        val handle = reel.creatorHandle?.let { raw ->
                            val clean = raw.replace("&#064;", "@").replace("&#x2022;", "•").replace("&amp;", "&")
                            Regex("(@[a-zA-Z0-9._]+)").find(clean)?.value ?: clean.take(12)
                        } ?: "Instagram"
                        Text(handle, fontSize = 11.sp, color = colors.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }

                    // Title & Summary
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(
                            text = reel.title ?: (reel.caption?.take(40) ?: "Reel #${reel.shortcode}"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val summaryText = reel.oneLineSummary ?: reel.caption?.take(60) ?: ""
                        if (summaryText.isNotBlank()) {
                            Text(
                                text = summaryText,
                                fontSize = 11.sp,
                                color = colors.textMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Tags
                    Column(modifier = Modifier.width(64.dp)) {
                        if (tags.isNotEmpty()) {
                            Text(
                                text = tags.take(2).joinToString(" ") { if (it.startsWith("#")) it else "#$it" },
                                fontSize = 10.sp,
                                color = colors.accent,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else {
                            Text("-", fontSize = 11.sp, color = colors.textMuted)
                        }
                    }
                }

                if (index < reels.size - 1) {
                    HorizontalDivider(
                        color = colors.border.copy(alpha = 0.5f),
                        thickness = 0.6.dp,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )
                }
            }
        }
    }
}

/**
 * Dedicated Full Screen Reel Detail Page with Tags Management & Notes (Hyperlink detection + Warnings)
 */
@Composable
fun ReelDetailScreen(
    reelId: Long,
    viewModel: VaultViewModel,
    colors: NeumorphicColors,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    BackHandler(onBack = onBack)

    val reelFlow = remember(reelId) { viewModel.getReel(reelId) }
    val reel by reelFlow.collectAsState(initial = null)

    if (reel == null) {
        Box(modifier = modifier.fillMaxSize().background(colors.background), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = colors.accent)
        }
        return
    }

    val currentReel = reel!!

    var notesText by remember(currentReel.id) { mutableStateOf(currentReel.userNotes ?: "") }
    var newTagInput by remember { mutableStateOf("") }
    var urlToOpenWithWarning by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showNoteDialog by remember { mutableStateOf(false) }
    var editingNoteText by remember(currentReel.id, showNoteDialog) { mutableStateOf(currentReel.userNotes ?: "") }
    var showRunAiConfirmDialog by remember { mutableStateOf(false) }

    val isRetryingMap by viewModel.isRetryingMap.collectAsState()
    val isRetrying = isRetryingMap[currentReel.id] == true

    var isTranscriptExpanded by remember(currentReel.id) { mutableStateOf(false) }
    var isCaptionExpanded by remember(currentReel.id) { mutableStateOf(false) }

    val tags = remember(currentReel.tagsJson) {
        try {
            Json.decodeFromString<List<String>>(currentReel.tagsJson)
        } catch (e: Exception) {
            emptyList()
        }
    }

    val takeaways = remember(currentReel.takeawaysJson) {
        try {
            Json.decodeFromString<List<String>>(currentReel.takeawaysJson)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Auto-reprocess queued reels when opened
    LaunchedEffect(currentReel.id) {
        if ((currentReel.status == IngestionStatus.QUEUED || currentReel.status == IngestionStatus.EXTRACTING) && !isRetrying) {
            viewModel.reprocessReel(currentReel.id)
        }
    }

    val popularTags = listOf("Tech", "AI", "Coding", "Design", "Productivity", "Business", "Marketing", "Finance", "Tutorial", "Favorites")

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
                .padding(horizontal = 16.dp, vertical = 10.dp),
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
                text = "Reel Details",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Open Instagram App
                NeumorphicIconButton(
                    onClick = { openInInstagram(context, currentReel.canonicalUrl) },
                    colors = colors,
                    size = 42.dp
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = "Open in Instagram",
                        tint = colors.accent,
                        modifier = Modifier.size(19.dp)
                    )
                }

                // Delete
                NeumorphicIconButton(
                    onClick = { showDeleteConfirmDialog = true },
                    colors = colors,
                    size = 42.dp
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = StatusError,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }

        // SCROLLABLE BODY
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // VIDEO / POSTER PREVIEW (Click to open in Instagram)
            NeumorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { openInInstagram(context, currentReel.canonicalUrl) },
                colors = colors,
                shape = RoundedCornerShape(20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.pillBg)
                ) {
                    if (!currentReel.thumbnailUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = currentReel.thumbnailUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .blur(20.dp)
                        )
                        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)))
                        AsyncImage(
                            model = currentReel.thumbnailUrl,
                            contentDescription = "Reel Preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .width(125.dp)
                                .height(220.dp)
                                .align(Alignment.Center)
                                .clip(RoundedCornerShape(10.dp))
                        )
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .align(Alignment.Center)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.6f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    } else {
                        // Fallback branded Tonal Rose card with Open in Instagram
                        val isRestricted = currentReel.transcriptNote?.contains("restricted", ignoreCase = true) == true
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            BrandRoseStart.copy(alpha = 0.85f),
                                            BrandRoseMiddle.copy(alpha = 0.90f),
                                            BrandRoseEnd.copy(alpha = 0.95f)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.35f))
                            )

                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.22f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Watch on Instagram",
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                Text(
                                    text = if (!currentReel.creatorHandle.isNullOrBlank()) currentReel.creatorHandle!! else "Instagram Reel #${currentReel.shortcode}",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isRestricted) WarningYellow.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.20f),
                                    border = androidx.compose.foundation.BorderStroke(0.8.dp, if (isRestricted) WarningYellow.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.35f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                            contentDescription = null,
                                            tint = if (isRestricted) WarningYellow else Color.White,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = "Tap to Watch on Instagram",
                                            color = if (isRestricted) WarningYellow else Color.White,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Notes Badge in Left Corner
                    if (!currentReel.userNotes.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(10.dp)
                        ) {
                            NotePill()
                        }
                    }

                    // Status Badge in Top Right
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                    ) {
                        RecallStatusPill(status = currentReel.status, transcript = currentReel.transcript, transcriptNote = currentReel.transcriptNote)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // CONCISE HEADER & SOURCE
            NeumorphicCard(
                modifier = Modifier.fillMaxWidth(),
                colors = colors,
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = currentReel.title ?: (currentReel.caption ?: "Instagram Reel #${currentReel.shortcode}"),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(InstagramPurple, InstagramPink, InstagramOrange)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .border(1.2.dp, Color.White, RoundedCornerShape(3.dp))
                                )
                            }

                            Text(
                                text = "instagram.com",
                                fontSize = 13.sp,
                                color = colors.textSecondary
                            )

                            if (!currentReel.creatorHandle.isNullOrBlank()) {
                                val cleanHandle = currentReel.creatorHandle
                                    .replace("&#064;", "@")
                                    .replace("&#x2022;", "•")
                                    .replace("&amp;", "&")
                                val handleOnly = Regex("(@[a-zA-Z0-9._]+)").find(cleanHandle)?.value ?: cleanHandle.substringBefore(" •").trim()
                                Text(
                                    text = "•  $handleOnly",
                                    fontSize = 13.sp,
                                    color = colors.textMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Note Button next to Instagram info
                        Surface(
                            onClick = {
                                editingNoteText = currentReel.userNotes ?: notesText
                                showNoteDialog = true
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = colors.pillBg,
                            border = androidx.compose.foundation.BorderStroke(0.8.dp, colors.border),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Note",
                                    tint = colors.accent,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = if (!currentReel.userNotes.isNullOrBlank() || notesText.isNotBlank()) "Edit Note" else "Note",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary
                                )
                            }
                        }
                    }

                    // USER SAVED NOTE (Displayed right below Instagram profile row)
                    val activeNote = currentReel.userNotes ?: notesText.takeIf { it.isNotBlank() }
                    if (!activeNote.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = colors.pillBg,
                            border = androidx.compose.foundation.BorderStroke(0.8.dp, colors.accent.copy(alpha = 0.45f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = null,
                                            tint = colors.accent,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = "Saved Note",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.accent,
                                            letterSpacing = 0.5.sp
                                        )
                                    }

                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Note",
                                        tint = colors.textMuted,
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clickable {
                                                editingNoteText = activeNote
                                                showNoteDialog = true
                                            }
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                HyperlinkNotesText(
                                    text = activeNote,
                                    colors = colors,
                                    onUrlClick = { url ->
                                        urlToOpenWithWarning = url
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // DETAILED EXECUTIVE SUMMARY SECTION WITH RETRY AI BUTTON
            val context = androidx.compose.ui.platform.LocalContext.current
            val clipboardManager = LocalClipboardManager.current

            NeumorphicCard(
                modifier = Modifier.fillMaxWidth(),
                colors = colors,
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    val hasAiContent = !currentReel.oneLineSummary.isNullOrBlank() || takeaways.isNotEmpty()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = SparkleOrange,
                                modifier = Modifier.size(19.dp)
                            )
                            Text(
                                text = "AI Summary",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                maxLines = 1
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Action Button (Run AI or Retry AI)
                        Surface(
                            onClick = {
                                android.util.Log.d("ReelLibrary", "Top AI Summary button clicked for ${currentReel.id}")
                                showRunAiConfirmDialog = true
                            },
                            enabled = !isRetrying,
                            shape = RoundedCornerShape(12.dp),
                            color = if (isRetrying) colors.accentBg else if (!hasAiContent) colors.accent.copy(alpha = 0.12f) else colors.pillBg,
                            border = androidx.compose.foundation.BorderStroke(
                                if (!hasAiContent) 1.dp else 0.7.dp,
                                if (isRetrying || !hasAiContent) colors.accent else colors.border
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                if (isRetrying) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(11.dp),
                                        strokeWidth = 1.5.dp,
                                        color = colors.accent
                                    )
                                    Text("Synthesizing...", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = colors.accent)
                                } else if (!hasAiContent) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = "Run AI", tint = colors.accent, modifier = Modifier.size(13.dp))
                                    Text("Run AI", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.accent)
                                } else {
                                    Icon(Icons.Default.Refresh, contentDescription = "Retry", tint = colors.textSecondary, modifier = Modifier.size(13.dp))
                                    Text("Retry AI", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = colors.textSecondary)
                                }
                            }
                        }
                    }

                    if (isRetrying) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = colors.accentBg.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(0.6.dp, colors.accent.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(13.dp), strokeWidth = 2.dp, color = colors.accent)
                                Text(
                                    text = "Analyzing audio & caption... Updating Title, AI Summary & Spoken Transcript...",
                                    fontSize = 11.5.sp,
                                    color = colors.accent
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val isFailed = currentReel.status == IngestionStatus.FAILED || !currentReel.errorMessage.isNullOrBlank()

                    if (isFailed) {
                        // DEDICATED AI FAILURE REASON CARD
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = StatusError.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, StatusError.copy(alpha = 0.45f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(StatusError.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = "Failed",
                                            tint = StatusError,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "AI Processing Failed",
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = StatusError
                                        )
                                        Text(
                                            text = "Selected model was unable to complete summarization",
                                            fontSize = 11.sp,
                                            color = colors.textMuted
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = currentReel.errorMessage ?: "Reason: Model not available or API key not configured.",
                                    fontSize = 12.5.sp,
                                    color = colors.textPrimary,
                                    lineHeight = 18.sp
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Surface(
                                    onClick = {
                                        android.util.Log.d("ReelLibrary", "Failure card Retry button clicked for ${currentReel.id}")
                                        viewModel.reprocessReel(currentReel.id)
                                        Toast.makeText(context, "Retrying AI Summarise...", Toast.LENGTH_SHORT).show()
                                    },
                                    enabled = !isRetrying,
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isRetrying) colors.pillBg else StatusError.copy(alpha = 0.12f),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isRetrying) colors.border else StatusError.copy(alpha = 0.45f)
                                    ),
                                    modifier = Modifier.fillMaxWidth().height(44.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        if (isRetrying) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(14.dp),
                                                strokeWidth = 2.dp,
                                                color = colors.accent
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Retrying AI Model...",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.accent
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = null,
                                                tint = StatusError,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Retry with Selected AI",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = StatusError
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else if (!hasAiContent) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = colors.pillBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, colors.accent.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(colors.accent.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = colors.accent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "AI Summary Ready",
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.textPrimary
                                        )
                                        Text(
                                            text = "Tap below to synthesize summary",
                                            fontSize = 11.sp,
                                            color = colors.textMuted
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Tap below to run AI summarization on demand using available caption and transcript data.",
                                    fontSize = 12.sp,
                                    color = colors.textSecondary,
                                    lineHeight = 17.sp
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                NeumorphicButton(
                                    onClick = {
                                        showRunAiConfirmDialog = true
                                    },
                                    enabled = !isRetrying,
                                    colors = colors,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    ) {
                                        if (isRetrying) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(14.dp),
                                                strokeWidth = 2.dp,
                                                color = colors.accent
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Synthesizing Summary...",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.accent
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = null,
                                                tint = colors.accent,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Run AI Summarise",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.textPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else if (currentReel.status == IngestionStatus.COMPLETED && !currentReel.oneLineSummary.isNullOrBlank()) {
                        Text(
                            text = currentReel.oneLineSummary!!,
                            fontSize = 14.5.sp,
                            color = colors.textPrimary,
                            lineHeight = 22.sp,
                            letterSpacing = 0.2.sp
                        )
                    }

                    if (!isFailed && currentReel.status == IngestionStatus.COMPLETED && takeaways.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(CyanAccent)
                            )
                            Text(
                                text = "Actionable Takeaways (${takeaways.size})",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        takeaways.forEachIndexed { idx, item ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = colors.pillBg,
                                border = androidx.compose.foundation.BorderStroke(0.6.dp, colors.border),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.5.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = "${idx + 1}.",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CyanAccent,
                                        modifier = Modifier.width(20.dp)
                                    )
                                    Text(
                                        text = item,
                                        fontSize = 13.sp,
                                        color = colors.textSecondary,
                                        lineHeight = 18.5.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // DEDICATED COLLAPSIBLE SPOKEN AUDIO TRANSCRIPT SECTION
            NeumorphicCard(
                modifier = Modifier.fillMaxWidth(),
                colors = colors,
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(19.dp)
                        )
                        Text(
                            text = "🎙️ Spoken Audio Transcript",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val hasValidTranscript = com.reelrecall.app.engine.Transcripts.hasSpokenTranscript(currentReel.transcript)
                    if (hasValidTranscript) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = colors.pillBg,
                            border = androidx.compose.foundation.BorderStroke(0.6.dp, colors.border),
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = currentReel.transcript!!,
                                    fontSize = 13.5.sp,
                                    color = colors.textPrimary,
                                    lineHeight = 21.sp,
                                    letterSpacing = 0.15.sp,
                                    maxLines = if (isTranscriptExpanded) Int.MAX_VALUE else 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    val wordCount = currentReel.transcript!!.split(Regex("""\s+""")).filter { it.isNotBlank() }.size
                                    Text(
                                        text = "Word count: ~$wordCount words",
                                        fontSize = 11.sp,
                                        color = colors.textMuted
                                    )

                                    // Expand / Collapse Toggle
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                        modifier = Modifier.clickable { isTranscriptExpanded = !isTranscriptExpanded }
                                    ) {
                                        Text(
                                            text = if (isTranscriptExpanded) "Show less" else "Show more",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = CyanAccent
                                        )
                                        Icon(
                                            imageVector = if (isTranscriptExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = null,
                                            tint = CyanAccent,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = colors.pillBg,
                            border = androidx.compose.foundation.BorderStroke(0.6.dp, colors.border),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                val reasonMessage = com.reelrecall.app.engine.Transcripts.display(currentReel.transcript, currentReel.transcriptNote)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.GraphicEq,
                                        contentDescription = null,
                                        tint = colors.textMuted,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = "Audio Not Available",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textMuted
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = reasonMessage,
                                    fontSize = 12.5.sp,
                                    color = colors.textSecondary,
                                    lineHeight = 18.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                TextButton(
                                    onClick = { viewModel.reprocessReel(currentReel.id) },
                                    enabled = !isRetrying
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = colors.accent,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isRetrying) "Retrying…" else "Retry transcription",
                                        color = colors.accent,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // DEDICATED COLLAPSIBLE ORIGINAL POST CAPTION SECTION
            if (!currentReel.caption.isNullOrBlank()) {
                NeumorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = colors,
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
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
                                    imageVector = Icons.Default.Language,
                                    contentDescription = null,
                                    tint = colors.accent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "📝 Post Caption",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = colors.pillBg,
                                border = androidx.compose.foundation.BorderStroke(0.6.dp, colors.accent.copy(alpha = 0.4f)),
                                modifier = Modifier.clickable {
                                    clipboardManager.setText(AnnotatedString(currentReel.caption))
                                    Toast.makeText(context, "Caption copied to clipboard!", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy",
                                        tint = colors.accent,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "Copy",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.accent
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = colors.pillBg,
                            border = androidx.compose.foundation.BorderStroke(0.6.dp, colors.border),
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = currentReel.caption,
                                    fontSize = 13.sp,
                                    color = colors.textSecondary,
                                    lineHeight = 20.sp,
                                    letterSpacing = 0.1.sp,
                                    maxLines = if (isCaptionExpanded) Int.MAX_VALUE else 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${currentReel.caption.length} characters",
                                        fontSize = 11.sp,
                                        color = colors.textMuted
                                    )

                                    // Expand / Collapse Toggle
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                        modifier = Modifier.clickable { isCaptionExpanded = !isCaptionExpanded }
                                    ) {
                                        Text(
                                            text = if (isCaptionExpanded) "Show less" else "Show more",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = colors.accent
                                        )
                                        Icon(
                                            imageVector = if (isCaptionExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = null,
                                            tint = colors.accent,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // TAGS SECTION (Select tags & Create custom tags)
            NeumorphicCard(
                modifier = Modifier.fillMaxWidth(),
                colors = colors,
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Tags (${tags.size})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Current Assigned Tags
                    if (tags.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            tags.forEach { tag ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = colors.accentBg,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.accent.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = if (tag.startsWith("#")) tag else "#$tag",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = colors.accent
                                        )
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove Tag",
                                            tint = colors.accent,
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clickable {
                                                    val updated = tags.filter { it != tag }
                                                    viewModel.updateReelTags(currentReel.id, updated)
                                                }
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    } else {
                        Text(
                            text = "No tags assigned yet. Select from below or create custom tags.",
                            fontSize = 12.sp,
                            color = colors.textMuted
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Popular / Suggested Tags to Toggle
                    Text(
                        text = "Suggested Tags",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textMuted
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        popularTags.forEach { popularTag ->
                            val isSelected = tags.any { it.equals(popularTag, ignoreCase = true) }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) colors.accent else colors.pillBg,
                                border = androidx.compose.foundation.BorderStroke(
                                    0.8.dp,
                                    if (isSelected) colors.accent else colors.border
                                ),
                                modifier = Modifier.clickable {
                                    val updated = if (isSelected) {
                                        tags.filterNot { it.equals(popularTag, ignoreCase = true) }
                                    } else {
                                        tags + popularTag
                                    }
                                    viewModel.updateReelTags(currentReel.id, updated)
                                }
                            ) {
                                Text(
                                    text = "+ $popularTag",
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else colors.textSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Create Custom Tag Input Field
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newTagInput,
                            onValueChange = { newTagInput = it },
                            placeholder = { Text("Create custom tag...", color = colors.textMuted, fontSize = 12.sp) },
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
                                .weight(1f)
                                .height(48.dp)
                        )

                        NeumorphicButton(
                            onClick = {
                                val trimmed = newTagInput.trim().removePrefix("#")
                                if (trimmed.isNotBlank() && !tags.any { it.equals(trimmed, ignoreCase = true) }) {
                                    val updated = tags + trimmed
                                    viewModel.updateReelTags(currentReel.id, updated)
                                    newTagInput = ""
                                }
                            },
                            colors = colors,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = colors.accent, modifier = Modifier.size(16.dp))
                                Text("Add", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // NOTE EDITING DIALOG
        if (showNoteDialog) {
            AlertDialog(
                onDismissRequest = { showNoteDialog = false },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (!currentReel.userNotes.isNullOrBlank() || notesText.isNotBlank()) "Edit Reel Note" else "Add Reel Note",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                    }
                },
                text = {
                    Column {
                        Text(
                            text = "Save personal notes, thoughts, or paste links for this reel.",
                            fontSize = 12.sp,
                            color = colors.textMuted
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = editingNoteText,
                            onValueChange = { editingNoteText = it },
                            placeholder = {
                                Text(
                                    text = "Write your note or paste links here...",
                                    color = colors.textMuted,
                                    fontSize = 13.sp
                                )
                            },
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
                                .height(140.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val cleanNote = editingNoteText.trim().ifBlank { null }
                            notesText = cleanNote ?: ""
                            viewModel.updateReelNotes(currentReel.id, cleanNote)
                            showNoteDialog = false
                            Toast.makeText(context, if (cleanNote != null) "Note saved!" else "Note cleared!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
                    ) {
                        Text("Save Note", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNoteDialog = false }) {
                        Text("Cancel", color = colors.textMuted)
                    }
                },
                containerColor = colors.surface,
                shape = RoundedCornerShape(18.dp)
            )
        }

        // EXTERNAL LINK WARNING ALERT DIALOG
        urlToOpenWithWarning?.let { url ->
            AlertDialog(
                onDismissRequest = { urlToOpenWithWarning = null },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = SparkleOrange,
                        modifier = Modifier.size(28.dp)
                    )
                },
                title = {
                    Text(
                        text = "Leaving Application",
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "You are about to leave Reel Library to open an external link in your web browser:",
                            fontSize = 13.sp,
                            color = colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = colors.pillBg,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = url,
                                fontSize = 12.sp,
                                color = colors.accent,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Are you sure you want to proceed?",
                            fontSize = 13.sp,
                            color = colors.textSecondary
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            urlToOpenWithWarning = null
                            try {
                                val fullUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) "https://$url" else url
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fullUrl))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Could not open browser: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Text("Open Link", color = colors.accent, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { urlToOpenWithWarning = null }) {
                        Text("Cancel", color = colors.textMuted)
                    }
                },
                containerColor = colors.surface,
                shape = RoundedCornerShape(20.dp)
            )
        }

        // RUN AI CONFIRMATION DIALOG
        if (showRunAiConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showRunAiConfirmDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = colors.accent, modifier = Modifier.size(20.dp))
                        Text("Run AI Analysis", fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    }
                },
                text = {
                    Text(
                        text = "Running AI will analyze this reel and regenerate the Title, AI Summary, Key Takeaways, and Spoken Audio Transcript.\n\nDo you want to proceed?",
                        color = colors.textSecondary,
                        fontSize = 13.5.sp,
                        lineHeight = 20.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showRunAiConfirmDialog = false
                            viewModel.reprocessReel(currentReel.id)
                            Toast.makeText(
                                context,
                                "Running AI... Updating Title, Summary & Spoken Transcript",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Run AI", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRunAiConfirmDialog = false }) {
                        Text("Cancel", color = colors.textMuted)
                    }
                },
                containerColor = colors.surface,
                shape = RoundedCornerShape(20.dp)
            )
        }

        // DELETE REEL CONFIRMATION DIALOG
        if (showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                title = { Text("Delete Reel", fontWeight = FontWeight.Bold, color = colors.textPrimary) },
                text = { Text("Are you sure you want to delete this Reel from your library?", color = colors.textSecondary) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteConfirmDialog = false
                            viewModel.deleteReel(currentReel.id)
                            onBack()
                        }
                    ) {
                        Text("Delete", color = StatusError, fontWeight = FontWeight.Bold)
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
    }
}

/**
 * Clickable Text component that highlights hyperlinks and detects taps
 */
@Composable
fun HyperlinkNotesText(
    text: String,
    colors: NeumorphicColors,
    onUrlClick: (String) -> Unit
) {
    val urlRegex = Regex("(https?://[a-zA-Z0-9\\-\\._~:/?#\\[\\]@!$&'()*+,;=%]+|www\\.[a-zA-Z0-9\\-\\._~:/?#\\[\\]@!$&'()*+,;=%]+)")
    val matches = urlRegex.findAll(text).toList()

    if (matches.isEmpty()) {
        Text(
            text = text,
            fontSize = 13.sp,
            color = colors.textPrimary,
            lineHeight = 19.sp
        )
    } else {
        val annotatedString = buildAnnotatedString {
            var lastIdx = 0
            matches.forEach { match ->
                val range = match.range
                if (range.first > lastIdx) {
                    append(text.substring(lastIdx, range.first))
                }
                val url = match.value
                val start = length
                append(url)
                val end = length
                addStyle(
                    style = SpanStyle(
                        color = colors.accent,
                        textDecoration = TextDecoration.Underline,
                        fontWeight = FontWeight.SemiBold
                    ),
                    start = start,
                    end = end
                )
                addStringAnnotation(
                    tag = "URL",
                    annotation = url,
                    start = start,
                    end = end
                )
                lastIdx = range.last + 1
            }
            if (lastIdx < text.length) {
                append(text.substring(lastIdx))
            }
        }

        ClickableText(
            text = annotatedString,
            style = androidx.compose.ui.text.TextStyle(
                fontSize = 13.sp,
                color = colors.textPrimary,
                lineHeight = 19.sp
            ),
            onClick = { offset ->
                val match = annotatedString.getStringAnnotations(tag = "URL", start = 0, end = annotatedString.length)
                    .firstOrNull { it.start <= offset && offset <= it.end }
                    ?: annotatedString.getStringAnnotations(tag = "URL", start = offset, end = (offset + 1).coerceAtMost(annotatedString.length)).firstOrNull()
                match?.let { annotation ->
                    onUrlClick(annotation.item)
                }
            }
        )
    }
}


