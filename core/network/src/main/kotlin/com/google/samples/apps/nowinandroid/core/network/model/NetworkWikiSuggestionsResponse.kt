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
 * Network representation of the Wikipedia search suggestions response returned by
 * `/w/rest.php/v1/search/page?q={query}`.
 */
@Serializable
data class NetworkWikiSuggestionsResponse(
    val pages: List<NetworkWikiSuggestionItem> = emptyList(),
)

/**
 * Network representation of a single Wikipedia search suggestion item.
 *
 * Example fields were verified against the live response for:
 * `https://en.wikipedia.org/w/rest.php/v1/search/page?q=Kotlin`
 */
@Serializable
data class NetworkWikiSuggestionItem(
    val id: Long,
    val key: String,
    val title: String,
    val excerpt: String? = null,
    @SerialName("matched_title")
    val matchedTitle: String? = null,
    val anchor: String? = null,
    val description: String? = null,
    val thumbnail: NetworkWikiThumbnail? = null,
)

/**
 * Network representation of the thumbnail metadata nested under a search item.
 */
@Serializable
data class NetworkWikiThumbnail(
    val mimetype: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val duration: Int? = null,
    val url: String? = null,
)
