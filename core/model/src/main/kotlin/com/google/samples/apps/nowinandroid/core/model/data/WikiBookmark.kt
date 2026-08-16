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

package com.google.samples.apps.nowinandroid.core.model.data

/**
 * 本地 Wiki 收藏夹（可有多个）。
 *
 * [bookmarks] 为读模型聚合，持久化时夹与条目分表存储，不嵌套写入。
 */
data class WikiBookmarkFolder(
    val id: Long,
    val name: String,
    val description: String? = null,
    /** 夹列表排序，越小越靠前。 */
    val sortOrder: Int = 0,
    val bookmarks: List<WikiBookmark> = emptyList(),
)

/**
 * 本地收藏的一条 Wiki 条目引用（不是整页 HTML）。
 *
 * 身份由 [folderId] + [language] + [title] 共同决定：
 * 同一词条可存在于不同收藏夹；同夹内同语言同标题不重复。
 */
data class WikiBookmark(
    val id: Long = 0L,
    val folderId: Long,
    val title: String,
    val language: WikiLanguage,
    /** 收藏时间，epoch millis；列表一般按此倒序。 */
    val bookmarkedAt: Long,
    val description: String? = null,
    val thumbnailUrl: String? = null,
)
