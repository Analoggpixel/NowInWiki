/*
 * Copyright 2022 The Android Open Source Project
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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.samples.apps.nowinandroid.core.designsystem.component.DynamicAsyncImage
import com.google.samples.apps.nowinandroid.core.designsystem.component.NiaLoadingWheel
import com.google.samples.apps.nowinandroid.core.designsystem.icon.NiaIcons
import com.google.samples.apps.nowinandroid.core.designsystem.theme.NiaTheme
import com.google.samples.apps.nowinandroid.core.model.data.WikiBookmark
import com.google.samples.apps.nowinandroid.core.model.data.WikiBookmarkFolder
import com.google.samples.apps.nowinandroid.core.model.data.WikiLanguage
import com.google.samples.apps.nowinandroid.core.ui.DevicePreviews
import com.google.samples.apps.nowinandroid.core.ui.TrackScreenViewEvent
import com.google.samples.apps.nowinandroid.feature.bookmarks.api.R

private const val FOLDER_THUMBNAIL_PREVIEW_COUNT = 3

@Composable
internal fun BookmarksScreen(
    onFolderClick: (folderId: Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BookmarksViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val renameState by viewModel.renameState.collectAsStateWithLifecycle()

    BookmarksScreen(
        uiState = uiState,
        renameState = renameState,
        onFolderClick = onFolderClick,
        onCreateFolderClick = viewModel::createBookmarkFolder,
        onRenameFolderClick = viewModel::startRenameFolder,
        onRenameDraftChanged = viewModel::onRenameDraftChanged,
        onConfirmRename = viewModel::confirmRenameFolder,
        onCancelRename = viewModel::cancelRenameFolder,
        modifier = modifier,
    )
}

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
@Composable
internal fun BookmarksScreen(
    uiState: WikiBookmarksUiState,
    onFolderClick: (folderId: Long) -> Unit,
    onCreateFolderClick: () -> Unit = {},
    renameState: FolderRenameUiState? = null,
    onRenameFolderClick: (folderId: Long, currentName: String) -> Unit = { _, _ -> },
    onRenameDraftChanged: (String) -> Unit = {},
    onConfirmRename: () -> Unit = {},
    onCancelRename: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        WikiBookmarksUiState.Loading -> LoadingState(modifier)
        WikiBookmarksUiState.Empty -> WikiBookmarkFoldersList(
            folders = emptyList(),
            renameState = renameState,
            onFolderClick = onFolderClick,
            onCreateFolderClick = onCreateFolderClick,
            onRenameFolderClick = onRenameFolderClick,
            onRenameDraftChanged = onRenameDraftChanged,
            onConfirmRename = onConfirmRename,
            onCancelRename = onCancelRename,
            modifier = modifier,
        )
        is WikiBookmarksUiState.Success -> WikiBookmarkFoldersList(
            folders = uiState.folders,
            renameState = renameState,
            onFolderClick = onFolderClick,
            onCreateFolderClick = onCreateFolderClick,
            onRenameFolderClick = onRenameFolderClick,
            onRenameDraftChanged = onRenameDraftChanged,
            onConfirmRename = onConfirmRename,
            onCancelRename = onCancelRename,
            modifier = modifier,
        )
    }

    TrackScreenViewEvent(screenName = "Saved")
}

@Composable
private fun WikiBookmarkFoldersList(
    folders: List<WikiBookmarkFolder>,
    renameState: FolderRenameUiState?,
    onFolderClick: (folderId: Long) -> Unit,
    onCreateFolderClick: () -> Unit,
    onRenameFolderClick: (folderId: Long, currentName: String) -> Unit,
    onRenameDraftChanged: (String) -> Unit,
    onConfirmRename: () -> Unit,
    onCancelRename: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .testTag("bookmarks:wiki-list"),
    ) {
        CreateFolderRow(onClick = onCreateFolderClick)
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .pointerInput(Unit) {
                    detectTapGestures {
                        onConfirmRename()
                        focusManager.clearFocus()
                    }
                },
            contentPadding = PaddingValues(vertical = 8.dp),
        ) {
            items(
                items = folders,
                key = { folder -> folder.id },
            ) { folder ->
                val isEditing = renameState?.folderId == folder.id
                WikiBookmarkFolderRow(
                    folder = folder,
                    isEditing = isEditing,
                    draftName = if (isEditing) renameState.draftName else folder.name,
                    onClick = { onFolderClick(folder.id) },
                    onRenameClick = { onRenameFolderClick(folder.id, folder.name) },
                    onDraftNameChanged = onRenameDraftChanged,
                    onConfirmRename = onConfirmRename,
                    onCancelRename = onCancelRename,
                )
            }

            item {
                Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
            }
        }
    }
}

@Composable
private fun CreateFolderRow(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("bookmarks:create-folder"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = NiaIcons.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        Text(
            text = stringResource(R.string.feature_bookmarks_api_create_folder),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun WikiBookmarkFolderRow(
    folder: WikiBookmarkFolder,
    isEditing: Boolean,
    draftName: String,
    onClick: () -> Unit,
    onRenameClick: () -> Unit,
    onDraftNameChanged: (String) -> Unit,
    onConfirmRename: () -> Unit,
    onCancelRename: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val previewBookmarks = folder.bookmarks.take(FOLDER_THUMBNAIL_PREVIEW_COUNT)
    // Plural-aware label: picks "1 item" or "N items" based on folder.bookmarks.size.
    val countLabel = pluralStringResource(
        R.plurals.feature_bookmarks_api_folder_item_count,
        folder.bookmarks.size, // quantity for one/other rule
        folder.bookmarks.size, // value for %d
    )
    val focusRequester = remember { FocusRequester() }
    var textFieldValue by remember(folder.id) {
        mutableStateOf(TextFieldValue(text = folder.name))
    }

    LaunchedEffect(isEditing, folder.id) {
        if (isEditing) {
            textFieldValue = TextFieldValue(
                text = draftName,
                // selection 表示输入框里当前选中的文字范围（或光标位置）
                selection = TextRange(0, draftName.length),
            )
            focusRequester.requestFocus()
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            // 假如可编辑就不可点击，不可编辑就可以点击
            .then(
                if (isEditing) {
                    Modifier
                } else {
                    Modifier.clickable(onClick = onClick)
                },
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("bookmarks:folder-${folder.id}"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp),
        ) {
            if (isEditing) {
                OutlinedTextField(
                    value = textFieldValue,
                    onValueChange = { value ->
                        textFieldValue = value
                        onDraftNameChanged(value.text)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .testTag("bookmarks:rename-field-${folder.id}")
                        .onFocusChanged { focusState ->
                            if (!focusState.isFocused && isEditing) {
                            }
                        },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { onConfirmRename() },
                    ),
                )
            } else {
                Text(
                    text = folder.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(modifier.height(4.dp))
            Text(
                text = folder.description?.takeIf { it.isNotBlank() } ?: countLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (!folder.description.isNullOrBlank()) {
                Spacer(modifier.height(2.dp))
                Text(
                    text = countLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        FolderThumbnailStack(bookmarks = previewBookmarks)
        if (isEditing) {
            IconButton(
                onClick = onConfirmRename,
                modifier = Modifier.testTag("bookmarks:confirm-rename-${folder.id}"),
            ) {
                Icon(
                    imageVector = NiaIcons.Check,
                    contentDescription = stringResource(R.string.feature_bookmarks_api_confirm_rename),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        } else {
            IconButton(
                onClick = onRenameClick,
                modifier = Modifier.testTag("bookmarks:rename-folder-${folder.id}"),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = stringResource(R.string.feature_bookmarks_api_rename_folder),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun FolderThumbnailStack(
    bookmarks: List<WikiBookmark>,
    modifier: Modifier = Modifier,
) {
    if (bookmarks.isEmpty()) return

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy((-10).dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        bookmarks.forEachIndexed { index, bookmark ->
            FolderThumbnail(
                imageUrl = bookmark.thumbnailUrl,
                modifier = Modifier.zIndex((bookmarks.size - index).toFloat()),
            )
        }
    }
}

@Composable
private fun FolderThumbnail(
    imageUrl: String?,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(6.dp)
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.surface,
                shape = shape,
            ),
    ) {
        if (!imageUrl.isNullOrBlank()) {
            DynamicAsyncImage(
                imageUrl = imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
            .testTag("bookmarks:loading"),
    ) {
        NiaLoadingWheel(
            contentDesc = stringResource(R.string.feature_bookmarks_api_loading_folders),
        )
    }
}

@DevicePreviews
@Composable
private fun WikiBookmarkFoldersListPreview() {
    NiaTheme {
        BookmarksScreen(
            uiState = WikiBookmarksUiState.Success(
                folders = listOf(
                    WikiBookmarkFolder(
                        id = 1,
                        name = "Saved",
                        bookmarks = listOf(
                            WikiBookmark(
                                id = 1,
                                folderId = 1,
                                title = "Kotlin",
                                language = WikiLanguage.ENGLISH,
                                bookmarkedAt = 0L,
                                thumbnailUrl = null,
                            ),
                            WikiBookmark(
                                id = 2,
                                folderId = 1,
                                title = "Compose",
                                language = WikiLanguage.ENGLISH,
                                bookmarkedAt = 0L,
                                thumbnailUrl = null,
                            ),
                        ),
                    ),
                ),
            ),
            onFolderClick = {},
        )
    }
}
