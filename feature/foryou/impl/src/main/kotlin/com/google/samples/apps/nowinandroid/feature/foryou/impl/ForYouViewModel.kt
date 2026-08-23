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

package com.google.samples.apps.nowinandroid.feature.foryou.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.samples.apps.nowinandroid.core.domain.GetWikiFeedUseCase
import com.google.samples.apps.nowinandroid.core.model.data.WikiLanguage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForYouViewModel @Inject constructor(
    private val getWikiFeedUseCase: GetWikiFeedUseCase,
) : ViewModel() {

    private val feedLanguage = WikiLanguage.CHINESE_SIMPLIFIED

    private val _uiState = MutableStateFlow<ForYouFeedUiState>(ForYouFeedUiState.Loading)
    val uiState: StateFlow<ForYouFeedUiState> = _uiState.asStateFlow()

    /**
     * ? [androidx.compose.material3.pulltorefresh.PullToRefreshBox] ???
     *
     * ???????????? isRefreshing??? onRefresh???
     * 1. ??????PullToRefreshBox ??????????? isRefreshing ?? false?
     * 2. ????????????? Screen ??? onRefresh???? ViewModel ? [onRefresh]??
     * 3. ? [onRefresh] ?? isRefreshing ?? true???????????????
     * 4. ?????? isRefreshing ?? false?????????
     *
     * ?? isRefreshing ??? ViewModel ?????????????????????
     */
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadFeed()
    }

    fun loadFeed(language: WikiLanguage = feedLanguage) {
        viewModelScope.launch {
            _uiState.value = ForYouFeedUiState.Loading
            runCatching {
                getWikiFeedUseCase(language = language)
            }.onSuccess { items ->
                _uiState.value = if (items.isEmpty()) {
                    ForYouFeedUiState.Empty
                } else {
                    ForYouFeedUiState.Success(items)
                }
            }.onFailure {
                _uiState.value = ForYouFeedUiState.Error
            }
        }
    }

    /**
     * PullToRefreshBox ???????????
     * ??????????????? Loading??
     * ???????????? UI??????????????????????
     */
    fun onRefresh(language: WikiLanguage = feedLanguage) {
        if (_isRefreshing.value) return
        viewModelScope.launch {
            _isRefreshing.value = true
            runCatching {
                getWikiFeedUseCase(language = language)
            }.onSuccess { items ->
                if (items.isNotEmpty()) {
                    _uiState.value = ForYouFeedUiState.Success(items)
                }
            }
            _isRefreshing.value = false
        }
    }
}
