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
 * Adds temporary wiki search result cache tables for RemoteMediator-backed paging.
 */
val MIGRATION_18_19 = object : Migration(18, 19) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `wiki_search_results` (
                `search_query` TEXT NOT NULL,
                `language` TEXT NOT NULL,
                `position` INTEGER NOT NULL,
                `page_id` INTEGER NOT NULL,
                `title` TEXT NOT NULL,
                `description` TEXT,
                `thumbnail_url` TEXT,
                PRIMARY KEY(`search_query`, `language`, `position`)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `wiki_search_remote_keys` (
                `search_query` TEXT NOT NULL,
                `language` TEXT NOT NULL,
                `next_offset` INTEGER,
                PRIMARY KEY(`search_query`, `language`)
            )
            """.trimIndent(),
        )
    }
}
