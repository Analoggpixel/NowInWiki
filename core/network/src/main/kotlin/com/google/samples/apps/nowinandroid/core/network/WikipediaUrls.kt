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

import android.net.Uri
import com.google.samples.apps.nowinandroid.core.model.data.WikiLanguage

/**
 * Builds absolute Wikipedia REST / site URLs for a given [WikiLanguage].
 *
 * Used with Retrofit `@Url` so requests are not tied to a fixed `baseUrl` host.
 */
fun wikipediaRestBaseUrl(language: WikiLanguage): String =
    "https://${language.code}.wikipedia.org/w/rest.php/v1/"

fun wikipediaSiteBaseUrl(language: WikiLanguage): String =
    "https://${language.code}.wikipedia.org/"

fun wikipediaSearchPageUrl(language: WikiLanguage, query: String): String =
    "${wikipediaRestBaseUrl(language)}search/page?q=${Uri.encode(query)}"

fun wikipediaPageWithHtmlUrl(language: WikiLanguage, title: String): String =
    "${wikipediaRestBaseUrl(language)}page/${Uri.encode(title)}/with_html"
