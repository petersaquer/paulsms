package com.example.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class AppUpdateInfo(
    val versionName: String = "1.0.0",
    val versionCode: Int = 1,
    val minSupportedVersion: Int = 1,
    val updateUrl: String = "",
    val apkDownloadUrl: String = "",
    val releaseNotes: String = "",
    val changelog: String = "Actualización con nuevas características y mejoras.",
    val isMandatory: Boolean = false,
    val isForceUpdate: Boolean = false,
    val publishedBy: String = "Administrador",
    val publishedAt: Long = 0L,
    val releaseDate: Long = System.currentTimeMillis(),
    val releaseDateFormatted: String = ""
)
