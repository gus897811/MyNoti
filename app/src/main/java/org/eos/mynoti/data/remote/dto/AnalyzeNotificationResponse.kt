package org.eos.mynoti.data.remote.dto

data class AnalyzeNotificationResponse(
    val summary: String,
    val isImportant: Boolean,
    val type: String,
    val actionRequired: Boolean,
    val deadline: String?,
    val actions: List<String> = emptyList(),
    val isFallback: Boolean = false
)
