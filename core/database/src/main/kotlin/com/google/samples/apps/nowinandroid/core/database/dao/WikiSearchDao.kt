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

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.google.samples.apps.nowinandroid.core.database.model.WikiSearchRemoteKeysEntity
import com.google.samples.apps.nowinandroid.core.database.model.WikiSearchResultEntity

/**
 * DAO for temporary wiki search result cache used by [androidx.paging.RemoteMediator].
 */
@Dao
interface WikiSearchDao {

    @Query(
        """
        SELECT * FROM wiki_search_results
        WHERE search_query = :searchQuery AND language = :language
        ORDER BY position ASC
        """,
    )
    fun pagingSource(searchQuery: String, language: String): PagingSource<Int, WikiSearchResultEntity>

    @Query(
        """
        SELECT * FROM wiki_search_remote_keys
        WHERE search_query = :searchQuery AND language = :language
        """,
    )
    suspend fun remoteKeys(searchQuery: String, language: String): WikiSearchRemoteKeysEntity?

    @Query(
        """
        SELECT MAX(position) FROM wiki_search_results
        WHERE search_query = :searchQuery AND language = :language
        """,
    )
    suspend fun lastPosition(searchQuery: String, language: String): Int?

    @Query(
        """
        SELECT page_id FROM wiki_search_results
        WHERE search_query = :searchQuery AND language = :language
        """,
    )
    suspend fun pageIds(searchQuery: String, language: String): List<Long>

    @Upsert
    suspend fun upsertResults(entities: List<WikiSearchResultEntity>)

    @Upsert
    suspend fun upsertRemoteKeys(entity: WikiSearchRemoteKeysEntity)

    @Query(
        """
        DELETE FROM wiki_search_results
        WHERE search_query = :searchQuery AND language = :language
        """,
    )
    suspend fun clearResults(searchQuery: String, language: String)

    @Query(
        """
        DELETE FROM wiki_search_remote_keys
        WHERE search_query = :searchQuery AND language = :language
        """,
    )
    suspend fun clearRemoteKeys(searchQuery: String, language: String)

    @Query("DELETE FROM wiki_search_results")
    suspend fun clearAllResults()

    @Query("DELETE FROM wiki_search_remote_keys")
    suspend fun clearAllRemoteKeys()

    @Transaction
    suspend fun replaceSearchPage(
        searchQuery: String,
        language: String,
        clearExisting: Boolean,
        clearAllCaches: Boolean,
        results: List<WikiSearchResultEntity>,
        remoteKeys: WikiSearchRemoteKeysEntity,
    ) {
        when {
            clearAllCaches -> {
                clearAllResults()
                clearAllRemoteKeys()
            }
            clearExisting -> {
                clearResults(searchQuery, language)
                clearRemoteKeys(searchQuery, language)
            }
        }
        if (results.isNotEmpty()) {
            upsertResults(results)
        }
        upsertRemoteKeys(remoteKeys)
    }
}
