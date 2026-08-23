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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.samples.apps.nowinandroid.core.designsystem.component.DynamicAsyncImage
import com.google.samples.apps.nowinandroid.core.designsystem.component.NiaLoadingWheel
import com.google.samples.apps.nowinandroid.core.model.data.RecentSearchQuery
import com.google.samples.apps.nowinandroid.core.model.data.WikiSuggestionItem
import com.google.samples.apps.nowinandroid.feature.search.api.R as searchR

@Composable
internal fun SearchSuggestionsScreen(
    uiState: SearchSuggestionUiState,
    onSuggestionClick: (WikiSuggestionItem) -> Unit,
    modifier: Modifier = Modifier,
    recentSearchQueriesUiState: RecentSearchQueriesUiState = RecentSearchQueriesUiState.Loading,
    onRecentSearchClick: (RecentSearchQuery) -> Unit = {},
    onClearRecentSearches: () -> Unit = {},
) {
    when (uiState) {
        SearchSuggestionUiState.Idle -> {
            if (recentSearchQueriesUiState is RecentSearchQueriesUiState.Success) {
                RecentSearchesScreen(
                    recentQueries = recentSearchQueriesUiState.recentQueries,
                    onRecentSearchClick = onRecentSearchClick,
                    onClearRecentSearches = onClearRecentSearches,
                    modifier = modifier,
                )
            }
        }
        SearchSuggestionUiState.Loading -> {
            SuggestionsLoading(modifier = modifier)
        }
        SearchSuggestionUiState.Empty -> {
            SuggestionsMessage(
                message = stringResource(searchR.string.feature_search_api_suggestions_empty),
                modifier = modifier,
            )
        }
        SearchSuggestionUiState.Error -> {
            SuggestionsMessage(
                message = stringResource(searchR.string.feature_search_api_suggestions_error),
                modifier = modifier,
            )
        }
        is SearchSuggestionUiState.Success -> {
            SearchSuggestionList(
                items = uiState.items,
                onSuggestionClick = onSuggestionClick,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun SearchSuggestionList(
    items: List<WikiSuggestionItem>,
    onSuggestionClick: (WikiSuggestionItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
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
internal fun SearchSuggestionItem(
    item: WikiSuggestionItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val thumbnailUrl = item.thumbnailUrl
    val description = item.description

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
                    modifier = Modifier.size(72.dp),
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

                if (!description.isNullOrBlank()) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
internal fun SuggestionsLoading(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center,
    ) {
        NiaLoadingWheel(contentDesc = stringResource(searchR.string.feature_search_api_loading_suggestions))
    }
}

@Composable
internal fun SuggestionsMessage(
    message: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}
