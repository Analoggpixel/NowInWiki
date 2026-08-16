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

package com.google.samples.apps.nowinandroid.core.data.model

import com.google.samples.apps.nowinandroid.core.database.model.WikiBookmarkEntity
import com.google.samples.apps.nowinandroid.core.database.model.WikiBookmarkFolderEntity
import com.google.samples.apps.nowinandroid.core.database.model.WikiBookmarkFolderWithBookmarks
import com.google.samples.apps.nowinandroid.core.model.data.WikiBookmark
import com.google.samples.apps.nowinandroid.core.model.data.WikiBookmarkFolder
import com.google.samples.apps.nowinandroid.core.model.data.WikiLanguage

fun WikiBookmarkEntity.asExternalModel(): WikiBookmark =
    WikiBookmark(
        id = id,
        folderId = folderId,
        title = title,
        language = WikiLanguage.fromCode(language),
        bookmarkedAt = bookmarkedAt,
        description = description,
        thumbnailUrl = thumbnailUrl,
    )

fun WikiBookmark.asEntity(): WikiBookmarkEntity =
    WikiBookmarkEntity(
        id = id,
        folderId = folderId,
        title = title,
        language = language.code,
        bookmarkedAt = bookmarkedAt,
        description = description,
        thumbnailUrl = thumbnailUrl,
    )

fun WikiBookmarkFolderEntity.asExternalModel(
    bookmarks: List<WikiBookmark> = emptyList(),
): WikiBookmarkFolder =
    WikiBookmarkFolder(
        id = id,
        name = name,
        description = description,
        sortOrder = sortOrder,
        bookmarks = bookmarks,
    )

fun WikiBookmarkFolderWithBookmarks.asExternalModel(): WikiBookmarkFolder =
    folder.asExternalModel(
        bookmarks = bookmarks
            .map(WikiBookmarkEntity::asExternalModel)
            .sortedByDescending(WikiBookmark::bookmarkedAt),
    )

fun WikiBookmarkFolder.asEntity(): WikiBookmarkFolderEntity =
    WikiBookmarkFolderEntity(
        id = id,
        name = name,
        description = description,
        sortOrder = sortOrder,
    )
