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

package com.google.samples.apps.nowinandroid.core.network.retrofit

import android.util.Log
import androidx.tracing.trace
import com.google.samples.apps.nowinandroid.core.model.data.WikiLanguage
import com.google.samples.apps.nowinandroid.core.network.BuildConfig
import com.google.samples.apps.nowinandroid.core.network.WikipediaNetworkDataSource
import com.google.samples.apps.nowinandroid.core.network.model.NetworkWikiSuggestionsResponse
import com.google.samples.apps.nowinandroid.core.network.wikipediaMobileHtmlOfflineResourcesUrl
import com.google.samples.apps.nowinandroid.core.network.wikipediaMobileHtmlUrl
import com.google.samples.apps.nowinandroid.core.network.wikipediaSearchPageUrl
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url
import javax.inject.Inject
import javax.inject.Singleton

private interface RetrofitWikipediaApi {
    @GET
    suspend fun searchSuggestions(
        @Url url: String,
    ): NetworkWikiSuggestionsResponse

    /** Raw body for non-JSON or hand-parsed JSON responses (PCS HTML / resource lists). */
    @GET
    suspend fun getRawBody(
        @Url url: String,
    ): ResponseBody
}

/**
 * Retrofit 要求的占位 baseUrl（[BuildConfig.WIKIPEDIA_BASE_URL]）。
 *
 * 真正的 Wikipedia 请求会传入由 [wikipediaSearchPageUrl] / [wikipediaMobileHtmlUrl] 等
 * 按 [WikiLanguage] 拼好的绝对 `@Url`，因此该 host 不参与解析这些接口的实际地址，
 * 仅用于满足 Retrofit.Builder.baseUrl。
 */
private const val WIKIPEDIA_BASE_URL = BuildConfig.WIKIPEDIA_BASE_URL

/**
 * [Retrofit] backed [WikipediaNetworkDataSource].
 */
@Singleton
internal class RetrofitWikipediaNetwork @Inject constructor(
    private val networkJson: Json,
    okhttpCallFactory: dagger.Lazy<Call.Factory>,
) : WikipediaNetworkDataSource {

    private val wikipediaApi = trace("RetrofitWikipediaNetwork") {
        Retrofit.Builder()
            .baseUrl(WIKIPEDIA_BASE_URL)
            // Keep the same lazy callFactory pattern as RetrofitNiaNetwork.
            .callFactory { request -> okhttpCallFactory.get().newCall(request) }
            .addConverterFactory(
                networkJson.asConverterFactory("application/json".toMediaType()),
            )
            .build()
            .create(RetrofitWikipediaApi::class.java)
    }

    override suspend fun searchSuggestions(
        query: String,
        language: WikiLanguage,
    ): NetworkWikiSuggestionsResponse {
        val url = wikipediaSearchPageUrl(language = language, query = query)
        return wikipediaApi.searchSuggestions(url = url).also {
            // Temporary connectivity debug log. Remove after suggestion chain is verified.
            Log.d(
                "WikiSuggestions",
                "network searchSuggestions language=${language.code} url=$url pages=${it.pages.size}",
            )
        }
    }

    override suspend fun getMobileHtml(
        title: String,
        language: WikiLanguage,
    ): String =
        wikipediaApi.getRawBody(
            url = wikipediaMobileHtmlUrl(language = language, title = title),
        ).use { it.string() }

    override suspend fun getMobileHtmlOfflineResources(
        title: String,
        language: WikiLanguage,
    ): List<String> {
        val body = wikipediaApi.getRawBody(
            url = wikipediaMobileHtmlOfflineResourcesUrl(language = language, title = title),
        ).use { it.string() }
        return networkJson.decodeFromString<List<String>>(body)
    }
}
