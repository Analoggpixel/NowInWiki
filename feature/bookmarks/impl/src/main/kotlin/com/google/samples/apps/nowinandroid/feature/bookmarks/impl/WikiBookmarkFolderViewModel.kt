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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.samples.apps.nowinandroid.core.domain.ObserveWikiBookmarkFolderUseCase
import com.google.samples.apps.nowinandroid.core.domain.ToggleWikiBookmarkUseCase
import com.google.samples.apps.nowinandroid.core.model.data.WikiLanguage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WikiBookmarkFolderViewModel @Inject constructor(
    private val observeWikiBookmarkFolderUseCase: ObserveWikiBookmarkFolderUseCase,
    private val toggleWikiBookmarkUseCase: ToggleWikiBookmarkUseCase,
) : ViewModel() {

    private val folderId = MutableStateFlow<Long?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<WikiBookmarkFolderUiState> =
        folderId
            .flatMapLatest { id ->
                if (id == null) {
                    flowOf(WikiBookmarkFolderUiState.Loading)
                } else {
                    observeWikiBookmarkFolderUseCase(id)
                        .map { folder ->
                            if (folder == null) {
                                WikiBookmarkFolderUiState.NotFound
                            } else {
                                WikiBookmarkFolderUiState.Success(folder = folder)
                            }
                        }
                        .onStart { emit(WikiBookmarkFolderUiState.Loading) }
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = WikiBookmarkFolderUiState.Loading,
            )

    fun loadFolder(folderId: Long) {
        this.folderId.value = folderId
    }

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
}
