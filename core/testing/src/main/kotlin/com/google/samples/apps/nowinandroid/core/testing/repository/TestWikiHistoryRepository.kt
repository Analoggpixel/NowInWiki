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

package com.google.samples.apps.nowinandroid.core.testing.repository

import com.google.samples.apps.nowinandroid.core.data.repository.WikiHistoryRepository
import com.google.samples.apps.nowinandroid.core.model.data.WikiHistoryEntry
import com.google.samples.apps.nowinandroid.core.model.data.WikiLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Clock

class TestWikiHistoryRepository : WikiHistoryRepository {

    private val entries = MutableStateFlow<List<WikiHistoryEntry>>(emptyList())

    override fun getWikiHistory(limit: Int): Flow<List<WikiHistoryEntry>> =
        entries.map { list ->
            list.sortedByDescending { it.viewedAt }.take(limit)
        }

    override suspend fun insertOrReplaceWikiHistory(
        title: String,
        language: WikiLanguage,
        description: String?,
        thumbnailUrl: String?,
    ) {
        val trimmed = title.trim()
        if (trimmed.isEmpty()) return
        entries.update { current ->
            current.filterNot { it.title == trimmed && it.language == language } +
                WikiHistoryEntry(
                    title = trimmed,
                    language = language,
                    viewedAt = Clock.System.now(),
                    description = description,
                    thumbnailUrl = thumbnailUrl,
                )
        }
    }

    override suspend fun clearWikiHistory() {
        entries.value = emptyList()
    }
}
