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
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.samples.apps.nowinandroid.core.domain.ClearRecentSearchesUseCase
import com.google.samples.apps.nowinandroid.core.domain.GetRecentSearchQueriesUseCase
import com.google.samples.apps.nowinandroid.core.domain.GetWikiSuggestionsUseCase
import com.google.samples.apps.nowinandroid.core.domain.SaveRecentSearchQueryUseCase
import com.google.samples.apps.nowinandroid.core.model.data.WikiLanguage
import com.google.samples.apps.nowinandroid.core.model.data.WikiSuggestionsResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchSuggestionViewModel @Inject constructor(
    private val getWikiSuggestionsUseCase: GetWikiSuggestionsUseCase,
    getRecentSearchQueriesUseCase: GetRecentSearchQueriesUseCase,
    private val saveRecentSearchQueryUseCase: SaveRecentSearchQueryUseCase,
    private val clearRecentSearchesUseCase: ClearRecentSearchesUseCase,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val query: StateFlow<String> = savedStateHandle.getStateFlow(
        key = SEARCH_SUGGESTION_QUERY,
        initialValue = "",
    )
    val selectedLanguage: StateFlow<WikiLanguage> = savedStateHandle.getStateFlow(
        key = SELECTED_LANGUAGE,
        initialValue = WikiLanguage.CHINESE,
    )
    private val _uiState = MutableStateFlow<SearchSuggestionUiState>(SearchSuggestionUiState.Idle)
    val uiState: StateFlow<SearchSuggestionUiState> = _uiState.asStateFlow()

    val recentSearchQueriesUiState: StateFlow<RecentSearchQueriesUiState> =
        getRecentSearchQueriesUseCase()
            .map(RecentSearchQueriesUiState::Success)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = RecentSearchQueriesUiState.Loading,
            )

    init {
        combine(
            query.map(String::trim),
            selectedLanguage,
        ) { trimmedQuery, language ->
            trimmedQuery to language
        }
            .distinctUntilChanged()
            .onEach { (trimmedQuery, _) ->
                if (trimmedQuery.isEmpty()) {
                    _uiState.value = SearchSuggestionUiState.Idle
                }
            }
            .filter { (trimmedQuery, _) -> trimmedQuery.isNotEmpty() }
            .debounce(SUGGESTION_DEBOUNCE_MILLIS)
            .onEach { (trimmedQuery, language) ->
                loadSuggestions(query = trimmedQuery, language = language)
            }
            .launchIn(viewModelScope)
    }

    fun onQueryChanged(newQuery: String) {
        savedStateHandle[SEARCH_SUGGESTION_QUERY] = newQuery
    }

    fun onLanguageSelected(selectedLanguage: WikiLanguage) {
        savedStateHandle[SELECTED_LANGUAGE] = selectedLanguage
    }

    /**
     * Persists an explicit search (IME search / enter). Blank queries are ignored.
     */
    fun saveRecentSearch(query: String, language: WikiLanguage) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            saveRecentSearchQueryUseCase(query = trimmed, language = language)
        }
    }

    fun clearRecentSearches() {
        viewModelScope.launch {
            clearRecentSearchesUseCase()
        }
    }

    private fun loadSuggestions(query: String, language: WikiLanguage) {
        viewModelScope.launch {
            // Temporary connectivity debug log. Remove after suggestion chain is verified.
            Log.d(
                "WikiSuggestions",
                "loadSuggestions language=${language.code} query=$query",
            )
            _uiState.value = SearchSuggestionUiState.Loading

            runCatching {
                getWikiSuggestionsUseCase(
                    query = query,
                    language = language,
                )
            }.onSuccess {
                // Temporary connectivity debug log. Remove after suggestion chain is verified.
                Log.d("WikiSuggestions", "loadSuggestions success count=${it.items.size}")
                handleSuggestionsLoaded(it)
            }
                .onFailure {
                    // Temporary connectivity debug log. Remove after suggestion chain is verified.
                    Log.e("WikiSuggestions", "loadSuggestions failure", it)
                    _uiState.value = SearchSuggestionUiState.Error
                }
        }
    }

    private fun handleSuggestionsLoaded(result: WikiSuggestionsResult) {
        _uiState.update {
            if (result.items.isEmpty()) {
                SearchSuggestionUiState.Empty
            } else {
                SearchSuggestionUiState.Success(result.items)
            }
        }
    }
}

private const val SEARCH_SUGGESTION_QUERY = "searchSuggestionQuery"
private const val SELECTED_LANGUAGE = "selectedLanguage"
private const val SUGGESTION_DEBOUNCE_MILLIS = 300L
