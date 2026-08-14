package org.eos.mynoti.data.remote.dto

data class AnalyzeNotificationRequest(
    val appName: String,
    val packageName: String,
    val title: String?,
    val content: String?,
    val receivedAt: String
)
