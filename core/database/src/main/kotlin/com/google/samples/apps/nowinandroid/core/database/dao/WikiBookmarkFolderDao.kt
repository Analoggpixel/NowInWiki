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
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.google.samples.apps.nowinandroid.core.database.model.WikiBookmarkFolderEntity
import com.google.samples.apps.nowinandroid.core.database.model.WikiBookmarkFolderWithBookmarks
import kotlinx.coroutines.flow.Flow

@Dao
interface WikiBookmarkFolderDao {

    @Query(
        value = """
            SELECT * FROM wiki_bookmark_folders
            ORDER BY sort_order ASC, id ASC
            """,
    )
    fun observeAllFolders(): Flow<List<WikiBookmarkFolderEntity>>

    @Transaction
    @Query(
        value = """
            SELECT * FROM wiki_bookmark_folders
            ORDER BY sort_order ASC, id ASC
            """,
    )
    fun observeAllFoldersWithBookmarks(): Flow<List<WikiBookmarkFolderWithBookmarks>>

    @Transaction
    @Query(
        value = """
            SELECT * FROM wiki_bookmark_folders
            WHERE id = :folderId
            """,
    )
    fun observeFolderWithBookmarks(folderId: Long): Flow<WikiBookmarkFolderWithBookmarks?>

    @Query(value = "SELECT * FROM wiki_bookmark_folders WHERE id = :folderId")
    suspend fun getFolderById(folderId: Long): WikiBookmarkFolderEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertFolder(folder: WikiBookmarkFolderEntity): Long

    @Update
    suspend fun updateFolder(folder: WikiBookmarkFolderEntity)

    @Query(value = "DELETE FROM wiki_bookmark_folders WHERE id = :folderId")
    suspend fun deleteFolder(folderId: Long)

    @Query(value = "SELECT COUNT(*) FROM wiki_bookmark_folders")
    suspend fun folderCount(): Int

    @Query(
        value = """
            SELECT * FROM wiki_bookmark_folders
            ORDER BY sort_order ASC, id ASC
            LIMIT 1
            """,
    )
    suspend fun getFirstFolder(): WikiBookmarkFolderEntity?
}
