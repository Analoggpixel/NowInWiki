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

package com.google.samples.apps.nowinandroid.core.domain

import com.google.samples.apps.nowinandroid.core.data.repository.DEFAULT_WIKI_SEARCH_PAGE_LIMIT
import com.google.samples.apps.nowinandroid.core.data.repository.WikiSearchRepository
import com.google.samples.apps.nowinandroid.core.model.data.WikiLanguage
import com.google.samples.apps.nowinandroid.core.model.data.WikiSearchPagesResult
import javax.inject.Inject

/**
 * Loads one page of Action API wiki search results for infinite-scroll / Paging.
 */
class GetWikiSearchPagesUseCase @Inject constructor(
    private val wikiSearchRepository: WikiSearchRepository,
) {
    suspend operator fun invoke(
        query: String,
        language: WikiLanguage,
        offset: Int,
        limit: Int = DEFAULT_WIKI_SEARCH_PAGE_LIMIT,
    ): WikiSearchPagesResult =
        wikiSearchRepository.searchPages(
            query = query.trim(),
            language = language,
            offset = offset,
            limit = limit,
        )
}
