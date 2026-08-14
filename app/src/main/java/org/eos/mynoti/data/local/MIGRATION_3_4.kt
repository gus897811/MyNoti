package org.eos.mynoti.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `reminder` (
                `reminder_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `notification_id` INTEGER NOT NULL,
                `remind_at` INTEGER NOT NULL,
                `is_fired` INTEGER NOT NULL,
                `created_at` INTEGER NOT NULL,
                FOREIGN KEY(`notification_id`) REFERENCES `notification`(`notification_id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_reminder_notification_id` ON `reminder` (`notification_id`)"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_reminder_remind_at` ON `reminder` (`remind_at`)"
        )
    }
}
