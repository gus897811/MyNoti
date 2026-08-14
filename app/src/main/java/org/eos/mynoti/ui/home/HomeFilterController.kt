package org.eos.mynoti.ui.home

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.eos.mynoti.domain.model.NotificationFilter
import org.eos.mynoti.domain.model.NotificationType

enum class HomeFilterPreset {
    IMPORTANT,
    ASSIGNMENT
}

class HomeFilterController {
    private val _pending = MutableStateFlow<HomeFilterPreset?>(null)
    val pending: StateFlow<HomeFilterPreset?> = _pending.asStateFlow()

    fun request(preset: HomeFilterPreset) {
        _pending.value = preset
    }

    fun consume() {
        _pending.value = null
    }
}

fun HomeFilterPreset.toFilter(): NotificationFilter {
    return when (this) {
        HomeFilterPreset.IMPORTANT -> NotificationFilter(importantOnly = true)
        HomeFilterPreset.ASSIGNMENT -> NotificationFilter(
            selectedTypes = setOf(NotificationType.ASSIGNMENT)
        )
    }
}
