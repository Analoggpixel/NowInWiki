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

import com.google.samples.apps.nowinandroid.core.model.data.WikiFeedItem
import com.google.samples.apps.nowinandroid.core.model.data.WikiLanguage
import com.google.samples.apps.nowinandroid.core.network.model.NetworkWikiSuggestionItem
import com.google.samples.apps.nowinandroid.core.network.model.NetworkWikiSuggestionsResponse

/**
 * 将网络搜索页条目映射为 For You 卡片。
 *
 * 不能再命名为 [asExternalModel]：同 receiver 上已有映射到 [com.google.samples.apps.nowinandroid.core.model.data.WikiSuggestionItem]
 * 的同名扩展，Kotlin 无法仅靠返回类型重载。
 *
 * 当前暂复用 search/page 的 network DTO；后续若有独立推荐接口，再改为对应 Network 类型。
 * TODO：后期可能要分离，目前用同一结构的 asExternalFeedModel 作为命名
 */
fun NetworkWikiSuggestionItem.asExternalFeedModel(itemLanguage: WikiLanguage): WikiFeedItem =
    WikiFeedItem(
        id = id,
        key = key,
        title = title,
        description = description,
        excerpt = excerpt,
        thumbnailUrl = thumbnail?.url?.toAbsoluteWikiUrl()?.toHighResolution(),
        thumbnailWidth = thumbnail?.width,
        thumbnailHeight = thumbnail?.height,
        itemLanguage = itemLanguage,
    )

fun NetworkWikiSuggestionsResponse.asExternalFeedModel(itemLanguage: WikiLanguage): List<WikiFeedItem> =
    pages.map({page -> page.asExternalFeedModel(itemLanguage)})

private fun String.toAbsoluteWikiUrl(): String =
    when {
        startsWith("//") -> "https:$this"
        else -> this
    }

private fun String.toHighResolution(size: Int = 250): String =
    replace(
        Regex("/\\d+px-"),
        "/${size}px-",
    )
