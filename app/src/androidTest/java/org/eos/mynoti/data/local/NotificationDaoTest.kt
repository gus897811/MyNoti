package org.eos.mynoti.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.eos.mynoti.data.local.dao.NotificationDao
import org.eos.mynoti.data.local.entity.NotificationEntity
import org.eos.mynoti.domain.model.AppPackages
import org.eos.mynoti.domain.model.NotificationType
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
class NotificationDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: NotificationDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.notificationDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndGetById() = runTest {
        val id = dao.insert(sample(title = "과제 제출"))
        val stored = dao.getById(id)
        assertNotNull(stored)
        assertEquals("과제 제출", stored?.title)
        assertEquals("org.eos.lms", stored?.appPackageName)
    }

    @Test
    fun observeAllOrdersByReceivedAtDesc() = runTest {
        val older = sample(title = "old", receivedAt = LocalDateTime.of(2026, 8, 1, 10, 0))
        val newer = sample(title = "new", receivedAt = LocalDateTime.of(2026, 8, 13, 10, 0))
        dao.insertAll(listOf(older, newer))

        val result = dao.observeAll().first()
        assertEquals(2, result.size)
        assertEquals("new", result.first().title)
    }

    @Test
    fun observeImportant() = runTest {
        dao.insertAll(
            listOf(
                sample(title = "일반", isImportant = false),
                sample(title = "중요", isImportant = true)
            )
        )
        val result = dao.observeImportant().first()
        assertEquals(1, result.size)
        assertEquals("중요", result.first().title)
    }

    @Test
    fun observeByPackageName() = runTest {
        dao.insertAll(
            listOf(
                sample(title = "lms", appPackageName = AppPackages.LEARNING_X),
                sample(title = "talk", appPackageName = "com.kakao.talk")
            )
        )
        val result = dao.observeByPackageName("com.kakao.talk").first()
        assertEquals(1, result.size)
        assertEquals("talk", result.first().title)
    }

    @Test
    fun updateImportance() = runTest {
        val id = dao.insert(sample(isImportant = false))
        dao.updateImportance(id, true)
        assertTrue(dao.getById(id)?.isImportant == true)
    }

    @Test
    fun deleteRemovesRow() = runTest {
        val entity = sample(title = "삭제")
        val id = dao.insert(entity)
        val stored = dao.getById(id)!!
        dao.delete(stored)
        assertNull(dao.getById(id))
        assertEquals(0, dao.count())
        assertFalse(dao.observeAll().first().any { it.notificationId == id })
    }

    private fun sample(
        title: String = "title",
        appPackageName: String = "org.eos.lms",
        receivedAt: LocalDateTime = LocalDateTime.of(2026, 8, 13, 10, 30),
        isImportant: Boolean = false
    ) = NotificationEntity(
        appName = "LearningX",
        appPackageName = appPackageName,
        title = title,
        content = "content",
        receivedAt = receivedAt,
        isImportant = isImportant,
        type = NotificationType.ASSIGNMENT,
        createdAt = receivedAt
    )
}
