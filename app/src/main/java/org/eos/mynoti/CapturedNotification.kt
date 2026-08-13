package org.eos.mynoti

data class CapturedNotification(
    val key: String,
    val packageName: String,
    val title: String,
    val text: String,
    val postedAtMillis: Long,
    val isRemoved: Boolean = false,
)
