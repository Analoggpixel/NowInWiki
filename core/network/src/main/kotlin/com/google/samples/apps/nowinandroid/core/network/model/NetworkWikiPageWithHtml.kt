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
 * Network representation of a Wikipedia page returned by
 * `GET /w/rest.php/v1/page/{title}/with_html`.
 *
 * Verified against live responses such as:
 * `https://en.wikipedia.org/w/rest.php/v1/page/Kotlin/with_html`
 */
@Serializable
data class NetworkWikiPageWithHtml(
    val id: Long,
    val key: String,
    val title: String,
    val latest: NetworkWikiPageLatest,
    @SerialName("content_model")
    val contentModel: String,
    val license: NetworkWikiPageLicense,
    val html: String,
)

/**
 * Latest revision metadata nested under a Wikipedia page response.
 */
@Serializable
data class NetworkWikiPageLatest(
    val id: Long,
    val timestamp: String,
)

/**
 * License metadata nested under a Wikipedia page response.
 */
@Serializable
data class NetworkWikiPageLicense(
    val url: String,
    val title: String,
)
