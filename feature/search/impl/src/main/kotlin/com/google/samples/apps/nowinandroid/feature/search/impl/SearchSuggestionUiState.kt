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

import com.google.samples.apps.nowinandroid.core.model.data.WikiSuggestionItem

/**
 * UI state for wiki search suggestions shown while the user is typing a query.
 */
sealed interface SearchSuggestionUiState {
    /**
     * No valid query has been entered yet, so no suggestion request should be shown as active.
     */
    data object Idle : SearchSuggestionUiState

    /**
     * Suggestions are currently being loaded from the remote wiki data source.
     */
    data object Loading : SearchSuggestionUiState

    /**
     * A valid request completed successfully but returned no suggestion items.
     */
    data object Empty : SearchSuggestionUiState

    /**
     * The request failed and the UI should surface an error state.
     */
    data object Error : SearchSuggestionUiState

    /**
     * Suggestions are available for display.
     */
    data class Success(
        val items: List<WikiSuggestionItem>,
    ) : SearchSuggestionUiState
}
