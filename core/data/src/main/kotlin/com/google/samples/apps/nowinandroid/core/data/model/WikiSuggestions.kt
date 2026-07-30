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

import com.google.samples.apps.nowinandroid.core.model.data.WikiSuggestionItem
import com.google.samples.apps.nowinandroid.core.model.data.WikiSuggestionsResult
import com.google.samples.apps.nowinandroid.core.network.model.NetworkWikiSuggestionItem
import com.google.samples.apps.nowinandroid.core.network.model.NetworkWikiSuggestionsResponse

fun NetworkWikiSuggestionItem.asExternalModel(): WikiSuggestionItem =
    WikiSuggestionItem(
        id = id,
        key = key,
        title = title,
        description = description,
        excerpt = excerpt,
        thumbnailUrl = thumbnail?.url?.toAbsoluteWikiUrl(),
    )

fun NetworkWikiSuggestionsResponse.asExternalModel(): WikiSuggestionsResult =
    WikiSuggestionsResult(
        items = pages.map(NetworkWikiSuggestionItem::asExternalModel),
    )

// 把 "//..." 风格的相对路径转成 "https://..." 这样的绝对路径
private fun String.toAbsoluteWikiUrl(): String =
    when {
        startsWith("//") -> "https:$this"
        else -> this
    }
