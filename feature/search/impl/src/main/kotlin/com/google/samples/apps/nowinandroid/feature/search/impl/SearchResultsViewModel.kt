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

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.google.samples.apps.nowinandroid.core.domain.GetWikiSearchPagingDataUseCase
import com.google.samples.apps.nowinandroid.core.domain.ObserveWikiBookmarkFoldersUseCase
import com.google.samples.apps.nowinandroid.core.domain.SaveRecentSearchQueryUseCase
import com.google.samples.apps.nowinandroid.core.domain.ToggleWikiBookmarkUseCase
import com.google.samples.apps.nowinandroid.core.model.data.WikiLanguage
import com.google.samples.apps.nowinandroid.core.model.data.WikiSuggestionItem
import com.google.samples.apps.nowinandroid.core.model.data.asSuggestionItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchResultsViewModel @Inject constructor(
    private val getWikiSearchPagingDataUseCase: GetWikiSearchPagingDataUseCase,
    private val toggleWikiBookmarkUseCase: ToggleWikiBookmarkUseCase,
    observeWikiBookmarkFoldersUseCase: ObserveWikiBookmarkFoldersUseCase,
    private val saveRecentSearchQueryUseCase: SaveRecentSearchQueryUseCase,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val searchQuery: StateFlow<String> = savedStateHandle.getStateFlow(
        key = SEARCH_RESULTS_QUERY,
        initialValue = "",
    )
    val selectedLanguage: StateFlow<WikiLanguage> = savedStateHandle.getStateFlow(
        key = SELECTED_LANGUAGE,
        initialValue = WikiLanguage.CHINESE_SIMPLIFIED,
    )

    /**
     * All bookmarked entries as [bookmarkKey] (title + language).
     * The list uses this set for checked state instead of observing each row.
     */
    val bookmarkedKeys: StateFlow<Set<String>> =
        observeWikiBookmarkFoldersUseCase()
            .map { folders ->
                folders.asSequence()
                    .flatMap { folder -> folder.bookmarks }
                    .map { bookmark -> bookmarkKey(bookmark.title, bookmark.language) }
                    .toSet()
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptySet(),
            )

    val searchResults: Flow<PagingData<WikiSuggestionItem>> =
        combine(searchQuery, selectedLanguage) { query, language ->
            query.trim() to language
        }
            .distinctUntilChanged()
            .flatMapLatest { (query, language) ->
                if (query.isEmpty()) {
                    flowOf(PagingData.empty())
                } else {
                    getWikiSearchPagingDataUseCase(
                        query = query,
                        language = language,
                    ).map { pagingData ->
                        pagingData.map { pageItem -> pageItem.asSuggestionItem() }
                    }
                }
            }
            .cachedIn(viewModelScope)

    fun onSearch(query: String, selectedLanguage: WikiLanguage) {
        val trimmed = query.trim()
        savedStateHandle[SEARCH_RESULTS_QUERY] = trimmed
        savedStateHandle[SELECTED_LANGUAGE] = selectedLanguage

        if (trimmed.isEmpty()) return

        viewModelScope.launch {
            saveRecentSearchQueryUseCase(
                query = trimmed,
                language = selectedLanguage,
            )
        }
    }

    fun onQueryChanged(query: String) {
        savedStateHandle[SEARCH_RESULTS_QUERY] = query
    }

    fun onLanguageSelected(selectedLanguage: WikiLanguage) {
        savedStateHandle[SELECTED_LANGUAGE] = selectedLanguage
    }

    fun toggleBookmark(item: WikiSuggestionItem) {
        viewModelScope.launch {
            toggleWikiBookmarkUseCase(
                title = item.title,
                language = item.itemLanguage,
                description = item.description ?: item.excerpt,
                thumbnailUrl = item.thumbnailUrl,
            )
        }
    }
}

internal fun bookmarkKey(title: String, language: WikiLanguage): String =
    "${language.code}\t${title.trim()}"

private const val SEARCH_RESULTS_QUERY = "searchResultsQuery"
private const val SELECTED_LANGUAGE = "selectedLanguage"
