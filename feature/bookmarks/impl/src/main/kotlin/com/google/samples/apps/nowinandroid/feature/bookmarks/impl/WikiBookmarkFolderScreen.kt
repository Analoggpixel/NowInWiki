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

package com.google.samples.apps.nowinandroid.feature.bookmarks.impl

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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.samples.apps.nowinandroid.core.designsystem.component.NiaLoadingWheel
import com.google.samples.apps.nowinandroid.core.designsystem.icon.NiaIcons
import com.google.samples.apps.nowinandroid.core.designsystem.theme.NiaTheme
import com.google.samples.apps.nowinandroid.core.model.data.WikiBookmark
import com.google.samples.apps.nowinandroid.core.model.data.WikiBookmarkFolder
import com.google.samples.apps.nowinandroid.core.model.data.WikiLanguage
import com.google.samples.apps.nowinandroid.core.ui.DevicePreviews
import com.google.samples.apps.nowinandroid.core.ui.TrackScreenViewEvent
import com.google.samples.apps.nowinandroid.core.ui.WikiBookmarkToggleButton

@Composable
internal fun WikiBookmarkFolderScreen(
    folderId: Long,
    onBackClick: () -> Unit,
    onWikiPageClick: (title: String, language: WikiLanguage) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WikiBookmarkFolderViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(folderId) {
        viewModel.loadFolder(folderId)
    }

    WikiBookmarkFolderScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onWikiPageClick = onWikiPageClick,
        onToggleBookmark = viewModel::toggleBookmark,
        modifier = modifier,
    )
}

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
@Composable
internal fun WikiBookmarkFolderScreen(
    uiState: WikiBookmarkFolderUiState,
    onBackClick: () -> Unit,
    onWikiPageClick: (title: String, language: WikiLanguage) -> Unit,
    onToggleBookmark: (
        title: String,
        language: WikiLanguage,
        description: String?,
        thumbnailUrl: String?,
    ) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.safeDrawing))
        FolderTopBar(
            title = when (uiState) {
                is WikiBookmarkFolderUiState.Success -> uiState.folder.name
                else -> ""
            },
            onBackClick = onBackClick,
        )
        when (uiState) {
            WikiBookmarkFolderUiState.Loading -> FolderLoadingState()
            WikiBookmarkFolderUiState.Empty -> FolderEmptyState()
            WikiBookmarkFolderUiState.NotFound -> FolderNotFoundState()
            is WikiBookmarkFolderUiState.Success -> {
                if (uiState.folder.bookmarks.isEmpty()) {
                    FolderEmptyState()
                } else {
                    FolderBookmarkList(
                        bookmarks = uiState.folder.bookmarks,
                        onWikiPageClick = onWikiPageClick,
                        onToggleBookmark = onToggleBookmark,
                    )
                }
            }
        }
    }

    TrackScreenViewEvent(screenName = "SavedFolder")
}

@Composable
private fun FolderTopBar(
    title: String,
    onBackClick: () -> Unit,
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
                contentDescription = "Back",
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp),
        )
    }
}

@Composable
private fun FolderBookmarkList(
    bookmarks: List<WikiBookmark>,
    onWikiPageClick: (title: String, language: WikiLanguage) -> Unit,
    onToggleBookmark: (
        title: String,
        language: WikiLanguage,
        description: String?,
        thumbnailUrl: String?,
    ) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("bookmarks:folder-list"),
        contentPadding = PaddingValues(vertical = 8.dp),
    ) {
        items(
            items = bookmarks,
            key = { bookmark -> bookmark.id },
        ) { bookmark ->
            WikiBookmarkListItem(
                bookmark = bookmark,
                onOpen = {
                    onWikiPageClick(bookmark.title, bookmark.language)
                },
                onToggleBookmark = {
                    onToggleBookmark(
                        bookmark.title,
                        bookmark.language,
                        bookmark.description,
                        bookmark.thumbnailUrl,
                    )
                },
            )
        }
        item {
            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
        }
    }
}

@Composable
internal fun WikiBookmarkListItem(
    bookmark: WikiBookmark,
    onOpen: () -> Unit,
    onToggleBookmark: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        modifier = modifier
            .clickable(onClick = onOpen)
            .clip(RoundedCornerShape(8.dp))
            .testTag("bookmarks:item-${bookmark.id}"),
        headlineContent = {
            Text(
                text = bookmark.title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        },
        supportingContent = {
            Text(
                text = bookmark.language.toDisplayName(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailingContent = {
            WikiBookmarkToggleButton(
                isBookmarked = true,
                onToggle = onToggleBookmark,
            )
        },
    )
}

@Composable
private fun FolderLoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
            .testTag("bookmarks:folder-loading"),
    ) {
        NiaLoadingWheel(contentDesc = "Loading wiki bookmark folder")
    }
}

@Composable
private fun FolderEmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .testTag("bookmarks:folder-empty"),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "This folder is empty",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Saved pages in this folder will show up here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun FolderNotFoundState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .testTag("bookmarks:folder-not-found"),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Folder not found",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

@DevicePreviews
@Composable
private fun WikiBookmarkFolderScreenPreview() {
    NiaTheme {
        WikiBookmarkFolderScreen(
            uiState = WikiBookmarkFolderUiState.Success(
                folder = WikiBookmarkFolder(
                    id = 1,
                    name = "默认收藏",
                    bookmarks = listOf(
                        WikiBookmark(
                            id = 1,
                            folderId = 1,
                            title = "Kotlin",
                            language = WikiLanguage.ENGLISH,
                            bookmarkedAt = 0L,
                            thumbnailUrl = null,
                        ),
                    ),
                ),
            ),
            onBackClick = {},
            onWikiPageClick = { _, _ -> },
            onToggleBookmark = { _, _, _, _ -> },
        )
    }
}
