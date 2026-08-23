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

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.samples.apps.nowinandroid.core.domain.CreateWikiBookmarkFolderUseCase
import com.google.samples.apps.nowinandroid.core.domain.ObserveWikiBookmarkFoldersUseCase
import com.google.samples.apps.nowinandroid.core.domain.RenameWikiBookmarkFolderUseCase
import com.google.samples.apps.nowinandroid.core.domain.ToggleWikiBookmarkUseCase
import com.google.samples.apps.nowinandroid.core.model.data.WikiLanguage
import com.google.samples.apps.nowinandroid.feature.bookmarks.api.R
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FolderRenameUiState(
    val folderId: Long,
    val draftName: String,
)

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    observeWikiBookmarkFoldersUseCase: ObserveWikiBookmarkFoldersUseCase,
    private val toggleWikiBookmarkUseCase: ToggleWikiBookmarkUseCase,
    private val createWikiBookmarkFolderUseCase: CreateWikiBookmarkFolderUseCase,
    private val renameWikiBookmarkFolderUseCase: RenameWikiBookmarkFolderUseCase,
) : ViewModel() {

    val uiState: StateFlow<WikiBookmarksUiState> =
        observeWikiBookmarkFoldersUseCase()
            .map { folders ->
                when {
                    folders.isEmpty() -> WikiBookmarksUiState.Empty
                    else -> WikiBookmarksUiState.Success(folders = folders)
                }
            }
            .onStart { emit(WikiBookmarksUiState.Loading) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = WikiBookmarksUiState.Loading,
            )

    private val _renameState = MutableStateFlow<FolderRenameUiState?>(null)
    val renameState: StateFlow<FolderRenameUiState?> = _renameState.asStateFlow()

    fun toggleBookmark(
        title: String,
        language: WikiLanguage,
        description: String? = null,
        thumbnailUrl: String? = null,
    ) {
        viewModelScope.launch {
            toggleWikiBookmarkUseCase(
                title = title,
                language = language,
                description = description,
                thumbnailUrl = thumbnailUrl,
            )
        }
    }

    fun createBookmarkFolder() {
        viewModelScope.launch {
            // 若正在改名，先落库，避免被新夹的改名会话覆盖
            val pendingRename = _renameState.value
            if (pendingRename != null) {
                renameWikiBookmarkFolderUseCase(
                    folderId = pendingRename.folderId,
                    name = pendingRename.draftName,
                )
                _renameState.value = null
            }

            val folderCount = when (val state = uiState.value) {
                is WikiBookmarksUiState.Success -> state.folders.size
                WikiBookmarksUiState.Empty -> 0
                WikiBookmarksUiState.Loading -> return@launch
            }

            val name = context.getString(
                R.string.feature_bookmarks_api_new_folder_name,
                folderCount + 1,
            )
            val folderId = createWikiBookmarkFolderUseCase(name = name) ?: return@launch
            startRenameFolder(folderId = folderId, currentName = name)
        }
    }

    fun startRenameFolder(folderId: Long, currentName: String) {
        _renameState.value = FolderRenameUiState(
            folderId = folderId,
            draftName = currentName,
        )
    }

    fun onRenameDraftChanged(draftName: String) {
        _renameState.update { current ->
            current?.copy(draftName = draftName)
        }
    }

    fun confirmRenameFolder() {
        val state = _renameState.value ?: return
        viewModelScope.launch {
            val renamed = renameWikiBookmarkFolderUseCase(
                folderId = state.folderId,
                name = state.draftName,
            )
            if (renamed) {
                _renameState.value = null
            }
        }
    }

    fun cancelRenameFolder() {
        _renameState.value = null
    }
}
