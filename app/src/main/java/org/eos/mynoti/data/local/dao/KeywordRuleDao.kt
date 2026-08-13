package org.eos.mynoti.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.eos.mynoti.data.local.entity.KeywordRuleEntity
import org.eos.mynoti.domain.model.KeywordRuleType

@Dao
interface KeywordRuleDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(rule: KeywordRuleEntity): Long

    @Delete
    suspend fun delete(rule: KeywordRuleEntity)

    @Query(
        """
        SELECT * FROM keyword_rules
        WHERE rule_type = :ruleType
        ORDER BY created_at ASC
        """
    )
    fun observeByType(ruleType: KeywordRuleType): Flow<List<KeywordRuleEntity>>

    @Query(
        """
        SELECT * FROM keyword_rules
        ORDER BY created_at ASC
        """
    )
    fun observeAll(): Flow<List<KeywordRuleEntity>>

    @Query(
        """
        DELETE FROM keyword_rules
        WHERE keyword = :keyword AND rule_type = :ruleType
        """
    )
    suspend fun deleteByKeyword(keyword: String, ruleType: KeywordRuleType)

    @Query("SELECT COUNT(*) FROM keyword_rules")
    suspend fun count(): Int
}
