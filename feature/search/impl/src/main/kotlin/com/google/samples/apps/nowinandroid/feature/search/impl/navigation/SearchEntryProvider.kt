/*
 * Copyright 2025 The Android Open Source Project
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

package com.google.samples.apps.nowinandroid.feature.search.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.google.samples.apps.nowinandroid.core.navigation.Navigator
import com.google.samples.apps.nowinandroid.feature.search.api.navigation.SearchNavKey
import com.google.samples.apps.nowinandroid.feature.search.api.navigation.SearchResultsNavKey
import com.google.samples.apps.nowinandroid.feature.search.api.navigation.navigateToSearchResults
import com.google.samples.apps.nowinandroid.feature.search.impl.SearchResultsScreen
import com.google.samples.apps.nowinandroid.feature.search.impl.SearchScreen
import com.google.samples.apps.nowinandroid.feature.wikipage.api.navigation.navigateToWikiPage

// TODO: 后续可能把 SearchResultsNavKey 的 entry 拆成独立 searchResultsEntry，
// 再在 NiaApp 的 entryProvider 里单独注册，与搜索输入页解耦。
fun EntryProviderScope<NavKey>.searchEntry(navigator: Navigator) {
    entry<SearchNavKey> {
        SearchScreen(
            onBackClick = { navigator.goBack() },
            onSearchTriggered = { query, selectedLanguage ->
                if (query.isNotBlank()) {
                    navigator.navigateToSearchResults(query.trim(), selectedLanguage)
                }
            },
            onSuggestionClick = { item ->
                navigator.navigateToWikiPage(item.title, item.itemLanguage)
            },
        )
    }
    entry<SearchResultsNavKey> { key ->
        SearchResultsScreen(
            navQuery = key.query,
            navLanguage = key.selectedLanguage,
            onBackClick = { navigator.goBack() },
            onSuggestionClick = { item ->
                navigator.navigateToWikiPage(item.title, item.itemLanguage)
            },
        )
    }
}
