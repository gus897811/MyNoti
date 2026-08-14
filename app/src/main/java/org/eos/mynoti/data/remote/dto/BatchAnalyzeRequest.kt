package org.eos.mynoti.data.remote.dto

data class BatchNotificationItemDto(
    val localId: Long,
    val appName: String,
    val packageName: String,
    val title: String?,
    val content: String?,
    val receivedAt: String
)

data class BatchAnalyzeRequest(
    val notifications: List<BatchNotificationItemDto>
)
