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

import com.google.samples.apps.nowinandroid.core.model.data.WikiBookmark
import com.google.samples.apps.nowinandroid.core.model.data.WikiBookmarkFolder
import com.google.samples.apps.nowinandroid.core.model.data.WikiLanguage
import kotlinx.coroutines.flow.Flow

/**
 * Local Wiki bookmark folders and entries (Room).
 *
 * Supports multiple folders; bookmarks belong to a folder via [WikiBookmark.folderId].
 */
interface WikiBookmarkRepository {

    /** All folders with their bookmarks (newest bookmarks first inside each folder). */
    fun observeFolders(): Flow<List<WikiBookmarkFolder>>

    fun observeFolder(folderId: Long): Flow<WikiBookmarkFolder?>

    /** Whether [title]+[language] exists in any folder. */
    fun observeIsBookmarked(title: String, language: WikiLanguage): Flow<Boolean>

    fun observeIsBookmarkedInFolder(
        folderId: Long,
        title: String,
        language: WikiLanguage,
    ): Flow<Boolean>

    /** Creates a folder; returns new folder id. [sortOrder] defaults to append at end. */
    suspend fun createFolder(
        name: String,
        description: String? = null,
        sortOrder: Int? = null,
    ): Long

    suspend fun updateFolder(
        folderId: Long,
        name: String,
        description: String? = null,
        sortOrder: Int? = null,
    )

    /** Deletes the folder and all bookmarks inside it (FK CASCADE). */
    suspend fun deleteFolder(folderId: Long)

    /**
     * Adds or replaces a bookmark in [folderId].
     * [bookmark].folderId is overwritten by [folderId]; id=0 inserts a new row.
     */
    suspend fun upsertBookmark(folderId: Long, bookmark: WikiBookmark)

    suspend fun removeBookmark(
        folderId: Long,
        title: String,
        language: WikiLanguage,
    )

    suspend fun removeBookmarkById(bookmarkId: Long)

    suspend fun moveBookmark(bookmarkId: Long, toFolderId: Long)

    /**
     * Returns the first folder by [WikiBookmarkFolder.sortOrder], or creates a default folder
     * when none exist.
     */
    suspend fun getOrCreateDefaultFolder(): Long

    /** Removes every bookmark row matching [title] and [language] across all folders. */
    suspend fun removeBookmarkEverywhere(
        title: String,
        language: WikiLanguage,
    )

    /**
     * Adds to [folderId] (or the default folder) when not bookmarked; removes everywhere when
     * already bookmarked in any folder.
     */
    suspend fun toggleBookmark(
        title: String,
        language: WikiLanguage,
        folderId: Long? = null,
        description: String? = null,
        thumbnailUrl: String? = null,
    )
}

/**
 * Default folder name when seeding the first bookmark folder.
 * Keep in sync with `feature_bookmarks_api_default_folder_name`.
 */
const val DEFAULT_WIKI_BOOKMARK_FOLDER_NAME = "Saved"
