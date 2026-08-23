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

/**
 * Cached Action API search hit for offline / beyond-memory paging.
 *
 * [language] stores [com.google.samples.apps.nowinandroid.core.model.data.WikiLanguage.code].
 * [position] is the DB-side list index (0-based) within a (searchQuery, language) result set.
 * It is distinct from the network request `gsroffset` used for API pagination.
 */
@Entity(
    tableName = "wiki_search_results",
    primaryKeys = ["search_query", "language", "position"],
)
data class WikiSearchResultEntity(
    @ColumnInfo(name = "search_query")
    val searchQuery: String,
    val language: String,
    val position: Int,
    @ColumnInfo(name = "page_id")
    val pageId: Long,
    val title: String,
    val description: String? = null,
    @ColumnInfo(name = "thumbnail_url")
    val thumbnailUrl: String? = null,
)

/**
 * Remote pagination cursor for a cached search.
 *
 * [nextOffset] is MediaWiki `gsroffset` for the next APPEND; null means no further pages.
 */
@Entity(
    tableName = "wiki_search_remote_keys",
    primaryKeys = ["search_query", "language"],
)
data class WikiSearchRemoteKeysEntity(
    @ColumnInfo(name = "search_query")
    val searchQuery: String,
    val language: String,
    @ColumnInfo(name = "next_offset")
    val nextOffset: Int? = null,
)
