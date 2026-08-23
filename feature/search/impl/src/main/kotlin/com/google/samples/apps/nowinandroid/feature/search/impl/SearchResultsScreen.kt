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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.google.samples.apps.nowinandroid.core.designsystem.component.DynamicAsyncImage
import com.google.samples.apps.nowinandroid.core.designsystem.theme.NiaTheme
import com.google.samples.apps.nowinandroid.core.model.data.WikiLanguage
import com.google.samples.apps.nowinandroid.core.model.data.WikiSuggestionItem
import com.google.samples.apps.nowinandroid.core.ui.DevicePreviews
import com.google.samples.apps.nowinandroid.core.ui.WikiBookmarkToggleButton
import com.google.samples.apps.nowinandroid.feature.search.api.R as searchR

@Composable
internal fun SearchResultsScreen(
    navQuery: String,
    navLanguage: WikiLanguage,
    onBackClick: () -> Unit,
    onSuggestionClick: (WikiSuggestionItem) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchResultsViewModel = hiltViewModel(),
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val bookmarkedKeys by viewModel.bookmarkedKeys.collectAsStateWithLifecycle()
    val pagingItems = viewModel.searchResults.collectAsLazyPagingItems()

    LaunchedEffect(navQuery, navLanguage) {
        viewModel.onSearch(navQuery, navLanguage)
    }

    SearchResultsScreen(
        searchQuery = searchQuery,
        selectedLanguage = selectedLanguage,
        pagingItems = pagingItems,
        bookmarkedKeys = bookmarkedKeys,
        onBackClick = onBackClick,
        onSuggestionClick = onSuggestionClick,
        onToggleBookmark = viewModel::toggleBookmark,
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
    pagingItems: LazyPagingItems<WikiSuggestionItem>,
    onBackClick: () -> Unit,
    onSuggestionClick: (WikiSuggestionItem) -> Unit,
    onSearch: (String, WikiLanguage) -> Unit,
    bookmarkedKeys: Set<String> = emptySet(),
    onToggleBookmark: (WikiSuggestionItem) -> Unit = {},
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

        val refreshState = pagingItems.loadState.refresh
        when {
            searchQuery.isBlank() -> Unit
            refreshState is LoadState.Loading && pagingItems.itemCount == 0 -> {
                SuggestionsLoading(modifier = Modifier.fillMaxWidth())
            }
            refreshState is LoadState.Error && pagingItems.itemCount == 0 -> {
                SuggestionsMessage(
                    message = stringResource(searchR.string.feature_search_api_search_results_load_error),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            refreshState is LoadState.NotLoading && pagingItems.itemCount == 0 -> {
                SuggestionsMessage(
                    message = stringResource(searchR.string.feature_search_api_search_results_empty),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            else -> {
                SearchResultsList(
                    pagingItems = pagingItems,
                    bookmarkedKeys = bookmarkedKeys,
                    onSuggestionClick = onSuggestionClick,
                    onToggleBookmark = onToggleBookmark,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                )
            }
        }

        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
    }
}

@Composable
private fun SearchResultsList(
    pagingItems: LazyPagingItems<WikiSuggestionItem>,
    bookmarkedKeys: Set<String>,
    onSuggestionClick: (WikiSuggestionItem) -> Unit,
    onToggleBookmark: (WikiSuggestionItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = stringResource(searchR.string.feature_search_api_search_results),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }
        items(
            count = pagingItems.itemCount,
            key = pagingItems.itemKey { item ->
                "${item.itemLanguage.code}/${item.listPosition}"
            },
        ) { index ->
            val item = pagingItems[index] ?: return@items
            SearchResultItem(
                item = item,
                isBookmarked = bookmarkKey(item.title, item.itemLanguage) in bookmarkedKeys,
                onClick = { onSuggestionClick(item) },
                onToggleBookmark = { onToggleBookmark(item) },
            )
        }

        when (pagingItems.loadState.append) {
            is LoadState.Loading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            is LoadState.Error -> {
                item {
                    SuggestionsMessage(
                        message = stringResource(searchR.string.feature_search_api_search_results_load_more_error),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            else -> Unit
        }
    }
}

@Composable
private fun SearchResultItem(
    item: WikiSuggestionItem,
    isBookmarked: Boolean,
    onClick: () -> Unit,
    onToggleBookmark: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val thumbnailUrl = item.thumbnailUrl
    val subtitle = item.excerpt ?: item.description

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

                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            WikiBookmarkToggleButton(
                isBookmarked = isBookmarked,
                onToggle = onToggleBookmark,
            )
        }
    }
}

@DevicePreviews
@Composable
private fun SearchResultsScreenPreview() {
    NiaTheme {
        // Preview uses empty paging stream; interactive preview is limited.
        Text(
            text = stringResource(searchR.string.feature_search_api_search_results_preview),
            modifier = Modifier.padding(16.dp),
        )
    }
}
