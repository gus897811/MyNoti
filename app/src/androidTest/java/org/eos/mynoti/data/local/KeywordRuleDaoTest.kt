package org.eos.mynoti.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.eos.mynoti.data.local.dao.KeywordRuleDao
import org.eos.mynoti.data.local.entity.KeywordRuleEntity
import org.eos.mynoti.domain.model.KeywordRuleType
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
class KeywordRuleDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: KeywordRuleDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.keywordRuleDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndObserveImportant() = runTest {
        dao.insert(rule("과제", KeywordRuleType.IMPORTANT))
        dao.insert(rule("광고", KeywordRuleType.MUTE))

        val important = dao.observeByType(KeywordRuleType.IMPORTANT).first()
        assertEquals(1, important.size)
        assertEquals("과제", important.first().keyword)
    }

    @Test
    fun observeMute() = runTest {
        dao.insert(rule("spam", KeywordRuleType.MUTE))
        val muted = dao.observeByType(KeywordRuleType.MUTE).first()
        assertEquals(listOf("spam"), muted.map { it.keyword })
    }

    @Test
    fun deleteRemovesRule() = runTest {
        dao.insert(rule("마감", KeywordRuleType.IMPORTANT))
        val stored = dao.observeByType(KeywordRuleType.IMPORTANT).first().first()
        dao.delete(stored)
        assertTrue(dao.observeByType(KeywordRuleType.IMPORTANT).first().isEmpty())
        assertEquals(0, dao.count())
    }

    @Test
    fun duplicateKeywordSameTypeIsIgnored() = runTest {
        dao.insert(rule("시험", KeywordRuleType.IMPORTANT))
        dao.insert(rule("시험", KeywordRuleType.IMPORTANT))
        assertEquals(1, dao.observeByType(KeywordRuleType.IMPORTANT).first().size)
    }

    @Test
    fun sameKeywordDifferentTypeAllowed() = runTest {
        dao.insert(rule("과제", KeywordRuleType.IMPORTANT))
        dao.insert(rule("과제", KeywordRuleType.MUTE))
        assertEquals(2, dao.observeAll().first().size)
    }

    private fun rule(keyword: String, type: KeywordRuleType) = KeywordRuleEntity(
        keyword = keyword,
        ruleType = type,
        createdAt = LocalDateTime.of(2026, 8, 13, 10, 0)
    )
}
