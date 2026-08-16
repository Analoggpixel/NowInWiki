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
import com.google.samples.apps.nowinandroid.core.database.model.WikiBookmarkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WikiBookmarkDao {

    @Query(
        value = """
            SELECT * FROM wiki_bookmarks
            WHERE folder_id = :folderId
            ORDER BY bookmarked_at DESC
            """,
    )
    fun observeBookmarksInFolder(folderId: Long): Flow<List<WikiBookmarkEntity>>

    @Query(
        value = """
            SELECT * FROM wiki_bookmarks
            ORDER BY bookmarked_at DESC
            """,
    )
    fun observeAllBookmarks(): Flow<List<WikiBookmarkEntity>>

    @Query(
        value = """
            SELECT EXISTS(
                SELECT 1 FROM wiki_bookmarks
                WHERE language = :language AND title = :title
                LIMIT 1
            )
            """,
    )
    fun observeIsBookmarked(title: String, language: String): Flow<Boolean>

    @Query(
        value = """
            SELECT EXISTS(
                SELECT 1 FROM wiki_bookmarks
                WHERE folder_id = :folderId AND language = :language AND title = :title
                LIMIT 1
            )
            """,
    )
    fun observeIsBookmarkedInFolder(
        folderId: Long,
        title: String,
        language: String,
    ): Flow<Boolean>

    @Upsert
    suspend fun upsertBookmark(bookmark: WikiBookmarkEntity): Long

    @Query(
        value = """
            DELETE FROM wiki_bookmarks
            WHERE folder_id = :folderId AND language = :language AND title = :title
            """,
    )
    suspend fun deleteBookmark(folderId: Long, title: String, language: String)

    @Query(value = "DELETE FROM wiki_bookmarks WHERE id = :bookmarkId")
    suspend fun deleteBookmarkById(bookmarkId: Long)

    @Query(
        value = """
            DELETE FROM wiki_bookmarks
            WHERE language = :language AND title = :title
            """,
    )
    suspend fun deleteBookmarksEverywhere(title: String, language: String)

    @Query(
        value = """
            UPDATE wiki_bookmarks
            SET folder_id = :toFolderId
            WHERE id = :bookmarkId
            """,
    )
    suspend fun moveBookmark(bookmarkId: Long, toFolderId: Long)
}
