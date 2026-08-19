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

package com.google.samples.apps.nowinandroid.core.data.repository

import com.google.samples.apps.nowinandroid.core.model.data.WikiHistoryEntry
import com.google.samples.apps.nowinandroid.core.model.data.WikiLanguage
import kotlinx.coroutines.flow.Flow

/**
 * Data layer for recently viewed Wiki articles.
 */
interface WikiHistoryRepository {

    fun getWikiHistory(limit: Int): Flow<List<WikiHistoryEntry>>

    /**
     * Inserts or replaces a viewed article. Same [title] + [language] updates [viewedAt].
     */
    suspend fun insertOrReplaceWikiHistory(
        title: String,
        language: WikiLanguage,
        description: String? = null,
        thumbnailUrl: String? = null,
    )

    suspend fun clearWikiHistory()
}
