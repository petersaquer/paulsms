package com.example.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class AppUpdateInfo(
    val versionName: String = "1.0.0",
    val versionCode: Int = 1,
    val releaseDate: Long = System.currentTimeMillis(),
    val releaseDateFormatted: String = "",
    val apkDownloadUrl: String = "",
    val changelog: String = "Actualización con nuevas características y mejoras.",
    val isMandatory: Boolean = false,
    val publishedBy: String = "Administrador"
)
