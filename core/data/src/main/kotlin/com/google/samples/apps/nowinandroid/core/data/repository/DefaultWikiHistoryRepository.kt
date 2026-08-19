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

import com.google.samples.apps.nowinandroid.core.data.model.asEntity
import com.google.samples.apps.nowinandroid.core.data.model.asExternalModel
import com.google.samples.apps.nowinandroid.core.database.dao.WikiHistoryDao
import com.google.samples.apps.nowinandroid.core.model.data.WikiHistoryEntry
import com.google.samples.apps.nowinandroid.core.model.data.WikiLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import javax.inject.Inject

internal class DefaultWikiHistoryRepository @Inject constructor(
    private val wikiHistoryDao: WikiHistoryDao,
) : WikiHistoryRepository {

    override fun getWikiHistory(limit: Int): Flow<List<WikiHistoryEntry>> =
        wikiHistoryDao.getWikiHistoryEntities(limit).map { entries ->
            entries.map { it.asExternalModel() }
        }

    override suspend fun insertOrReplaceWikiHistory(
        title: String,
        language: WikiLanguage,
        description: String?,
        thumbnailUrl: String?,
    ) {
        val trimmed = title.trim()
        if (trimmed.isEmpty()) return
        wikiHistoryDao.insertOrReplaceWikiHistory(
            WikiHistoryEntry(
                title = trimmed,
                language = language,
                viewedAt = Clock.System.now(),
                description = description,
                thumbnailUrl = thumbnailUrl,
            ).asEntity(),
        )
    }

    override suspend fun clearWikiHistory() = wikiHistoryDao.clearWikiHistory()
}
