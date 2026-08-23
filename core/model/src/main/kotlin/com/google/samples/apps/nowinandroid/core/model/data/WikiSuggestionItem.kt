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

package com.google.samples.apps.nowinandroid.core.model.data

/**
 * Internal app representation of a single wiki search suggestion item.
 *
 * This model is intentionally flatter than the network DTO so upper layers do not need to know
 * about source-specific response nesting such as the `thumbnail` object.
 */
data class WikiSuggestionItem(
    val id: Long,
    val key: String,
    val title: String,
    val description: String? = null,
    val excerpt: String? = null,
    val thumbnailUrl: String? = null,
    val itemLanguage: WikiLanguage,
    /** Stable list index for paging lists; distinct from [id] (page id). */
    val listPosition: Int = 0,
)
