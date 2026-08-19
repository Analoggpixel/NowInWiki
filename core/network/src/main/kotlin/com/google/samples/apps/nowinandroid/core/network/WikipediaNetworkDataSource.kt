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

package com.google.samples.apps.nowinandroid.core.network

import com.google.samples.apps.nowinandroid.core.model.data.WikiLanguage
import com.google.samples.apps.nowinandroid.core.network.model.NetworkWikiSuggestionsResponse

/**
 * Interface representing network calls to the Wikipedia / MediaWiki REST API and PCS.
 */
interface WikipediaNetworkDataSource {
    suspend fun searchSuggestions(
        query: String,
        language: WikiLanguage,
    ): NetworkWikiSuggestionsResponse

    /**
     * PCS mobile-html document for [title] (`GET /api/rest_v1/page/mobile-html/{title}`).
     *
     * Returns raw HTML (not JSON).
     */
    suspend fun getMobileHtml(
        title: String,
        language: WikiLanguage,
    ): String

    /**
     * Schemeless CSS/JS URLs for rendering [getMobileHtml] offline / in a WebView.
     *
     * `GET /api/rest_v1/page/mobile-html-offline-resources/{title}`
     */
    suspend fun getMobileHtmlOfflineResources(
        title: String,
        language: WikiLanguage,
    ): List<String>

    /**
     * Title of a random article from the [language] edition.
     *
     * `GET /api/rest_v1/page/random/title`
     */
    suspend fun getRandomPageTitle(
        language: WikiLanguage,
    ): String
}
