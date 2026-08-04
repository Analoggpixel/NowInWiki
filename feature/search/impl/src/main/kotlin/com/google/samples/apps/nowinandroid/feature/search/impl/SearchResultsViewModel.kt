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
import com.google.samples.apps.nowinandroid.core.domain.GetWikiSuggestionsUseCase
import com.google.samples.apps.nowinandroid.core.model.data.WikiLanguage
import com.google.samples.apps.nowinandroid.core.model.data.WikiSuggestionsResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchResultsViewModel @Inject constructor(
    private val getWikiSuggestionsUseCase: GetWikiSuggestionsUseCase,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val searchQuery: StateFlow<String> = savedStateHandle.getStateFlow(
        key = SEARCH_RESULTS_QUERY,
        initialValue = "",
    )
    val selectedLanguage: StateFlow<WikiLanguage> = savedStateHandle.getStateFlow(
        key = SELECTED_LANGUAGE,
        initialValue = WikiLanguage.CHINESE,
    )

    private val _uiState = MutableStateFlow<SearchResultsUiState>(SearchResultsUiState.Idle)
    val uiState: StateFlow<SearchResultsUiState> = _uiState.asStateFlow()

    fun onSearch(query: String, selectedLanguage: WikiLanguage) {
        val trimmed = query.trim()
        savedStateHandle[SEARCH_RESULTS_QUERY] = trimmed
        savedStateHandle[SELECTED_LANGUAGE] = selectedLanguage

        if (trimmed.isEmpty()) {
            _uiState.value = SearchResultsUiState.Idle
            return
        }

        viewModelScope.launch {
            _uiState.value = SearchResultsUiState.Loading

            runCatching {
                // Language picker UI is not wired yet; default to English.
                getWikiSuggestionsUseCase(
                    query = trimmed,
                    language = selectedLanguage,
                )
            }.onSuccess(::handleLoaded)
                .onFailure {
                    _uiState.value = SearchResultsUiState.Error
                }
        }
    }

    private fun handleLoaded(result: WikiSuggestionsResult) {
        val firstPageItems = result.items.take(SEARCH_RESULTS_PAGE_SIZE)
        _uiState.update {
            if (firstPageItems.isEmpty()) {
                SearchResultsUiState.Empty
            } else {
                SearchResultsUiState.Success(firstPageItems)
            }
        }
    }

    fun onQueryChanged(query: String) {
        savedStateHandle[SEARCH_RESULTS_QUERY] = query
    }

    fun onLanguageSelected(selectedLanguage: WikiLanguage) {
        savedStateHandle[SELECTED_LANGUAGE] = selectedLanguage
    }
}

private const val SEARCH_RESULTS_QUERY = "searchResultsQuery"
private const val SELECTED_LANGUAGE = "selectedLanguage"
private const val SEARCH_RESULTS_PAGE_SIZE = 20
