package org.eos.mynoti

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object NotificationRepository {
    private val _notifications = MutableStateFlow<List<CapturedNotification>>(emptyList())
    val notifications: StateFlow<List<CapturedNotification>> = _notifications.asStateFlow()

    fun add(notification: CapturedNotification) {
        _notifications.update { current ->
            listOf(notification) + current.filter { it.key != notification.key }
        }
    }

    fun markRemoved(key: String) {
        _notifications.update { current ->
            current.map { item ->
                if (item.key == key) item.copy(isRemoved = true) else item
            }
        }
    }

    fun clear() {
        _notifications.value = emptyList()
    }
}
