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

package com.google.samples.apps.nowinandroid.feature.settings.impl

import androidx.annotation.VisibleForTesting
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
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.samples.apps.nowinandroid.core.designsystem.component.NiaLoadingWheel
import com.google.samples.apps.nowinandroid.core.designsystem.icon.NiaIcons
import com.google.samples.apps.nowinandroid.core.designsystem.theme.NiaTheme
import com.google.samples.apps.nowinandroid.core.model.data.WikiHistoryEntry
import com.google.samples.apps.nowinandroid.core.model.data.WikiLanguage
import com.google.samples.apps.nowinandroid.core.ui.DevicePreviews
import com.google.samples.apps.nowinandroid.core.ui.TrackScreenViewEvent
import com.google.samples.apps.nowinandroid.feature.settings.impl.R.string

@Composable
internal fun WikiHistoryScreen(
    onBackClick: () -> Unit,
    onWikiPageClick: (title: String, language: WikiLanguage) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WikiHistoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    WikiHistoryScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onWikiPageClick = onWikiPageClick,
        onClearHistory = viewModel::clearHistory,
        modifier = modifier,
    )
}

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
@Composable
internal fun WikiHistoryScreen(
    uiState: WikiHistoryUiState,
    onBackClick: () -> Unit,
    onWikiPageClick: (title: String, language: WikiLanguage) -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showClearDialog by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.safeDrawing))
        HistoryTopBar(
            title = stringResource(string.feature_settings_impl_profile_history),
            showClearAction = uiState is WikiHistoryUiState.Success,
            onBackClick = onBackClick,
            onClearClick = { showClearDialog = true },
        )
        when (uiState) {
            WikiHistoryUiState.Loading -> HistoryLoadingState()
            WikiHistoryUiState.Empty -> HistoryEmptyState()
            is WikiHistoryUiState.Success -> {
                HistoryList(
                    entries = uiState.entries,
                    onWikiPageClick = onWikiPageClick,
                )
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(stringResource(string.feature_settings_impl_history_clear_title)) },
            text = { Text(stringResource(string.feature_settings_impl_history_clear_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearDialog = false
                        onClearHistory()
                    },
                ) {
                    Text(stringResource(string.feature_settings_impl_history_clear_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(stringResource(string.feature_settings_impl_history_clear_dismiss))
                }
            },
        )
    }

    TrackScreenViewEvent(screenName = "WikiHistory")
}

@Composable
private fun HistoryTopBar(
    title: String,
    showClearAction: Boolean,
    onBackClick: () -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = NiaIcons.ArrowBack,
                contentDescription = stringResource(string.feature_settings_impl_history_back),
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
        )
        if (showClearAction) {
            IconButton(onClick = onClearClick) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = stringResource(
                        string.feature_settings_impl_history_clear,
                    ),
                )
            }
        }
    }
}

@Composable
private fun HistoryList(
    entries: List<WikiHistoryEntry>,
    onWikiPageClick: (title: String, language: WikiLanguage) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("wiki-history:list"),
        contentPadding = PaddingValues(vertical = 8.dp),
    ) {
        items(
            items = entries,
            key = { entry -> "${entry.language.code}/${entry.title}" },
        ) { entry ->
            WikiHistoryListItem(
                entry = entry,
                onOpen = { onWikiPageClick(entry.title, entry.language) },
            )
        }
        item {
            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
        }
    }
}

@Composable
private fun WikiHistoryListItem(
    entry: WikiHistoryEntry,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        modifier = modifier
            .clickable(onClick = onOpen)
            .clip(RoundedCornerShape(8.dp))
            .testTag("wiki-history:item-${entry.language.code}-${entry.title}"),
        headlineContent = {
            Text(
                text = entry.title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        },
        supportingContent = {
            Text(
                text = entry.language.toDisplayName(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
    )
}

@Composable
private fun HistoryLoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
            .testTag("wiki-history:loading"),
    ) {
        NiaLoadingWheel(
            contentDesc = stringResource(string.feature_settings_impl_loading),
        )
    }
}

@Composable
private fun HistoryEmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .testTag("wiki-history:empty"),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(string.feature_settings_impl_history_empty_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(string.feature_settings_impl_history_empty_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@DevicePreviews
@Composable
private fun WikiHistoryScreenPreview() {
    NiaTheme {
        WikiHistoryScreen(
            uiState = WikiHistoryUiState.Success(
                entries = listOf(
                    WikiHistoryEntry(
                        title = "Kotlin",
                        language = WikiLanguage.ENGLISH,
                    ),
                    WikiHistoryEntry(
                        title = "维基百科",
                        language = WikiLanguage.CHINESE_SIMPLIFIED,
                    ),
                ),
            ),
            onBackClick = {},
            onWikiPageClick = { _, _ -> },
            onClearHistory = {},
        )
    }
}
