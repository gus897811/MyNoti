package org.eos.mynoti

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object NotificationRepository {
    private val _notifications = MutableStateFlow<List<CapturedNotification>>(emptyList())
    val notifications: StateFlow<List<CapturedNotification>> = _notifications.asStateFlow()

    fun addAll(items: List<CapturedNotification>) {
        if (items.isEmpty()) return
        _notifications.update { current ->
            var result = current
            items.forEach { item ->
                result = if (item.key == item.notificationKey) {
                    listOf(item) + result.filter { it.key != item.key }
                } else if (result.none { it.key == item.key }) {
                    listOf(item) + result
                } else {
                    result
                }
            }
            result
        }
    }

    fun markRemoved(notificationKey: String) {
        _notifications.update { current ->
            current.map { item ->
                if (item.notificationKey == notificationKey) item.copy(isRemoved = true) else item
            }
        }
    }

    fun clear() {
        _notifications.value = emptyList()
    }
}
