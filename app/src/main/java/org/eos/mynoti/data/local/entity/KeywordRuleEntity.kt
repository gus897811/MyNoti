package org.eos.mynoti.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import org.eos.mynoti.domain.model.KeywordRuleType
import java.time.LocalDateTime

/**
 * 사용자 Highlight/Mute 키워드.
 *
 * DB Spec의 `mapped_type`은 LLM type 대체가 아니라 규칙 종류(IMPORTANT/MUTE)다.
 * 같은 키워드를 IMPORTANT와 MUTE에 동시에 둘 수 있으나, 유형이 같으면 UNIQUE로 막는다.
 */
@Entity(
    tableName = "keyword_rules",
    indices = [
        Index(value = ["keyword", "rule_type"], unique = true)
    ]
)
data class KeywordRuleEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "rule_id")
    val ruleId: Long = 0,

    @ColumnInfo(name = "keyword")
    val keyword: String,

    @ColumnInfo(name = "rule_type")
    val ruleType: KeywordRuleType,

    @ColumnInfo(name = "created_at")
    val createdAt: LocalDateTime
)
