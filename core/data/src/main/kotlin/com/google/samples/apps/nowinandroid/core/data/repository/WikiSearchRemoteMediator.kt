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
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.google.samples.apps.nowinandroid.core.data.model.asExternalModel
import com.google.samples.apps.nowinandroid.core.data.model.asSearchResultEntity
import com.google.samples.apps.nowinandroid.core.database.dao.WikiSearchDao
import com.google.samples.apps.nowinandroid.core.database.model.WikiSearchRemoteKeysEntity
import com.google.samples.apps.nowinandroid.core.database.model.WikiSearchResultEntity
import com.google.samples.apps.nowinandroid.core.model.data.WikiLanguage
import com.google.samples.apps.nowinandroid.core.network.WikipediaNetworkDataSource
import kotlinx.coroutines.CancellationException

/**
 * Loads Action API search pages into Room so the UI [PagingSource] can page beyond
 * in-memory [androidx.paging.PagingConfig.maxSize].
 */
@OptIn(ExperimentalPagingApi::class)
internal class WikiSearchRemoteMediator(
    private val query: String,
    private val language: WikiLanguage,
    private val pageSize: Int,
    private val wikiSearchDao: WikiSearchDao,
    private val network: WikipediaNetworkDataSource,
) : RemoteMediator<Int, WikiSearchResultEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, WikiSearchResultEntity>,
    ): MediatorResult {
        val languageCode = language.code
        return try {
            // API-side pagination: MediaWiki `gsroffset` sent on each network request.
            val offset = when (loadType) {
                LoadType.REFRESH -> 0
                LoadType.PREPEND -> {
                    return MediatorResult.Success(endOfPaginationReached = true)
                }
                LoadType.APPEND -> {
                    val nextOffset = wikiSearchDao.remoteKeys(query, languageCode)?.nextOffset
                    if (nextOffset == null) {
                        return MediatorResult.Success(endOfPaginationReached = true)
                    }
                    nextOffset
                }
            }

            val networkResult = network
                .searchPages(
                    query = query,
                    language = language,
                    offset = offset,
                    limit = pageSize,
                )
                .asExternalModel(language)

            // DB-side ordering: first `position` for this batch in `wiki_search_results`.
            // Usually matches [offset] when every page is written successfully; stored per row
            // as `position`, not as a separate column.
            val startPosition = when (loadType) {
                LoadType.REFRESH -> 0
                else -> (wikiSearchDao.lastPosition(query, languageCode) ?: -1) + 1
            }
            val existingPageIds = when (loadType) {
                LoadType.REFRESH -> mutableSetOf()
                else -> wikiSearchDao.pageIds(query, languageCode).toMutableSet()
            }
            var position = startPosition
            val entities = buildList {
                for (item in networkResult.items) {
                    if (item.pageId in existingPageIds) continue
                    existingPageIds += item.pageId
                    add(
                        item.asSearchResultEntity(
                            searchQuery = query,
                            position = position,
                        ),
                    )
                    position++
                }
            }

            wikiSearchDao.replaceSearchPage(
                searchQuery = query,
                language = languageCode,
                clearExisting = loadType == LoadType.REFRESH,
                // Keep only the active search in the temp cache.
                clearAllCaches = loadType == LoadType.REFRESH,
                results = entities,
                remoteKeys = WikiSearchRemoteKeysEntity(
                    searchQuery = query,
                    language = languageCode,
                    nextOffset = networkResult.nextOffset,
                ),
            )

            MediatorResult.Success(
                endOfPaginationReached = networkResult.nextOffset == null,
            )
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (t: Throwable) {
            MediatorResult.Error(t)
        }
    }
}
