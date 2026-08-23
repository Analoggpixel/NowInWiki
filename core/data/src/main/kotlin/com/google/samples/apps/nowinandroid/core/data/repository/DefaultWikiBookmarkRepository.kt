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
import com.google.samples.apps.nowinandroid.core.database.dao.WikiBookmarkDao
import com.google.samples.apps.nowinandroid.core.database.dao.WikiBookmarkFolderDao
import com.google.samples.apps.nowinandroid.core.database.model.WikiBookmarkFolderEntity
import com.google.samples.apps.nowinandroid.core.model.data.WikiBookmark
import com.google.samples.apps.nowinandroid.core.model.data.WikiBookmarkFolder
import com.google.samples.apps.nowinandroid.core.model.data.WikiLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class DefaultWikiBookmarkRepository @Inject constructor(
    private val folderDao: WikiBookmarkFolderDao,
    private val bookmarkDao: WikiBookmarkDao,
) : WikiBookmarkRepository {

    override fun observeFolders(): Flow<List<WikiBookmarkFolder>> =
        folderDao.observeAllFoldersWithBookmarks().map { folders ->
            folders.map { it.asExternalModel() }
        }

    override fun observeFolder(folderId: Long): Flow<WikiBookmarkFolder?> =
        folderDao.observeFolderWithBookmarks(folderId).map { it?.asExternalModel() }

    override fun observeIsBookmarked(title: String, language: WikiLanguage): Flow<Boolean> =
        bookmarkDao.observeIsBookmarked(title = title, language = language.code)

    override fun observeIsBookmarkedInFolder(
        folderId: Long,
        title: String,
        language: WikiLanguage,
    ): Flow<Boolean> =
        bookmarkDao.observeIsBookmarkedInFolder(
            folderId = folderId,
            title = title,
            language = language.code,
        )

    override suspend fun createFolder(
        name: String,
        description: String?,
        sortOrder: Int?,
    ): Long {
        val resolvedSortOrder = sortOrder ?: folderDao.folderCount()
        return folderDao.insertFolder(
            WikiBookmarkFolderEntity(
                name = name.trim(),
                description = description,
                sortOrder = resolvedSortOrder,
            ),
        )
    }

    override suspend fun updateFolder(
        folderId: Long,
        name: String,
        description: String?,
        sortOrder: Int?,
    ) {
        val existing = folderDao.getFolderById(folderId) ?: return
        folderDao.updateFolder(
            existing.copy(
                name = name.trim(),
                description = description,
                sortOrder = sortOrder ?: existing.sortOrder,
            ),
        )
    }

    override suspend fun deleteFolder(folderId: Long) {
        folderDao.deleteFolder(folderId)
    }

    override suspend fun upsertBookmark(folderId: Long, bookmark: WikiBookmark) {
        bookmarkDao.upsertBookmark(
            bookmark.copy(folderId = folderId).asEntity(),
        )
    }

    override suspend fun removeBookmark(
        folderId: Long,
        title: String,
        language: WikiLanguage,
    ) {
        bookmarkDao.deleteBookmark(
            folderId = folderId,
            title = title,
            language = language.code,
        )
    }

    override suspend fun removeBookmarkById(bookmarkId: Long) {
        bookmarkDao.deleteBookmarkById(bookmarkId)
    }

    override suspend fun moveBookmark(bookmarkId: Long, toFolderId: Long) {
        bookmarkDao.moveBookmark(bookmarkId = bookmarkId, toFolderId = toFolderId)
    }

    override suspend fun getOrCreateDefaultFolder(): Long =
        folderDao.getFirstFolder()?.id
            ?: createFolder(name = DEFAULT_WIKI_BOOKMARK_FOLDER_NAME)

    override suspend fun removeBookmarkEverywhere(
        title: String,
        language: WikiLanguage,
    ) {
        bookmarkDao.deleteBookmarksEverywhere(
            title = title.trim(),
            language = language.code,
        )
    }

    override suspend fun toggleBookmark(
        title: String,
        language: WikiLanguage,
        folderId: Long?,
        description: String?,
        thumbnailUrl: String?,
    ) {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isEmpty()) return

        val isBookmarked = bookmarkDao
            .observeIsBookmarked(title = trimmedTitle, language = language.code)
            .first()
        if (isBookmarked) {
            removeBookmarkEverywhere(title = trimmedTitle, language = language)
            return
        }

        val targetFolderId = folderId ?: getOrCreateDefaultFolder()
        upsertBookmark(
            folderId = targetFolderId,
            bookmark = WikiBookmark(
                folderId = targetFolderId,
                title = trimmedTitle,
                language = language,
                bookmarkedAt = System.currentTimeMillis(),
                description = description,
                thumbnailUrl = thumbnailUrl,
            ),
        )
    }
}
