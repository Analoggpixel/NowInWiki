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

package com.google.samples.apps.nowinandroid.feature.settings.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.samples.apps.nowinandroid.core.data.repository.WikiPageRepository
import com.google.samples.apps.nowinandroid.core.model.data.WikiLanguage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val wikiPageRepository: WikiPageRepository,
) : ViewModel() {

    private val _isOpeningRandomArticle = MutableStateFlow(false)
    val isOpeningRandomArticle: StateFlow<Boolean> = _isOpeningRandomArticle.asStateFlow()

    private val _randomArticleEvents = MutableSharedFlow<RandomArticleEvent>(extraBufferCapacity = 1)
    val randomArticleEvents: SharedFlow<RandomArticleEvent> = _randomArticleEvents.asSharedFlow()

    fun onRandomArticleClick(language: WikiLanguage) {
        if (_isOpeningRandomArticle.value) return
        viewModelScope.launch {
            _isOpeningRandomArticle.value = true
            runCatching {
                wikiPageRepository.getRandomTitle(language)
            }.onSuccess { title ->
                _randomArticleEvents.emit(RandomArticleEvent.Open(title = title, language = language))
            }.onFailure {
                _randomArticleEvents.emit(RandomArticleEvent.Error)
            }
            _isOpeningRandomArticle.value = false
        }
    }
}

sealed interface RandomArticleEvent {
    data class Open(
        val title: String,
        val language: WikiLanguage,
    ) : RandomArticleEvent

    data object Error : RandomArticleEvent
}
