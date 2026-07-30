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
import com.google.samples.apps.nowinandroid.core.network.BuildConfig
import com.google.samples.apps.nowinandroid.core.network.WikipediaNetworkDataSource
import com.google.samples.apps.nowinandroid.core.network.model.NetworkWikiSuggestionsResponse
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import javax.inject.Inject
import javax.inject.Singleton

private interface RetrofitWikipediaApi {
    @GET("search/page")
    suspend fun searchSuggestions(
        @Query("q") query: String,
    ): NetworkWikiSuggestionsResponse
}

private const val WIKIPEDIA_BASE_URL = BuildConfig.WIKIPEDIA_BASE_URL

/**
 * [Retrofit] backed [WikipediaNetworkDataSource].
 *
 * This first version only supports search suggestions through the Wikipedia REST API.
 */
@Singleton
internal class RetrofitWikipediaNetwork @Inject constructor(
    networkJson: Json,
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

    override suspend fun searchSuggestions(query: String): NetworkWikiSuggestionsResponse =
        wikipediaApi.searchSuggestions(query = query).also {
            // Temporary connectivity debug log. Remove after suggestion chain is verified.
            Log.d("WikiSuggestions", "network searchSuggestions query=$query pages=${it.pages.size}")
        }
}
