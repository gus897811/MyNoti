package org.eos.mynoti.domain.model

enum class ThemePreference {
    SYSTEM,
    LIGHT,
    DARK
}

fun ThemePreference.isDark(systemInDark: Boolean): Boolean = when (this) {
    ThemePreference.SYSTEM -> systemInDark
    ThemePreference.LIGHT -> false
    ThemePreference.DARK -> true
}

fun parseThemePreference(raw: String?): ThemePreference {
    return raw?.let { runCatching { ThemePreference.valueOf(it) }.getOrNull() }
        ?: ThemePreference.SYSTEM
}
