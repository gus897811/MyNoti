package org.eos.mynoti.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `notification` ADD COLUMN `original_title` TEXT")
        db.execSQL("UPDATE `notification` SET `original_title` = `title`")
    }
}
