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

package com.google.samples.apps.nowinandroid.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.google.samples.apps.nowinandroid.core.database.model.WikiHistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for [WikiHistoryEntity] access.
 */
@Dao
interface WikiHistoryDao {
    @Query(value = "SELECT * FROM wiki_history ORDER BY viewed_at DESC LIMIT :limit")
    fun getWikiHistoryEntities(limit: Int): Flow<List<WikiHistoryEntity>>

    @Upsert
    suspend fun insertOrReplaceWikiHistory(entry: WikiHistoryEntity)

    @Query(value = "DELETE FROM wiki_history")
    suspend fun clearWikiHistory()
}
