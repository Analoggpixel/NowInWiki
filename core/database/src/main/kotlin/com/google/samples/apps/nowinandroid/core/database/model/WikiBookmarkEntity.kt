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

package com.google.samples.apps.nowinandroid.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Wiki 收藏条目表。通过 [folderId] 归属某个收藏夹；删夹时级联删除条目。
 */
@Entity(
    tableName = "wiki_bookmarks",
    foreignKeys = [
        ForeignKey(
            entity = WikiBookmarkFolderEntity::class,
            parentColumns = ["id"],
            childColumns = ["folder_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["folder_id"]),
        Index(value = ["language", "title"]),
        Index(
            value = ["folder_id", "language", "title"],
            unique = true,
        ),
    ],
)
data class WikiBookmarkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "folder_id")
    val folderId: Long,
    val title: String,
    /** [com.google.samples.apps.nowinandroid.core.model.data.WikiLanguage.code] */
    val language: String,
    @ColumnInfo(name = "bookmarked_at")
    val bookmarkedAt: Long,
    val description: String? = null,
    @ColumnInfo(name = "thumbnail_url")
    val thumbnailUrl: String? = null,
)
