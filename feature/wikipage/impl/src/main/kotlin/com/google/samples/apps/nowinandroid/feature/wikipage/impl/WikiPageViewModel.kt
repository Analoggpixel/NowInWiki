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

package com.google.samples.apps.nowinandroid.feature.wikipage.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.samples.apps.nowinandroid.core.domain.GetWikiPageUseCase
import com.google.samples.apps.nowinandroid.core.domain.ObserveWikiBookmarkStatusUseCase
import com.google.samples.apps.nowinandroid.core.domain.ToggleWikiBookmarkUseCase
import com.google.samples.apps.nowinandroid.core.model.data.WikiLanguage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WikiPageViewModel @Inject constructor(
    private val observeWikiBookmarkStatusUseCase: ObserveWikiBookmarkStatusUseCase,
    private val toggleWikiBookmarkUseCase: ToggleWikiBookmarkUseCase,
    private val getWikiPageUseCase: GetWikiPageUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<WikiPageUiState>(WikiPageUiState.Idle)
    val uiState: StateFlow<WikiPageUiState> = _uiState

    private val bookmarkTarget = MutableStateFlow<BookmarkTarget?>(null)

    val isBookmarked: StateFlow<Boolean> = bookmarkTarget
        .flatMapLatest { target ->
            if (target == null) {
                flowOf(false)
            } else {
                observeWikiBookmarkStatusUseCase(
                    title = target.title,
                    language = target.language,
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )

    fun loadPage(
        title: String,
        language: WikiLanguage = WikiLanguage.ENGLISH,
    ) {
        val trimmed = title.trim()
        if (trimmed.isEmpty()) {
            bookmarkTarget.value = null
            _uiState.value = WikiPageUiState.Idle
            return
        }

        bookmarkTarget.value = BookmarkTarget(title = trimmed, language = language)

        viewModelScope.launch {
            _uiState.value = WikiPageUiState.Loading
            runCatching {
                getWikiPageUseCase(
                    title = trimmed,
                    language = language,
                )
            }.onSuccess { page ->
                bookmarkTarget.value = BookmarkTarget(title = page.title, language = language)
                _uiState.value = WikiPageUiState.Success(page)
            }.onFailure {
                _uiState.value = WikiPageUiState.Error
            }
        }
    }

    fun toggleBookmark() {
        val target = bookmarkTarget.value ?: return
        viewModelScope.launch {
            toggleWikiBookmarkUseCase(
                title = target.title,
                language = target.language,
            )
        }
    }

    private data class BookmarkTarget(
        val title: String,
        val language: WikiLanguage,
    )
}
