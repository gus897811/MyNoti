package org.eos.mynoti.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE notification ADD COLUMN summary TEXT")
        db.execSQL(
            "ALTER TABLE notification ADD COLUMN action_required INTEGER NOT NULL DEFAULT 0"
        )
        db.execSQL(
            "ALTER TABLE notification ADD COLUMN analysis_status TEXT NOT NULL DEFAULT 'PENDING'"
        )
        db.execSQL(
            "ALTER TABLE notification ADD COLUMN actions_json TEXT NOT NULL DEFAULT '[]'"
        )
    }
}
