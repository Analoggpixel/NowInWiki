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

package com.google.samples.apps.nowinandroid.core.data.repository

import android.util.Log
import com.google.samples.apps.nowinandroid.core.data.model.asExternalModel
import com.google.samples.apps.nowinandroid.core.model.data.WikiSuggestionsResult
import com.google.samples.apps.nowinandroid.core.network.WikipediaNetworkDataSource
import javax.inject.Inject

internal class DefaultWikiSuggestionRepository @Inject constructor(
    private val wikipediaNetworkDataSource: WikipediaNetworkDataSource,
) : WikiSuggestionRepository {

    override suspend fun getSuggestions(query: String): WikiSuggestionsResult {
        // Temporary connectivity debug log. Remove after suggestion chain is verified.
        Log.d("WikiSuggestions", "repository getSuggestions query=$query")
        val result = wikipediaNetworkDataSource.searchSuggestions(query).asExternalModel()
        // Temporary connectivity debug log. Remove after suggestion chain is verified.
        Log.d("WikiSuggestions", "repository mapped suggestions count=${result.items.size}")
        return result
    }
}
