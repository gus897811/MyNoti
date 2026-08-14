package org.eos.mynoti.data.remote.dto

data class BatchResultItemDto(
    val localId: Long,
    val title: String? = null,
    val summary: String,
    val isImportant: Boolean,
    val type: String,
    val actionRequired: Boolean,
    val deadline: String?,
    val actions: List<String> = emptyList(),
    val isFallback: Boolean = false
)

data class BatchFailedItemDto(
    val localId: Long,
    val reason: String
)

data class BatchFilteredItemDto(
    val localId: Long
)

data class BatchAnalyzeResponse(
    val results: List<BatchResultItemDto>? = emptyList(),
    val failed: List<BatchFailedItemDto>? = emptyList(),
    val filtered: List<BatchFilteredItemDto>? = emptyList()
)
