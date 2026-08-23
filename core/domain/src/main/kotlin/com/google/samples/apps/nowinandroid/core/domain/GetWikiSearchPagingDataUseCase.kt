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

import androidx.paging.PagingData
import com.google.samples.apps.nowinandroid.core.data.repository.WikiSearchRepository
import com.google.samples.apps.nowinandroid.core.model.data.WikiLanguage
import com.google.samples.apps.nowinandroid.core.model.data.WikiSearchPageItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Paging stream of Action API wiki search results for the results screen.
 */
class GetWikiSearchPagingDataUseCase @Inject constructor(
    private val wikiSearchRepository: WikiSearchRepository,
) {
    operator fun invoke(
        query: String,
        language: WikiLanguage,
    ): Flow<PagingData<WikiSearchPageItem>> =
        wikiSearchRepository.searchPagesPagingData(
            query = query.trim(),
            language = language,
        )
}
