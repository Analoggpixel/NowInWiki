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

package com.google.samples.apps.nowinandroid.core.domain

import com.google.samples.apps.nowinandroid.core.data.repository.WikiPageRepository
import com.google.samples.apps.nowinandroid.core.model.data.WikiLanguage
import com.google.samples.apps.nowinandroid.core.model.data.WikiPage
import javax.inject.Inject

/**
 * A use case which returns a Wikipedia page (including rendered HTML)
 * for the given title and language edition.
 */
class GetWikiPageUseCase @Inject constructor(
    private val wikiPageRepository: WikiPageRepository,
) {
    suspend operator fun invoke(
        title: String,
        language: WikiLanguage,
    ): WikiPage =
        wikiPageRepository.getPage(
            title = title.trim(),
            language = language,
        )
}
