package org.eos.mynoti

data class CapturedNotification(
    val key: String,
    val notificationKey: String,
    val packageName: String,
    val appLabel: String,
    val title: String,
    val text: String,
    val postedAtMillis: Long,
    val isRemoved: Boolean = false,
    val isKakaoTalk: Boolean = false,
)
