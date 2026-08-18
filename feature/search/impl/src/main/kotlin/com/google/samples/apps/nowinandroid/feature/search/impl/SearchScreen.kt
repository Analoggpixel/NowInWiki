/*
 * Copyright 2023 The Android Open Source Project
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

package com.google.samples.apps.nowinandroid.feature.search.impl

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.samples.apps.nowinandroid.core.model.data.RecentSearchQuery
import com.google.samples.apps.nowinandroid.core.model.data.WikiLanguage
import com.google.samples.apps.nowinandroid.core.model.data.WikiSuggestionItem

@Composable
internal fun SearchScreen(
    onBackClick: () -> Unit,
    onSearchTriggered: (String, WikiLanguage) -> Unit,
    onSuggestionClick: (WikiSuggestionItem) -> Unit,
    modifier: Modifier = Modifier,
    searchSuggestionViewModel: SearchSuggestionViewModel = hiltViewModel(),
) {
    val searchQuery by searchSuggestionViewModel.query.collectAsStateWithLifecycle()
    val selectedLanguage by searchSuggestionViewModel.selectedLanguage.collectAsStateWithLifecycle()
    val suggestionUiState by searchSuggestionViewModel.uiState.collectAsStateWithLifecycle()
    val recentSearchQueriesUiState by
        searchSuggestionViewModel.recentSearchQueriesUiState.collectAsStateWithLifecycle()
    SearchScreen(
        modifier = modifier,
        searchQuery = searchQuery,
        selectedLanguage = selectedLanguage,
        searchSuggestionUiState = suggestionUiState,
        recentSearchQueriesUiState = recentSearchQueriesUiState,
        onSearchQueryChanged = searchSuggestionViewModel::onQueryChanged,
        onSearchTriggered = { query, language ->
            searchSuggestionViewModel.saveRecentSearch(query = query, language = language)
            onSearchTriggered(query, language)
        },
        onBackClick = onBackClick,
        onSuggestionClick = { item ->
            onSuggestionClick(item)
            searchSuggestionViewModel.onQueryChanged("")
        },
        onLanguageSelected = searchSuggestionViewModel::onLanguageSelected,
        onRecentSearchClick = { item ->
            searchSuggestionViewModel.onLanguageSelected(item.language)
            searchSuggestionViewModel.saveRecentSearch(
                query = item.query,
                language = item.language,
            )
            onSearchTriggered(item.query, item.language)
        },
        onClearRecentSearches = searchSuggestionViewModel::clearRecentSearches,
    )
}

@Composable
internal fun SearchScreen(
    modifier: Modifier = Modifier,
    searchQuery: String = "",
    selectedLanguage: WikiLanguage = WikiLanguage.CHINESE,
    searchSuggestionUiState: SearchSuggestionUiState = SearchSuggestionUiState.Idle,
    recentSearchQueriesUiState: RecentSearchQueriesUiState = RecentSearchQueriesUiState.Loading,
    onSearchQueryChanged: (String) -> Unit = {},
    onSearchTriggered: (String, WikiLanguage) -> Unit = { _, _ -> },
    onBackClick: () -> Unit = {},
    onSuggestionClick: (WikiSuggestionItem) -> Unit = {},
    onLanguageSelected: (WikiLanguage) -> Unit = {},
    onRecentSearchClick: (RecentSearchQuery) -> Unit = {},
    onClearRecentSearches: () -> Unit = {},
) {
    Column(modifier = modifier) {
        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.safeDrawing))
        SearchToolbar(
            searchQuery = searchQuery,
            selectedLanguage = selectedLanguage,
            onBackClick = onBackClick,
            onSearchQueryChanged = onSearchQueryChanged,
            onSearchTriggered = onSearchTriggered,
            onLanguageSelected = onLanguageSelected,
        )
        SearchSuggestionsScreen(
            uiState = searchSuggestionUiState,
            recentSearchQueriesUiState = recentSearchQueriesUiState,
            onSuggestionClick = { item -> onSuggestionClick(item) },
            onRecentSearchClick = onRecentSearchClick,
            onClearRecentSearches = onClearRecentSearches,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        )
        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
    }
}
