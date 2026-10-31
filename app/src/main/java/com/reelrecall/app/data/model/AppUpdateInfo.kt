package com.reelrecall.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AppUpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val apkUrl: String,
    val releaseNotes: String = "",
    val forceUpdate: Boolean = false,
    val minSupportedVersionCode: Int = 1
)

