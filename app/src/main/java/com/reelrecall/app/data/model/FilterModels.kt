package com.reelrecall.app.data.model

enum class OrderBy(val label: String) {
    LAST_UPDATED("Last updated"),
    CREATED_AT("Created at")
}

enum class SortDirection(val label: String) {
    ASCENDING("Ascending"),
    DESCENDING("Descending")
}

enum class GroupBy(val label: String) {
    DAY("Day"),
    WEEK("Week"),
    MONTH("Month"),
    NONE("None")
}

enum class ViewMode(val label: String) {
    GRID("Grid"),
    LIST("List")
}
