package org.eos.mynoti.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.eos.mynoti.data.local.converter.AnalysisStatusConverter
import org.eos.mynoti.data.local.converter.KeywordRuleTypeConverter
import org.eos.mynoti.data.local.converter.LocalDateTimeConverter
import org.eos.mynoti.data.local.converter.NotificationTypeConverter
import org.eos.mynoti.data.local.converter.StringListConverter
import org.eos.mynoti.data.local.dao.KeywordRuleDao
import org.eos.mynoti.data.local.dao.NotificationDao
import org.eos.mynoti.data.local.entity.KeywordRuleEntity
import org.eos.mynoti.data.local.entity.NotificationEntity

@Database(
    entities = [
        NotificationEntity::class,
        KeywordRuleEntity::class
    ],
    version = 3,
    exportSchema = true
)
@TypeConverters(
    LocalDateTimeConverter::class,
    NotificationTypeConverter::class,
    KeywordRuleTypeConverter::class,
    AnalysisStatusConverter::class,
    StringListConverter::class
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun notificationDao(): NotificationDao

    abstract fun keywordRuleDao(): KeywordRuleDao

    companion object {
        private const val DATABASE_NAME = "mynoti.db"

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                    .also { instance = it }
            }
        }
    }
}
