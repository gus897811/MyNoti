package org.eos.mynoti.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `manual_calendar_event` (
                `event_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `title` TEXT NOT NULL,
                `location` TEXT,
                `event_at` INTEGER NOT NULL,
                `type` TEXT NOT NULL,
                `is_important` INTEGER NOT NULL,
                `created_at` INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }
}
