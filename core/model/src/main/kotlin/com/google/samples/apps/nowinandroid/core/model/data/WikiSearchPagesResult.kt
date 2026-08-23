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
 * One hit from Action API paginated search (`generator=search`).
 */
data class WikiSearchPageItem(
    val pageId: Long,
    val title: String,
    val description: String? = null,
    val thumbnailUrl: String? = null,
    val language: WikiLanguage,
    /** 0-based index within the cached search result list. */
    val position: Int = 0,
)

/**
 * One page of Action API search results, ready for Paging / infinite scroll.
 *
 * @property nextOffset MediaWiki `gsroffset` for the next request, or null when exhausted.
 */
data class WikiSearchPagesResult(
    val items: List<WikiSearchPageItem> = emptyList(),
    val nextOffset: Int? = null,
)

fun WikiSearchPageItem.asSuggestionItem(): WikiSuggestionItem =
    WikiSuggestionItem(
        id = pageId,
        key = title.trim().replace(' ', '_'),
        title = title,
        description = description,
        excerpt = null,
        thumbnailUrl = thumbnailUrl,
        itemLanguage = language,
        listPosition = position,
    )
