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

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.samples.apps.nowinandroid.core.domain.GetWikiPageUseCase
import com.google.samples.apps.nowinandroid.core.model.data.WikiLanguage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val WIKI_PAGE_NAV_TAG = "WikiPageNav"

@HiltViewModel
class WikiPageViewModel @Inject constructor(
    private val getWikiPageUseCase: GetWikiPageUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<WikiPageUiState>(WikiPageUiState.Idle)
    val uiState: StateFlow<WikiPageUiState> = _uiState.asStateFlow()

    fun loadPage(
        title: String,
        language: WikiLanguage = WikiLanguage.ENGLISH,
    ) {
        val trimmed = title.trim()
        Log.d(
            WIKI_PAGE_NAV_TAG,
            "loadPage request title='$trimmed' language=${language.code} " +
                "currentState=${_uiState.value::class.simpleName}",
        )
        if (trimmed.isEmpty()) {
            Log.d(WIKI_PAGE_NAV_TAG, "loadPage skip: empty title")
            _uiState.value = WikiPageUiState.Idle
            return
        }

        viewModelScope.launch {
            _uiState.value = WikiPageUiState.Loading
            runCatching {
                getWikiPageUseCase(
                    title = trimmed,
                    language = language,
                )
            }.onSuccess { page ->
                Log.d(
                    WIKI_PAGE_NAV_TAG,
                    "loadPage ok title='${page.title}' htmlLen=${page.html.length} " +
                        "resources=${page.resourceUrls.size}",
                )
                _uiState.value = WikiPageUiState.Success(page)
            }.onFailure { error ->
                Log.e(
                    WIKI_PAGE_NAV_TAG,
                    "loadPage fail title='$trimmed' language=${language.code}",
                    error,
                )
                _uiState.value = WikiPageUiState.Error
            }
        }
    }
}
