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

package com.google.samples.apps.nowinandroid.feature.search.impl

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.samples.apps.nowinandroid.core.designsystem.component.DynamicAsyncImage
import com.google.samples.apps.nowinandroid.core.designsystem.theme.NiaTheme
import com.google.samples.apps.nowinandroid.core.model.data.WikiLanguage
import com.google.samples.apps.nowinandroid.core.model.data.WikiSuggestionItem
import com.google.samples.apps.nowinandroid.core.ui.DevicePreviews

@Composable
internal fun SearchResultsScreen(
    navQuery: String,
    selectedLanguage: WikiLanguage,
    onBackClick: () -> Unit,
    onSuggestionClick: (WikiSuggestionItem) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchResultsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()

    LaunchedEffect(navQuery) {
        viewModel.onSearch(navQuery, selectedLanguage)
    }

    SearchResultsScreen(
        searchQuery = searchQuery,
        selectedLanguage = selectedLanguage,
        uiState = uiState,
        onBackClick = onBackClick,
        onSuggestionClick = onSuggestionClick,
        onSearch = viewModel::onSearch,
        onSearchQueryChanged = viewModel::onQueryChanged,
        onLanguageSelected = viewModel::onLanguageSelected,
        modifier = modifier,
    )
}

@Composable
internal fun SearchResultsScreen(
    searchQuery: String,
    selectedLanguage: WikiLanguage,
    uiState: SearchResultsUiState,
    onBackClick: () -> Unit,
    onSuggestionClick: (WikiSuggestionItem) -> Unit,
    onSearch: (String, WikiLanguage) -> Unit,
    onSearchQueryChanged: (String) -> Unit = {},
    onLanguageSelected: (WikiLanguage) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.safeDrawing))
        SearchToolbar(
            searchQuery = searchQuery,
            selectedLanguage = selectedLanguage,
            onSearchQueryChanged = onSearchQueryChanged,
            onSearchTriggered = onSearch,
            onBackClick = onBackClick,
            onLanguageSelected = onLanguageSelected,
        )

        when (uiState) {
            SearchResultsUiState.Idle -> Unit
            SearchResultsUiState.Loading -> SuggestionsLoading(
                modifier = Modifier.fillMaxWidth(),
            )
            SearchResultsUiState.Empty -> SuggestionsMessage(
                message = "No results found.",
                modifier = Modifier.fillMaxWidth(),
            )
            SearchResultsUiState.Error -> SuggestionsMessage(
                message = "Unable to load search results.",
                modifier = Modifier.fillMaxWidth(),
            )
            is SearchResultsUiState.Success -> SearchResultsList(
                items = uiState.items,
                onSuggestionClick = onSuggestionClick,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
            )
        }

        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
    }
}

@Composable
private fun SearchResultsList(
    items: List<WikiSuggestionItem>,
    onSuggestionClick: (WikiSuggestionItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // TODO: 不必要的文字提醒
        item {
            Text(
                text = "Search results",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }
        items(
            items = items,
            key = { item -> item.id },
        ) { item ->
            SearchSuggestionItem(
                item = item,
                onClick = { onSuggestionClick(item) },
            )
        }
    }
}

@Composable
private fun SearchResultItem(
    item: WikiSuggestionItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val thumbnailUrl = item.thumbnailUrl
    val description = item.description
    val excerpt = item.excerpt

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            if (thumbnailUrl != null) {
                Log.d("WikiSuggestions", "thumbnailUrl=$thumbnailUrl")
                DynamicAsyncImage(
                    imageUrl = thumbnailUrl,
                    contentDescription = item.title,
                    modifier = Modifier.size(120.dp),
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                if (!excerpt.isNullOrBlank()) {
                    Text(
                        text = excerpt,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

//                if (!excerpt.isNullOrBlank()) {
//                    Text(
//                        text = excerpt,
//                        style = MaterialTheme.typography.bodySmall,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant,
//                        maxLines = 3,
//                        overflow = TextOverflow.Ellipsis,
//                    )
//                }
            }
        }
    }
}

@DevicePreviews
@Composable
private fun SearchResultsScreenPreview() {
    NiaTheme {
        SearchResultsScreen(
            searchQuery = "kotlin",
            selectedLanguage = WikiLanguage.ENGLISH,
            uiState = SearchResultsUiState.Success(
                items = listOf(
                    WikiSuggestionItem(
                        id = 1,
                        key = "Kotlin",
                        title = "Kotlin",
                        description = "General-purpose programming language",
                        thumbnailUrl = null,
                    ),
                ),
            ),
            onBackClick = {},
            onSuggestionClick = {},
            onSearch = {_, _ ->},
        )
    }
}
