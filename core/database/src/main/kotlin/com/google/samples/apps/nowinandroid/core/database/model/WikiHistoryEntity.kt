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

package com.google.samples.apps.nowinandroid.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import kotlinx.datetime.Instant

/**
 * Recently viewed Wiki articles.
 *
 * [language] stores [com.google.samples.apps.nowinandroid.core.model.data.WikiLanguage.code].
 */
@Entity(
    tableName = "wiki_history",
    primaryKeys = ["title", "language"],
)
data class WikiHistoryEntity(
    val title: String,
    val language: String,
    @ColumnInfo(name = "viewed_at")
    val viewedAt: Instant,
    val description: String? = null,
    @ColumnInfo(name = "thumbnail_url")
    val thumbnailUrl: String? = null,
)
