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
 * Drops legacy NiA news/topics tables (including FTS) that are no longer used by the wiki client.
 */
val MIGRATION_17_18 = object : Migration(17, 18) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS `news_resources_topics`")
        db.execSQL("DROP TABLE IF EXISTS `news_resources`")
        db.execSQL("DROP TABLE IF EXISTS `newsResourcesFts`")
        db.execSQL("DROP TABLE IF EXISTS `topics`")
        db.execSQL("DROP TABLE IF EXISTS `topicsFts`")
    }
}
