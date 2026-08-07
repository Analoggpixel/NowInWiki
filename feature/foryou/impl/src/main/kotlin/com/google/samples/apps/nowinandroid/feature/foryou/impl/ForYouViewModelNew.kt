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
import com.google.samples.apps.nowinandroid.core.data.repository.UserDataRepository
import com.google.samples.apps.nowinandroid.core.domain.GetWikiFeedUseCase
import com.google.samples.apps.nowinandroid.core.model.data.WikiLanguage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForYouViewModelNew @Inject constructor(
    private val getWikiFeedUseCase: GetWikiFeedUseCase,
    private val userDataRepository: UserDataRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ForYouFeedUiState>(ForYouFeedUiState.Loading)
    val uiState: StateFlow<ForYouFeedUiState> = _uiState.asStateFlow()

    /**
     * 供 [androidx.compose.material3.pulltorefresh.PullToRefreshBox] 使用。
     *
     * 下拉刷新流程（不是「先改 isRefreshing，再调 onRefresh」）：
     * 1. 手指下拉时，PullToRefreshBox 内部处理跟手动画，此时 isRefreshing 仍为 false。
     * 2. 超过阈值并松手后，组件回调 Screen 传入的 onRefresh（接到本 ViewModel 的 [onRefresh]）。
     * 3. 在 [onRefresh] 里把 isRefreshing 设为 true，指示器进入「正在刷新」转圈。
     * 4. 请求结束后把 isRefreshing 设回 false，组件收起指示器。
     *
     * 因此 isRefreshing 必须由 ViewModel 在刷新期间显式维护，否则指示器会马上消失。
     */
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val preferredLanguage: StateFlow<WikiLanguage> =
        userDataRepository.userData
            .map { it.preferredWikiLanguage }
            .distinctUntilChanged()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = WikiLanguage.CHINESE,
            )

    init {
        loadFeed()
    }

    fun loadFeed(language: WikiLanguage = WikiLanguage.ENGLISH) {
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
     * PullToRefreshBox 松手后触发的刷新入口。
     * 刷新时保留当前列表（不切回全屏 Loading）。
     * 仅当拿到非空列表时才更新 UI；空结果或失败都视为空操作，避免交互被打断。
     */
    fun onRefresh(language: WikiLanguage = WikiLanguage.ENGLISH) {
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
