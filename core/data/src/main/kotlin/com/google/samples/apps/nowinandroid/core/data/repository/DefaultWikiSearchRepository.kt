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

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.google.samples.apps.nowinandroid.core.data.model.asExternalModel
import com.google.samples.apps.nowinandroid.core.database.dao.WikiSearchDao
import com.google.samples.apps.nowinandroid.core.model.data.WikiLanguage
import com.google.samples.apps.nowinandroid.core.model.data.WikiSearchPageItem
import com.google.samples.apps.nowinandroid.core.model.data.WikiSearchPagesResult
import com.google.samples.apps.nowinandroid.core.network.WikipediaNetworkDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class DefaultWikiSearchRepository @Inject constructor(
    private val wikipediaNetworkDataSource: WikipediaNetworkDataSource,
    private val wikiSearchDao: WikiSearchDao,
) : WikiSearchRepository {

    override suspend fun searchPages(
        query: String,
        language: WikiLanguage,
        offset: Int,
        limit: Int,
    ): WikiSearchPagesResult =
        wikipediaNetworkDataSource
            .searchPages(
                query = query,
                language = language,
                offset = offset,
                limit = limit,
            )
            .asExternalModel(language)

    @OptIn(ExperimentalPagingApi::class)
    override fun searchPagesPagingData(
        query: String,
        language: WikiLanguage,
    ): Flow<PagingData<WikiSearchPageItem>> =
        Pager(
            config = PagingConfig(
                pageSize = DEFAULT_WIKI_SEARCH_PAGE_LIMIT,
                initialLoadSize = DEFAULT_WIKI_SEARCH_PAGE_LIMIT,
                prefetchDistance = 5,
                maxSize = WIKI_SEARCH_PAGING_MAX_SIZE,
                enablePlaceholders = false,
            ),
            remoteMediator = WikiSearchRemoteMediator(
                query = query,
                language = language,
                pageSize = DEFAULT_WIKI_SEARCH_PAGE_LIMIT,
                wikiSearchDao = wikiSearchDao,
                network = wikipediaNetworkDataSource,
            ),
            pagingSourceFactory = {
                wikiSearchDao.pagingSource(
                    searchQuery = query,
                    language = language.code,
                )
            },
        ).flow.map { pagingData ->
            pagingData.map { entity -> entity.asExternalModel() }
        }
}
