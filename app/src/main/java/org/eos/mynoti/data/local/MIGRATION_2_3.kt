package org.eos.mynoti.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `notification_new` (
                `notification_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `app_name` TEXT NOT NULL,
                `app_package_name` TEXT NOT NULL,
                `title` TEXT,
                `content` TEXT,
                `received_at` INTEGER NOT NULL,
                `is_important` INTEGER NOT NULL,
                `type` TEXT NOT NULL,
                `created_at` INTEGER NOT NULL,
                `deadline` INTEGER,
                `summary` TEXT,
                `analysis_status` TEXT NOT NULL,
                `actions_json` TEXT NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO `notification_new` (
                `notification_id`, `app_name`, `app_package_name`, `title`, `content`,
                `received_at`, `is_important`, `type`, `created_at`, `deadline`,
                `summary`, `analysis_status`, `actions_json`
            )
            SELECT
                `notification_id`, `app_name`, `app_package_name`, `title`, `content`,
                `received_at`, `is_important`, `type`, `created_at`, `remind_at`,
                `summary`, `analysis_status`, `actions_json`
            FROM `notification`
            """.trimIndent()
        )
        db.execSQL("DROP TABLE `notification`")
        db.execSQL("ALTER TABLE `notification_new` RENAME TO `notification`")
    }
}
