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

import com.google.samples.apps.nowinandroid.core.data.model.asExternalFeedModel
import com.google.samples.apps.nowinandroid.core.model.data.WikiFeedItem
import com.google.samples.apps.nowinandroid.core.model.data.WikiLanguage
import com.google.samples.apps.nowinandroid.core.network.WikipediaNetworkDataSource
import javax.inject.Inject

/**
 * TODO：暂无独立推荐接口前，用 search/page + 固定种子词拉数据；正式 Feed API 就绪后替换实现即可。
 */
internal class DefaultWikiFeedRepository @Inject constructor(
    private val wikipediaNetworkDataSource: WikipediaNetworkDataSource,
) : WikiFeedRepository {

    override suspend fun getFeed(language: WikiLanguage): List<WikiFeedItem> =
        wikipediaNetworkDataSource
            .searchSuggestions(
                query = PLACEHOLDER_FEED_SEED_QUERY,
                language = language,
            )
            .asExternalFeedModel(language)

    private companion object {
        const val PLACEHOLDER_FEED_SEED_QUERY = "Android"
    }
}
