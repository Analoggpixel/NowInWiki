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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.samples.apps.nowinandroid.core.designsystem.icon.NiaIcons
import com.google.samples.apps.nowinandroid.core.model.data.RecentSearchQuery
import com.google.samples.apps.nowinandroid.feature.search.api.R as searchR

private val ChipHorizontalPadding = 12.dp
private val ChipCharWidth = 14.dp
private val ChipItemSpacing = 8.dp

@Composable
internal fun RecentSearchesScreen(
    recentQueries: List<RecentSearchQuery>,
    onRecentSearchClick: (RecentSearchQuery) -> Unit,
    onClearRecentSearches: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (recentQueries.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("search:recent-searches"),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(searchR.string.feature_search_api_recent_searches),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f),
            )
            IconButton(
                onClick = onClearRecentSearches,
                modifier = Modifier.testTag("search:clear-recent-searches"),
            ) {
                Icon(
                    imageVector = NiaIcons.Close,
                    contentDescription = stringResource(
                        searchR.string.feature_search_api_clear_recent_searches_content_desc,
                    ),
                )
            }
        }
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val maxWidthDp = maxWidth.value * 0.6f
            val layout = remember(recentQueries, maxWidthDp) {
                takeRecentSearchesForTwoRows(
                    queries = recentQueries,
                    maxWidthDp = maxWidthDp,
                    horizontalPaddingDp = ChipHorizontalPadding.value,
                    charWidthDp = ChipCharWidth.value,
                    itemSpacingDp = ChipItemSpacing.value,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                RecentSearchChipRow(
                    queries = layout.firstRow,
                    maxChipWidthDp = maxWidthDp,
                    onRecentSearchClick = onRecentSearchClick,
                )
                if (layout.secondRow.isNotEmpty()) {
                    RecentSearchChipRow(
                        queries = layout.secondRow,
                        maxChipWidthDp = maxWidthDp,
                        onRecentSearchClick = onRecentSearchClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentSearchChipRow(
    queries: List<RecentSearchQuery>,
    maxChipWidthDp: Float,
    onRecentSearchClick: (RecentSearchQuery) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ChipItemSpacing),
    ) {
        queries.forEach { item ->
            AssistChip(
                onClick = { onRecentSearchClick(item) },
                label = {
                    Text(
                        text = recentSearchChipDisplayText(
                            query = item.query,
                            maxChipWidthDp = maxChipWidthDp,
                            horizontalPaddingDp = ChipHorizontalPadding.value,
                            charWidthDp = ChipCharWidth.value,
                        ),
                        maxLines = 1,
                    )
                },
            )
        }
    }
}
