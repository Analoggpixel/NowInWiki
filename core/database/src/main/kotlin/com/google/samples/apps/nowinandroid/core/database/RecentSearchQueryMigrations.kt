/*
 * Copyright 2026 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.samples.apps.nowinandroid.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Adds [language] to [recentSearchQueries] and switches to composite primary key
 * `(query, language)`. Existing rows default to `en`.
 */
val MIGRATION_15_16 = object : Migration(15, 16) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `recentSearchQueries_new` (
                `query` TEXT NOT NULL,
                `language` TEXT NOT NULL,
                `queriedDate` INTEGER NOT NULL,
                PRIMARY KEY(`query`, `language`)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO `recentSearchQueries_new` (`query`, `language`, `queriedDate`)
            SELECT `query`, 'en', `queriedDate` FROM `recentSearchQueries`
            """.trimIndent(),
        )
        db.execSQL("DROP TABLE `recentSearchQueries`")
        db.execSQL("ALTER TABLE `recentSearchQueries_new` RENAME TO `recentSearchQueries`")
    }
}
