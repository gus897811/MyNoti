package org.eos.mynoti.domain.model

data class TargetApp(
    val packageName: String,
    val name: String,
    val enabled: Boolean = true
)

data class AppSettings(
    val targetApps: List<TargetApp>,
    val highlightKeywords: List<String>,
    val muteKeywords: List<String>,
    val themePreference: ThemePreference = ThemePreference.SYSTEM
) {
    val enabledPackageNames: Set<String>
        get() = targetApps.filter { it.enabled }.map { it.packageName }.toSet()

    companion object {
        val defaultTargetApps = listOf(
            TargetApp(AppPackages.LEARNING_X, "LearningX Student", enabled = true),
            TargetApp(AppPackages.HEY_YOUNG, "헤이영캠퍼스", enabled = true),
            TargetApp(AppPackages.KAKAOTALK, "KakaoTalk", enabled = true),
            TargetApp(AppPackages.SHINHAN_CARD, "신한카드", enabled = true),
            TargetApp(AppPackages.KAKAOBANK, "카카오뱅크", enabled = true)
        )

        val defaultHighlightKeywords = listOf("과제", "시험", "마감", "긴급", "장학금")
        val defaultMuteKeywords = listOf("광고", "세일", "프로모션", "spam")

        fun defaults() = AppSettings(
            targetApps = defaultTargetApps,
            highlightKeywords = defaultHighlightKeywords,
            muteKeywords = defaultMuteKeywords,
            themePreference = ThemePreference.SYSTEM
        )
    }
}
