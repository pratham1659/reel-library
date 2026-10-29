package com.reelrecall.app.ui.screens.vault

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.reelrecall.app.data.local.AppDatabase
import com.reelrecall.app.data.model.GroupBy
import com.reelrecall.app.data.model.IngestionStatus
import com.reelrecall.app.data.model.OrderBy
import com.reelrecall.app.data.model.ReelEntity
import com.reelrecall.app.data.model.SortDirection
import com.reelrecall.app.data.model.ViewMode
import com.reelrecall.app.worker.ReelIngestWorker
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

data class DateGroup(
    val primaryHeader: String,   // e.g. "Today", "Yesterday", "Oct 25"
    val secondaryHeader: String, // e.g. "Oct 28"
    val reels: List<ReelEntity>
)

@OptIn(ExperimentalCoroutinesApi::class)
class VaultViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val dao = db.reelDao()
    private val preferencesRepo = com.reelrecall.app.data.repository.PreferencesRepository(application)

    val preferences: StateFlow<com.reelrecall.app.data.repository.AppPreferences> = preferencesRepo.preferencesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = com.reelrecall.app.data.repository.AppPreferences()
    )

    val searchQuery = MutableStateFlow("")
    val isSearchActive = MutableStateFlow(false)
    val isSortMenuOpen = MutableStateFlow(false)
    val isDrawerOpen = MutableStateFlow(false)

    val orderBy = MutableStateFlow(OrderBy.LAST_UPDATED)
    val sortDirection = MutableStateFlow(SortDirection.DESCENDING)
    val groupBy = MutableStateFlow(GroupBy.DAY)
    val viewMode = MutableStateFlow(ViewMode.GRID)

    val selectedTag = MutableStateFlow<String?>(null)
    val selectedSource = MutableStateFlow<String?>(null)

    val selectedReelIdForDetail = MutableStateFlow<Long?>(null)
    fun selectReelForDetail(id: Long?) {
        selectedReelIdForDetail.value = id
    }

    val isSelectionMode = MutableStateFlow(false)
    val selectedReelIds = MutableStateFlow<Set<Long>>(emptySet())

    fun toggleSelectionMode(enable: Boolean? = null) {
        val next = enable ?: !isSelectionMode.value
        isSelectionMode.value = next
        if (!next) {
            selectedReelIds.value = emptySet()
        }
    }

    fun toggleSelectReel(id: Long) {
        val current = selectedReelIds.value.toMutableSet()
        if (current.contains(id)) {
            current.remove(id)
        } else {
            current.add(id)
        }
        selectedReelIds.value = current
    }

    fun selectAllReels(allIds: List<Long>) {
        if (selectedReelIds.value.size == allIds.size) {
            selectedReelIds.value = emptySet()
        } else {
            selectedReelIds.value = allIds.toSet()
        }
    }

    fun clearSelection() {
        selectedReelIds.value = emptySet()
        isSelectionMode.value = false
    }

    fun deleteSelectedReels() {
        val idsToDelete = selectedReelIds.value.toList()
        if (idsToDelete.isEmpty()) return
        viewModelScope.launch {
            dao.deleteByIds(idsToDelete)
            clearSelection()
        }
    }

    // Dynamic AI Search Suggestions derived from reel content and tags
    val aiSearchSuggestions: StateFlow<List<String>> = dao.getAllReels().map { reels ->
        val tags = mutableSetOf<String>()
        reels.forEach { reel ->
            try {
                val list = Json.decodeFromString<List<String>>(reel.tagsJson)
                tags.addAll(list.map { if (it.startsWith("#")) it else "#$it" })
            } catch (_: Exception) {}
        }
        val defaultAi = listOf(
            "AI & Models",
            "Performance",
            "Android",
            "Takeaways",
            "Coding",
            "Architecture"
        )
        (tags.toList() + defaultAi).distinct().take(10)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Comprehensive real-time multi-field search across titles, summaries, tags, context, notes & handles
    private val rawReelsFlow = combine(dao.getAllReels(), searchQuery) { allReels, query ->
        val q = query.trim()
        if (q.isBlank()) {
            allReels
        } else {
            val terms = q.lowercase().split("\\s+".toRegex()).filter { it.isNotBlank() }
            allReels.filter { reel ->
                terms.all { term ->
                    val cleanTerm = term.removePrefix("#").removePrefix("@")
                    (reel.title?.contains(term, ignoreCase = true) == true) ||
                    (reel.title?.contains(cleanTerm, ignoreCase = true) == true) ||
                    (reel.oneLineSummary?.contains(term, ignoreCase = true) == true) ||
                    (reel.oneLineSummary?.contains(cleanTerm, ignoreCase = true) == true) ||
                    (reel.tagsJson.contains(term, ignoreCase = true)) ||
                    (reel.tagsJson.contains(cleanTerm, ignoreCase = true)) ||
                    (reel.takeawaysJson.contains(term, ignoreCase = true)) ||
                    (reel.takeawaysJson.contains(cleanTerm, ignoreCase = true)) ||
                    (reel.caption?.contains(term, ignoreCase = true) == true) ||
                    (reel.transcript?.contains(term, ignoreCase = true) == true) ||
                    (reel.userNotes?.contains(term, ignoreCase = true) == true) ||
                    (reel.creatorHandle?.contains(term, ignoreCase = true) == true) ||
                    (reel.creatorHandle?.contains(cleanTerm, ignoreCase = true) == true) ||
                    (reel.canonicalUrl.contains(term, ignoreCase = true))
                }
            }
        }
    }

    // Processed & Filtered Reels Flow
    private val filterConfigFlow = combine(orderBy, sortDirection, groupBy) { order, direction, group ->
        Triple(order, direction, group)
    }

    private val selectionFilterFlow = combine(selectedTag, selectedSource) { tag, source ->
        Pair(tag, source)
    }

    companion object {
        const val INITIAL_PAGE_SIZE = 10
        const val PAGE_SIZE_INCREMENT = 10
    }

    val visibleItemsCount = MutableStateFlow(INITIAL_PAGE_SIZE)
    val isLoadingMore = MutableStateFlow(false)
    val totalMatchingCount = MutableStateFlow(0)
    val hasMoreReels = MutableStateFlow(false)
    val allMatchingReelIds = MutableStateFlow<List<Long>>(emptyList())

    init {
        // Reset pagination window whenever filters or sorting change
        viewModelScope.launch {
            combine(rawReelsFlow, filterConfigFlow, selectionFilterFlow) { _, _, _ -> }
                .collect {
                    visibleItemsCount.value = INITIAL_PAGE_SIZE
                }
        }
    }

    fun loadNextPage() {
        if (isLoadingMore.value || !hasMoreReels.value) return
        viewModelScope.launch {
            isLoadingMore.value = true
            kotlinx.coroutines.delay(180)
            visibleItemsCount.value += PAGE_SIZE_INCREMENT
            isLoadingMore.value = false
        }
    }

    fun resetPagination() {
        visibleItemsCount.value = INITIAL_PAGE_SIZE
    }

    val groupedReels: StateFlow<List<DateGroup>> = combine(
        rawReelsFlow,
        filterConfigFlow,
        selectionFilterFlow,
        visibleItemsCount
    ) { reelsList: List<ReelEntity>, (order, direction, group), (tag, source), visibleCount ->

        // Step 1: Filter by tag & source
        var filtered: List<ReelEntity> = reelsList
        if (tag != null) {
            filtered = filtered.filter { it.tagsJson.contains(tag, ignoreCase = true) }
        }
        if (source != null) {
            filtered = filtered.filter { it.canonicalUrl.contains(source, ignoreCase = true) }
        }

        // Step 2: Sort
        filtered = when (order) {
            OrderBy.LAST_UPDATED, OrderBy.CREATED_AT -> {
                if (direction == SortDirection.DESCENDING) filtered.sortedByDescending { it.createdAt }
                else filtered.sortedBy { it.createdAt }
            }
        }

        totalMatchingCount.value = filtered.size
        hasMoreReels.value = filtered.size > visibleCount
        allMatchingReelIds.value = filtered.map { it.id }

        // Step 3: Lazy Windowing / Slicing - process only visible batch for speed & low memory footprint
        val windowed = filtered.take(visibleCount)

        // Step 4: Group by Date
        if (group == GroupBy.NONE) {
            listOf(DateGroup(primaryHeader = "All Reels", secondaryHeader = "${windowed.size} of ${filtered.size} items", reels = windowed))
        } else {
            val today = LocalDate.now()
            val zone = ZoneId.systemDefault()
            val dayFormatter = DateTimeFormatter.ofPattern("MMM d")
            val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")

            val groupsMap = when (group) {
                GroupBy.DAY -> {
                    windowed.groupBy { entity ->
                        val date = Instant.ofEpochMilli(entity.createdAt).atZone(zone).toLocalDate()
                        val daysDiff = ChronoUnit.DAYS.between(date, today)

                        val (primary, secondary) = when {
                            daysDiff == 0L -> "Today" to date.format(dayFormatter)
                            daysDiff == 1L -> "Yesterday" to date.format(dayFormatter)
                            daysDiff in 2..6 -> date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() } to date.format(dayFormatter)
                            else -> date.format(dayFormatter) to "${date.year}"
                        }
                        primary to secondary
                    }
                }
                GroupBy.WEEK -> {
                    windowed.groupBy { entity ->
                        val date = Instant.ofEpochMilli(entity.createdAt).atZone(zone).toLocalDate()
                        val daysDiff = ChronoUnit.DAYS.between(date, today)
                        val weeksDiff = daysDiff / 7

                        val (primary, secondary) = when {
                            weeksDiff == 0L -> "This Week" to "Recent"
                            weeksDiff == 1L -> "Last Week" to "7-14 days ago"
                            weeksDiff in 2..3 -> "$weeksDiff Weeks Ago" to "Past month"
                            else -> "Earlier" to "${date.year}"
                        }
                        primary to secondary
                    }
                }
                GroupBy.MONTH -> {
                    windowed.groupBy { entity ->
                        val date = Instant.ofEpochMilli(entity.createdAt).atZone(zone).toLocalDate()
                        val primary = date.format(monthFormatter)
                        val secondary = date.month.name.lowercase().replaceFirstChar { it.uppercase() }
                        primary to secondary
                    }
                }
                GroupBy.NONE -> emptyMap()
            }

            groupsMap.map { entry ->
                DateGroup(
                    primaryHeader = entry.key.first,
                    secondaryHeader = entry.key.second,
                    reels = entry.value
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChange(newQuery: String) {
        searchQuery.value = newQuery
    }

    fun setSearchActive(active: Boolean) {
        isSearchActive.value = active
        if (!active) {
            searchQuery.value = ""
        }
    }

    fun setSortMenuOpen(open: Boolean) {
        isSortMenuOpen.value = open
    }

    fun setDrawerOpen(open: Boolean) {
        isDrawerOpen.value = open
    }

    fun setOrderBy(order: OrderBy) {
        orderBy.value = order
    }

    fun setSortDirection(direction: SortDirection) {
        sortDirection.value = direction
    }

    fun setGroupBy(group: GroupBy) {
        groupBy.value = group
    }

    fun setViewMode(mode: ViewMode) {
        viewMode.value = mode
    }

    fun onTagSelected(tag: String?) {
        selectedTag.value = if (selectedTag.value == tag) null else tag
    }

    fun onSourceSelected(source: String?) {
        selectedSource.value = if (selectedSource.value == source) null else source
    }

    fun deleteReel(id: Long) {
        viewModelScope.launch {
            dao.deleteById(id)
        }
    }

    fun clearAllReels() {
        viewModelScope.launch {
            dao.deleteAll()
        }
    }

    fun retryReel(reel: ReelEntity) {
        viewModelScope.launch {
            dao.updateStatus(reel.id, IngestionStatus.QUEUED, null)
            val workRequest = androidx.work.OneTimeWorkRequestBuilder<ReelIngestWorker>()
                .setInputData(
                    androidx.work.workDataOf(
                        ReelIngestWorker.KEY_REEL_ID to reel.id,
                        ReelIngestWorker.KEY_CANONICAL_URL to reel.canonicalUrl
                    )
                )
                .build()

            androidx.work.WorkManager.getInstance(getApplication()).enqueueUniqueWork(
                "ingest_${reel.shortcode}",
                androidx.work.ExistingWorkPolicy.REPLACE,
                workRequest
            )
        }
    }

    fun getReel(id: Long) = dao.getReelById(id)

    private val reprocessMutex = kotlinx.coroutines.sync.Mutex()

    suspend fun reprocessDemoReelsIfNeeded() {
        try {
            val allReels = dao.getAllReelsSync()
            val currentPrefs = preferencesRepo.preferencesFlow.first()
            val dispatcher = com.reelrecall.app.engine.DualEngineDispatcher(getApplication())
            val summarizer = dispatcher.resolveSummarizationEngine(currentPrefs)
            val transcriber = dispatcher.resolveTranscriptionEngine(currentPrefs)
            android.util.Log.d("ReelRecall", "Starting reprocess: ${allReels.size} reels, summarizer=${summarizer.name}, transcriber=${transcriber.name}")

            for (reel in allReels) {
                val hasOldBogusTranscript = reel.transcript?.contains("likes") == true ||
                                           reel.transcript?.contains("comments - ") == true ||
                                           reel.transcript?.contains("top three principles", ignoreCase = true) == true

                val isGenericOrDemo = reel.title?.contains("3 Rules for 120 FPS", ignoreCase = true) == true ||
                                     reel.title?.contains("Key Architectural Insights", ignoreCase = true) == true ||
                                     reel.title?.contains("Android Performance", ignoreCase = true) == true ||
                                     reel.title?.contains("Building High Performance", ignoreCase = true) == true ||
                                     reel.title?.contains("Comprehensive Resource List", ignoreCase = true) == true ||
                                     reel.title?.contains("Save Time And Money Tool", ignoreCase = true) == true ||
                                     reel.creatorHandle?.contains("&#") == true ||
                                     reel.transcript.isNullOrBlank()

                val needsOptimization = hasOldBogusTranscript || isGenericOrDemo

                android.util.Log.d("ReelRecall", "Reel ${reel.id} (${reel.shortcode}): needsOptimization=$needsOptimization, currentTitle='${reel.title}'")

                if (needsOptimization) {
                    val extractionRes = com.reelrecall.app.worker.InstagramExtractor.extract(
                        reel.canonicalUrl,
                        getApplication<android.app.Application>().cacheDir
                    )
                    val metadata = extractionRes.getOrNull()

                    val cleanCaption = metadata?.caption ?: reel.caption?.let {
                        val unescaped = android.text.Html.fromHtml(it, android.text.Html.FROM_HTML_MODE_LEGACY).toString().trim()
                        if (unescaped.contains(": \"")) unescaped.substringAfter(": \"").removeSuffix("\"").removeSuffix("\".").trim()
                        else if (unescaped.contains(" - ") && unescaped.contains(": ")) unescaped.substringAfter(": ").trim()
                        else unescaped
                    } ?: ""

                    val cleanHandle = metadata?.creatorHandle ?: reel.creatorHandle?.let {
                        android.text.Html.fromHtml(it, android.text.Html.FROM_HTML_MODE_LEGACY).toString().trim()
                    }

                    val parsedHandle = if (cleanHandle != null && cleanHandle.contains("@")) {
                        Regex("(@[a-zA-Z0-9._]+)").find(cleanHandle)?.value ?: cleanHandle
                    } else cleanHandle

                    // Step 2: Transcribe real audio if audioFile is present
                    val realTranscript = if (metadata?.audioFile != null && metadata.audioFile.exists() && metadata.audioFile.length() > 0) {
                        transcriber.transcribe(metadata.audioFile).getOrNull()?.trim()?.ifBlank { null }
                    } else null

                    android.util.Log.d("ReelRecall", "Reel ${reel.id} extracted transcript: ${realTranscript?.take(80) ?: "None"}")

                    // Step 3: Summarize using rich caption + real audio transcript
                    val summaryResult = summarizer.summarize(cleanCaption, realTranscript ?: "")
                    val summary = summaryResult.getOrNull()
                    android.util.Log.d("ReelRecall", "Reel ${reel.id} summary: success=${summaryResult.isSuccess}, title='${summary?.title}'")

                    if (summary != null) {
                        val updated = reel.copy(
                            title = summary.title,
                            oneLineSummary = summary.one_line_summary,
                            takeawaysJson = kotlinx.serialization.json.Json.encodeToString(summary.actionable_takeaways),
                            tagsJson = kotlinx.serialization.json.Json.encodeToString(summary.tags),
                            creatorHandle = parsedHandle ?: reel.creatorHandle,
                            caption = cleanCaption.ifBlank { reel.caption },
                            transcript = realTranscript,
                            thumbnailUrl = metadata?.thumbnailUrl ?: reel.thumbnailUrl,
                            status = IngestionStatus.COMPLETED,
                            engineUsed = "${transcriber.name} + ${summarizer.name}",
                            errorMessage = null
                        )
                        dao.update(updated)
                    } else {
                        val updated = reel.copy(
                            creatorHandle = parsedHandle ?: reel.creatorHandle,
                            caption = cleanCaption.ifBlank { reel.caption },
                            transcript = realTranscript,
                            thumbnailUrl = metadata?.thumbnailUrl ?: reel.thumbnailUrl,
                            status = IngestionStatus.FAILED,
                            errorMessage = "Sync failed: ${summaryResult.exceptionOrNull()?.message ?: "API unavailable"}"
                        )
                        dao.update(updated)
                    }
                    // Rate limit protection between batch items
                    kotlinx.coroutines.delay(2000)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("ReelRecall", "reprocessDemoReelsIfNeeded error", e)
        }
    }

    val isRetryingMap = kotlinx.coroutines.flow.MutableStateFlow<Map<Long, Boolean>>(emptyMap())

    fun reprocessReel(id: Long) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            if (isRetryingMap.value[id] == true) return@launch
            isRetryingMap.value = isRetryingMap.value + (id to true)
            reprocessMutex.withLock {
                try {
                    val reel = dao.getReelById(id).first() ?: return@withLock
                    val currentPrefs = preferencesRepo.preferencesFlow.first()
                    val dispatcher = com.reelrecall.app.engine.DualEngineDispatcher(getApplication())
                    val summarizer = dispatcher.resolveSummarizationEngine(currentPrefs)
                    val transcriber = dispatcher.resolveTranscriptionEngine(currentPrefs)

                    val extractionRes = com.reelrecall.app.worker.InstagramExtractor.extract(
                        reel.canonicalUrl,
                        getApplication<android.app.Application>().cacheDir
                    )
                    val metadata = extractionRes.getOrNull()

                    val cleanCaption = metadata?.caption ?: reel.caption?.let {
                        val unescaped = android.text.Html.fromHtml(it, android.text.Html.FROM_HTML_MODE_LEGACY).toString().trim()
                        if (unescaped.contains(": \"")) unescaped.substringAfter(": \"").removeSuffix("\"").removeSuffix("\".").trim()
                        else if (unescaped.contains(" - ") && unescaped.contains(": ")) unescaped.substringAfter(": ").trim()
                        else unescaped
                    } ?: ""

                    val cleanHandle = metadata?.creatorHandle ?: reel.creatorHandle?.let {
                        android.text.Html.fromHtml(it, android.text.Html.FROM_HTML_MODE_LEGACY).toString().trim()
                    }

                    val parsedHandle = if (cleanHandle != null && cleanHandle.contains("@")) {
                        Regex("(@[a-zA-Z0-9._]+)").find(cleanHandle)?.value ?: cleanHandle
                    } else cleanHandle

                    val realTranscript = if (!reel.transcript.isNullOrBlank()) {
                        reel.transcript
                    } else if (metadata?.audioFile != null && metadata.audioFile.exists() && metadata.audioFile.length() > 0) {
                        transcriber.transcribe(metadata.audioFile).getOrNull()?.trim()?.ifBlank { null }
                    } else {
                        null
                    }

                    val summaryResult = summarizer.summarize(cleanCaption, realTranscript ?: "")
                    val summary = summaryResult.getOrNull()

                    if (summary != null) {
                        val updated = reel.copy(
                            title = summary.title,
                            oneLineSummary = summary.one_line_summary,
                            takeawaysJson = kotlinx.serialization.json.Json.encodeToString(summary.actionable_takeaways),
                            tagsJson = kotlinx.serialization.json.Json.encodeToString(summary.tags),
                            creatorHandle = parsedHandle ?: reel.creatorHandle,
                            caption = cleanCaption.ifBlank { reel.caption },
                            transcript = realTranscript,
                            thumbnailUrl = metadata?.thumbnailUrl ?: reel.thumbnailUrl,
                            status = IngestionStatus.COMPLETED,
                            engineUsed = "${transcriber.name} + ${summarizer.name}",
                            errorMessage = null
                        )
                        dao.update(updated)
                    } else {
                        val err = summaryResult.exceptionOrNull()?.message ?: "Processing failed: Summarization returned no data"
                        val failedReel = reel.copy(
                            status = IngestionStatus.FAILED,
                            errorMessage = err
                        )
                        dao.update(failedReel)
                    }

                    // Strict delay between individual reel calls to respect Groq OTPM/RPM limits
                    kotlinx.coroutines.delay(1500)
                } catch (e: Exception) {
                    android.util.Log.e("ReelRecall", "reprocessReel $id error", e)
                    try {
                        val r = dao.getReelById(id).first()
                        if (r != null) {
                            dao.update(r.copy(status = IngestionStatus.FAILED, errorMessage = e.message ?: "Reprocessing error"))
                        }
                    } catch (_: Exception) {}
                } finally {
                    isRetryingMap.value = isRetryingMap.value - id
                }
            }
        }
    }

    fun updateReelNotes(id: Long, notes: String?) {
        viewModelScope.launch {
            dao.updateNotes(id, notes)
        }
    }

    fun updateReelTags(id: Long, tags: List<String>) {
        viewModelScope.launch {
            val json = kotlinx.serialization.json.Json.encodeToString(tags)
            dao.updateTags(id, json)
        }
    }
}

