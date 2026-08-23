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

package com.google.samples.apps.nowinandroid.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Network representation of MediaWiki Action API search via
 * `generator=search` + `prop=pageimages|description` (`formatversion=2`).
 *
 * @see <a href="https://www.mediawiki.org/wiki/API:Search">API:Search</a>
 * @see <a href="https://www.mediawiki.org/wiki/Extension:PageImages#API">PageImages</a>
 */
@Serializable
data class NetworkWikiSearchResponse(
    @SerialName("continue")
    val continueData: NetworkWikiSearchContinue? = null,
    val query: NetworkWikiSearchQuery? = null,
) {
    /** Next `gsroffset` for pagination, or null when there is no further page. */
    val nextOffset: Int?
        get() = continueData?.gsroffset

    /**
     * Search hits in relevance order (`index` ascending).
     *
     * Generator responses may not preserve order in [NetworkWikiSearchQuery.pages].
     */
    val results: List<NetworkWikiSearchResult>
        get() = query?.pages.orEmpty().sortedBy { it.index ?: Int.MAX_VALUE }
}

@Serializable
data class NetworkWikiSearchContinue(
    val gsroffset: Int? = null,
    @SerialName("continue")
    val continueToken: String? = null,
)

@Serializable
data class NetworkWikiSearchQuery(
    val pages: List<NetworkWikiSearchResult> = emptyList(),
)

/**
 * A single hit from `generator=search` with page images / description props.
 *
 * [index] is the search-rank position added by the search generator.
 */
@Serializable
data class NetworkWikiSearchResult(
    val pageid: Long,
    val ns: Int = 0,
    val title: String,
    val index: Int? = null,
    val thumbnail: NetworkWikiSearchThumbnail? = null,
    val description: String? = null,
    @SerialName("descriptionsource")
    val descriptionSource: String? = null,
)

@Serializable
data class NetworkWikiSearchThumbnail(
    val source: String? = null,
    val width: Int? = null,
    val height: Int? = null,
)
